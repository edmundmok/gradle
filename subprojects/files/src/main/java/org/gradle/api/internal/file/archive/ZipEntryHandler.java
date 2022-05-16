/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.api.internal.file.archive;

import java.io.IOException;
import java.io.InputStream;

public interface ZipEntryHandler {
    /**
     * Returns the ZipEntry object associated with this handler
     */
    java.util.zip.ZipEntry getZipEntry();

    /**
     * Get an input stream positioned to read the content of this zip entry
     */
    InputStream getInputStream();

    /**
     * Close the entry and clean up any streams associated with it
     */
    void closeEntry() throws IOException;

    /**
     * Whether the entry can be reopened (by calling {@link #getInputStream()} again) once bytes have been read from it
     */
    boolean canReopen();
}
