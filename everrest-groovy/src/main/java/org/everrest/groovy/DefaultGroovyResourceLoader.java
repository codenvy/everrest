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
package org.everrest.groovy;

import groovy.lang.GroovyResourceLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author andrew00x
 */
public class DefaultGroovyResourceLoader implements GroovyResourceLoader {
    private static final String DEFAULT_SOURCE_FILE_EXTENSION = ".groovy";
    protected URL[] roots;

    private final int maxEntries = 200;

    protected final Map<String, URL>                    resources;
    final           ConcurrentMap<String, FileNameLock> locks;

    @SuppressWarnings("serial")
    public DefaultGroovyResourceLoader(URL[] roots) throws MalformedURLException {
        this.roots = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            String str = roots[i].toString();
            if (str.charAt(str.length() - 1) != '/') {
                this.roots[i] = new URL(str + '/');
            } else {
                this.roots[i] = roots[i];
            }
        }
        resources = Collections.synchronizedMap(new LinkedHashMap<String, URL>(maxEntries + 1, 1.0f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, URL> eldest) {
                if (size() > maxEntries) {
                    locks.remove(eldest.getKey());
                    return true;
                }
                return false;
            }
        });
        locks = new ConcurrentHashMap<>();
    }

    public DefaultGroovyResourceLoader(URL root) throws MalformedURLException {
        this(new URL[]{root});
    }


    @Override
    public final URL loadGroovySource(String filename) throws MalformedURLException {
        String[] sourceFileExtensions = getSourceFileExtensions();
        URL resource = null;
        filename = filename.replace('.', '/');
        for (int i = 0; i < sourceFileExtensions.length && resource == null; i++) {
            resource = getResource(filename + sourceFileExtensions[i]);
        }
        return resource;
    }

    protected URL getResource(String filename) throws MalformedURLException {
        FileNameLock lock = locks.get(filename);
        if (lock == null) {
            FileNameLock newLock = new FileNameLock();
            lock = locks.putIfAbsent(filename, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }

        URL resource;
        synchronized (lock) {
            resource = resources.get(filename);
            final boolean inCache = resource != null;
            if (inCache && checkResource(resource)) {
                return resource;
            }
            resource = null; // Resource in cache is unreachable.
            for (int i = 0; i < roots.length && resource == null; i++) {
                URL tmp = createURL(roots[i], filename);
                if (checkResource(tmp)) {
                    resource = tmp;
                }
            }
            if (resource != null) {
                resources.put(filename, resource);
            } else if (inCache) {
                resources.remove(filename);
            }
        }
        return resource;
    }

    protected URL createURL(URL root, String filename) throws MalformedURLException {
        return new URL(root, filename);
    }

    protected boolean checkResource(URL resource) {
        try {
            resource.openStream().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected String[] getSourceFileExtensions() {
        return new String[]{DEFAULT_SOURCE_FILE_EXTENSION};
    }

    private static final class FileNameLock {
        private static final AtomicInteger counter = new AtomicInteger();
        private final        int           hash    = counter.incrementAndGet();

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return hash == ((FileNameLock)obj).hash;
        }
    }
}
