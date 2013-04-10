/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.everrest.groovy;

import groovy.lang.GroovyClassLoader;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Factory of Groovy class loader. It can provide preset GroovyClassLoader
 * instance or customized instance of GroovyClassLoader able resolve additional
 * Groovy source files.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyClassLoaderProvider {
    /** Preset default GroovyClassLoader. */
    private ExtendedGroovyClassLoader defaultClassLoader;

    public GroovyClassLoaderProvider() {
        this(new ExtendedGroovyClassLoader(GroovyClassLoaderProvider.class.getClassLoader()));
    }

    protected GroovyClassLoaderProvider(ExtendedGroovyClassLoader defaultClassLoader) {
        this.defaultClassLoader = defaultClassLoader;
    }

    /**
     * Get default GroovyClassLoader.
     *
     * @return default GroovyClassLoader
     */
    public ExtendedGroovyClassLoader getGroovyClassLoader() {
        return defaultClassLoader;
    }

    /**
     * Get customized instance of GroovyClassLoader that able to resolve
     * additional Groovy source files.
     *
     * @param sources
     *         additional Groovy sources
     * @return GroovyClassLoader
     * @throws MalformedURLException
     *         if any of entries in <code>sources</code>
     *         has invalid URL.
     */
    public ExtendedGroovyClassLoader getGroovyClassLoader(SourceFolder[] sources) throws MalformedURLException {
        if (sources == null || sources.length == 0)
            return getGroovyClassLoader();
        URL[] roots = new URL[sources.length];
        for (int i = 0; i < sources.length; i++)
            roots[i] = sources[i].getPath();
        GroovyClassLoader parent = getGroovyClassLoader();
        ExtendedGroovyClassLoader classLoader = new ExtendedGroovyClassLoader(parent);
        classLoader.setResourceLoader(new DefaultGroovyResourceLoader(roots));
        return classLoader;
    }
}
