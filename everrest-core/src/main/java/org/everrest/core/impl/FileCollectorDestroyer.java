/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

/**
 * Stop FileCollector if FileCollector class is loaded by the context class loader. Typically it means FileCollector
 * loaded from .war file by the web application class loader. Do nothing if class is loaded by another class loader than
 * context class loader.
 *
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class FileCollectorDestroyer {
    public void stopFileCollector() {
        FileCollector fc = FileCollector.getInstance();
        Class<? extends FileCollector> fcClass = fc.getClass();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl == fcClass.getClassLoader()) {
            fc.stop();
        }
    }
}
