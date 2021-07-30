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
package org.everrest.groovy.servlet;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import org.codehaus.groovy.control.CompilationFailedException;
import org.everrest.core.Filter;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.groovy.DefaultGroovyResourceLoader;
import org.everrest.groovy.GroovyClassLoaderProvider;
import org.everrest.groovy.ScriptFinder;
import org.everrest.groovy.ScriptFinderFactory;
import org.everrest.groovy.SourceFile;
import org.everrest.groovy.URLFilter;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class GroovyEverrestServletContextInitializer extends EverrestServletContextInitializer {
  private static final org.slf4j.Logger LOG =
      LoggerFactory.getLogger(GroovyEverrestServletContextInitializer.class);

  public static final String EVERREST_GROOVY_ROOT_RESOURCES = "org.everrest.groovy.root.resources";
  public static final String EVERREST_GROOVY_APPLICATION = "org.everrest.groovy.Application";
  public static final String EVERREST_GROOVY_SCAN_COMPONENTS =
      "org.everrest.groovy.scan.components";

  protected final GroovyClassLoaderProvider classLoaderProvider;
  protected final URL[] groovyClassPath;

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

      classLoaderProvider
          .getGroovyClassLoader()
          .setResourceLoader(new DefaultGroovyResourceLoader(groovyClassPath));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public Application getApplication() {
    final String groovyApplicationFQN = getParameter(EVERREST_GROOVY_APPLICATION);
    final boolean scan = getBoolean(EVERREST_GROOVY_SCAN_COMPONENTS, false);
    Application groovyApplication = null;

    if (groovyApplicationFQN != null) {
      if (scan) {
        LOG.warn(
            "Scan of Groovy JAX-RS components is disabled cause to specified 'org.everrest.groovy.Application'.");
      }

      Class<?> applicationClass;
      try {
        applicationClass =
            classLoaderProvider.getGroovyClassLoader().loadClass(groovyApplicationFQN, true, false);
        groovyApplication = (Application) applicationClass.newInstance();
      } catch (CompilationFailedException
          | IllegalAccessException
          | InstantiationException
          | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else if (scan) {
      try {
        final Set<Class<?>> scanned = new HashSet<>();
        final Class[] jaxrsAnnotations = new Class[] {Path.class, Provider.class, Filter.class};
        final URLFilter filter =
            new URLFilter() {
              @Override
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
                      LOG.debug("Add class : {}", clazz);
                    }
                  } else {
                    LOG.warn("Skip duplicated class: {}", clazz);
                  }
                }
              }
            }
          } else {
            LOG.warn(
                "Skip URL : {}. Protocol '{}' is not supported for scan JAX-RS components",
                path,
                protocol);
          }
        }
        groovyApplication =
            new Application() {
              @Override
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
