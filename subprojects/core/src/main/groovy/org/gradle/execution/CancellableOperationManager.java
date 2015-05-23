/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.execution;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.initialization.BuildCancellationToken;

public interface CancellableOperationManager {

    /**
     * Executes the operation, while consuming System.in, watching for closure or EOT.
     *
     * Implies {@link #interruptYield(Transformer)}
     */
    <T> T monitorInputYield(Transformer<? extends T, ? super BuildCancellationToken> operation);

    /**
     * Executes the operation, while consuming System.in, watching for closure or EOT.
     *
     * Implies {@link #interruptExecute(Action)}
     */
    void monitorInputExecute(Action<? super BuildCancellationToken> operation);

    /**
     * If a cancel is initiated during the operation, interrupt the thread.
     */
    <T> T interruptYield(Transformer<? extends T, ? super BuildCancellationToken> operation);

    /**
     * If a cancel is initiated during the operation, interrupt the thread.
     */
    void interruptExecute(Action<? super BuildCancellationToken> operation);

}
