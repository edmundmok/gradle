/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.tasks.scala;

import com.google.common.collect.ImmutableList;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.scala.ScalaCompilerFactory;
import org.gradle.api.internal.tasks.scala.ScalaJavaJointCompileSpec;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.ScalaRuntime;
import org.gradle.api.tasks.scala.internal.ScalaCompileOptionsConfigurer;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.internal.classloader.ClasspathHasher;
import org.gradle.language.scala.tasks.AbstractScalaCompile;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.process.internal.worker.child.WorkerDirectoryProvider;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.WorkerDaemonFactory;

import javax.inject.Inject;

/**
 * Compiles Scala source files, and optionally, Java source files.
 */
@CacheableTask
public class ScalaCompile extends AbstractScalaCompile {

    private FileCollection scalaClasspath;
    private FileCollection zincClasspath;
    private FileCollection scalaCompilerPlugins;
    private final Property<ScalaRuntime> scalaRuntime;
    private org.gradle.language.base.internal.compile.Compiler<ScalaJavaJointCompileSpec> compiler;

    @Inject
    public ScalaCompile() {
        ObjectFactory objectFactory = getObjectFactory();
        this.scalaRuntime = objectFactory.property(ScalaRuntime.class);
    }

    @Nested
    @Override
    public ScalaCompileOptions getScalaCompileOptions() {
        return (ScalaCompileOptions) super.getScalaCompileOptions();
    }

    /**
     * Returns the classpath to use to load the Scala compiler.
     */
    @Classpath
    public FileCollection getScalaClasspath() {
        return scalaClasspath;
    }

    public void setScalaClasspath(FileCollection scalaClasspath) {
        this.scalaClasspath = scalaClasspath;
    }

    /**
     * Returns the Scala compiler plugins to use.
     *
     * @since 6.4
     */
    @Classpath
    public FileCollection getScalaCompilerPlugins() {
        return scalaCompilerPlugins;
    }

    /**
     * Sets the Scala compiler plugins to use.
     *
     * @param scalaCompilerPlugins Collection of Scala compiler plugins.
     * @since 6.4
     */
    public void setScalaCompilerPlugins(FileCollection scalaCompilerPlugins) {
        this.scalaCompilerPlugins = scalaCompilerPlugins;
    }

    @Override
    protected ScalaJavaJointCompileSpec createSpec() {
        ScalaCompileOptionsConfigurer.configure(getScalaCompileOptions(), getToolchain(), getScalaClasspath().getFiles());
        ScalaJavaJointCompileSpec spec = super.createSpec();
        if (getScalaCompilerPlugins() != null) {
            spec.setScalaCompilerPlugins(ImmutableList.copyOf(getScalaCompilerPlugins()));
        }
        return spec;
    }

    /**
     * Returns the classpath to use to load the Zinc incremental compiler. This compiler in turn loads the Scala compiler.
     */
    @Classpath
    public FileCollection getZincClasspath() {
        return zincClasspath;
    }

    public void setZincClasspath(FileCollection zincClasspath) {
        this.zincClasspath = zincClasspath;
    }

    /**
     * For testing only.
     */
    public void setCompiler(org.gradle.language.base.internal.compile.Compiler<ScalaJavaJointCompileSpec> compiler) {
        this.compiler = compiler;
    }

    @Override
    protected org.gradle.language.base.internal.compile.Compiler<ScalaJavaJointCompileSpec> getCompiler(ScalaJavaJointCompileSpec spec) {
        assertScalaClasspathIsNonEmpty();
        if (compiler == null) {
            WorkerDaemonFactory workerDaemonFactory = getServices().get(WorkerDaemonFactory.class);
            JavaForkOptionsFactory forkOptionsFactory = getServices().get(JavaForkOptionsFactory.class);
            ClassPathRegistry classPathRegistry = getServices().get(ClassPathRegistry.class);
            ClassLoaderRegistry classLoaderRegistry = getServices().get(ClassLoaderRegistry.class);
            ActionExecutionSpecFactory actionExecutionSpecFactory = getServices().get(ActionExecutionSpecFactory.class);
            ScalaCompilerFactory scalaCompilerFactory = new ScalaCompilerFactory(
                getServices().get(WorkerDirectoryProvider.class).getWorkingDirectory(), workerDaemonFactory, getScalaClasspath(),
                getZincClasspath(), forkOptionsFactory, classPathRegistry, classLoaderRegistry, actionExecutionSpecFactory,
                getServices().get(ClasspathHasher.class));
            compiler = scalaCompilerFactory.newCompiler(spec);
        }
        return compiler;
    }

    protected void assertScalaClasspathIsNonEmpty() {
        if (getScalaClasspath().isEmpty()) {
            throw new InvalidUserDataException("'" + getName() + ".scalaClasspath' must not be empty. If a Scala compile dependency is provided, "
                    + "the 'scala-base' plugin will attempt to configure 'scalaClasspath' automatically. Alternatively, you may configure 'scalaClasspath' explicitly.");
        }
    }
}
