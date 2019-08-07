/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

/**
 * Stop FileCollector if FileCollector class is loaded by the context class loader. Typically it means FileCollector
 * loaded from .war file by the web application class loader. Do nothing if class is loaded by another class loader than
 * context class loader.
 *
 * @author andrew00x
 */
public class FileCollectorDestroyer {
    public void stopFileCollector() {
        FileCollector fileCollector = FileCollector.getInstance();
        Class<? extends FileCollector> fileCollectorClass = fileCollector.getClass();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == fileCollectorClass.getClassLoader()) {
            fileCollector.stop();
        }
    }
}
