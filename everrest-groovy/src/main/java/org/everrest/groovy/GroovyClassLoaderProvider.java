/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.groovy;

import groovy.lang.GroovyClassLoader;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Factory of Groovy class loader. It can provide preset GroovyClassLoader instance or customized instance of GroovyClassLoader able
 * resolve
 * additional Groovy source files.
 *
 * @author andrew00x
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
     * Get customized instance of GroovyClassLoader that able to resolve additional Groovy source files.
     *
     * @param sources
     *         additional Groovy sources
     * @return GroovyClassLoader
     * @throws MalformedURLException
     *         if any of entries in <code>sources</code> has invalid URL.
     */
    public ExtendedGroovyClassLoader getGroovyClassLoader(SourceFolder[] sources) throws MalformedURLException {
        if (sources == null || sources.length == 0) {
            return getGroovyClassLoader();
        }
        URL[] roots = new URL[sources.length];
        for (int i = 0; i < sources.length; i++) {
            roots[i] = sources[i].getPath();
        }
        GroovyClassLoader parent = getGroovyClassLoader();
        ExtendedGroovyClassLoader classLoader = new ExtendedGroovyClassLoader(parent);
        classLoader.setResourceLoader(new DefaultGroovyResourceLoader(roots));
        return classLoader;
    }
}
