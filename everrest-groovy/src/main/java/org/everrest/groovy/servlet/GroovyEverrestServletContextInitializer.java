/**
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

package org.everrest.groovy.servlet;

import org.codehaus.groovy.control.CompilationFailedException;
import org.everrest.core.Filter;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.util.Logger;
import org.everrest.groovy.DefaultGroovyResourceLoader;
import org.everrest.groovy.GroovyClassLoaderProvider;
import org.everrest.groovy.ScriptFinder;
import org.everrest.groovy.ScriptFinderFactory;
import org.everrest.groovy.SourceFile;
import org.everrest.groovy.URLFilter;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyEverrestServletContextInitializer.java 77 2010-10-26
 *          15:20:15Z andrew00x $
 */
public class GroovyEverrestServletContextInitializer extends EverrestServletContextInitializer {
    private static final Logger LOG = Logger.getLogger(GroovyEverrestServletContextInitializer.class);

    public static final String EVERREST_GROOVY_ROOT_RESOURCES  = "org.everrest.groovy.root.resources";
    public static final String EVERREST_GROOVY_APPLICATION     = "org.everrest.groovy.Application";
    public static final String EVERREST_GROOVY_SCAN_COMPONENTS = "org.everrest.groovy.scan.components";

    protected final GroovyClassLoaderProvider classLoaderProvider;
    protected final URL[]                     groovyClassPath;

    public GroovyEverrestServletContextInitializer(ServletContext sctx) {
        super(sctx);
        classLoaderProvider = new GroovyClassLoaderProvider();

        try {
            String rootResourcesParameter = getParameter(EVERREST_GROOVY_ROOT_RESOURCES);
            if (rootResourcesParameter != null) {
                String[] tokens = rootResourcesParameter.split(",");
                groovyClassPath = new URL[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    groovyClassPath[i] = URI.create(tokens[i]).toURL();
                }
            } else {
                groovyClassPath = new URL[0];
            }

            classLoaderProvider.getGroovyClassLoader().setResourceLoader(new DefaultGroovyResourceLoader(groovyClassPath));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Application getApplication() {
        final String groovyApplicationFQN = getParameter(EVERREST_GROOVY_APPLICATION);
        final boolean scan = getBoolean(EVERREST_GROOVY_SCAN_COMPONENTS, false);
        Application groovyApplication = null;

        if (groovyApplicationFQN != null) {
            if (scan) {
                LOG.warn("Scan of Groovy JAX-RS components is disabled cause to specified 'org.everrest.groovy.Application'.");
            }

            Class<?> applicationClass;
            try {
                applicationClass = classLoaderProvider.getGroovyClassLoader().loadClass(groovyApplicationFQN, true, false);
                groovyApplication = (Application)applicationClass.newInstance();
            } catch (CompilationFailedException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (scan) {
            try {
                final Set<Class<?>> scanned = new HashSet<Class<?>>();
                final Class[] jaxrsAnnotations = new Class[]{Path.class, Provider.class, Filter.class};
                final URLFilter filter = new URLFilter() {
                    public boolean accept(URL url) {
                        return url.getPath().endsWith(".groovy");
                    }
                };

                for (int i = 0; i < groovyClassPath.length; i++) {
                    final URL path = groovyClassPath[i];
                    final String protocol = path.getProtocol();
                    ScriptFinder finder = ScriptFinderFactory.getScriptFinder(protocol);
                    if (finder != null) {
                        URL[] scripts = finder.find(filter, path);
                        if (scripts != null && scripts.length > 0) {
                            SourceFile[] files = new SourceFile[scripts.length];

                            for (int k = 0; k < scripts.length; k++) {
                                files[k] = new SourceFile(scripts[k]);
                            }

                            Class[] classes = classLoaderProvider.getGroovyClassLoader().parseClasses(files);
                            for (int k = 0; k < classes.length; k++) {
                                Class clazz = classes[k];
                                if (findAnnotation(clazz, jaxrsAnnotations)) {
                                    boolean added = scanned.add(clazz);
                                    if (added) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug("Add class : " + clazz);
                                        }
                                    } else {
                                        LOG.warn("Skip duplicated class: " + clazz);
                                    }
                                }
                            }
                        }
                    } else {
                        LOG.warn("Skip URL : " + path + ". Protocol '" + protocol + "' is not supported for scan JAX-RS components. ");
                    }
                }
                groovyApplication = new Application() {
                    public Set<Class<?>> getClasses() {
                        return scanned;
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return groovyApplication;
    }

    private boolean findAnnotation(Class<?> clazz, Class<? extends Annotation>... annClasses) {
        for (Class<? extends Annotation> ac : annClasses) {
            if (clazz.getAnnotation(ac) != null) {
                return true;
            }
        }
        return false;
    }
}
