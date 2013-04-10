/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
