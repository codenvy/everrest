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
package org.everrest.core.servlet;

import static java.lang.reflect.Modifier.isAbstract;
import static org.everrest.core.servlet.EverrestServletContextInitializer.EVERREST_SCAN_SKIP_PACKAGES;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.everrest.core.Filter;

/** @author andrew00x */
@HandlesTypes({Path.class, Provider.class, Filter.class})
public class ComponentFinder implements ServletContainerInitializer {

  private static Set<Class<?>> scanned = new LinkedHashSet<>();

  public static Set<Class<?>> findComponents() {
    return scanned;
  }

  private Set<String> defaultSkipPackages =
      new HashSet<>(Arrays.asList("org.everrest.core", "jakarta.ws.rs"));

  void reset() {
    defaultSkipPackages.clear();
    scanned.clear();
  }

  @Override
  public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
    if (classes != null) {
      List<String> skip = new LinkedList<>();
      String skipParameter = ctx.getInitParameter(EVERREST_SCAN_SKIP_PACKAGES);
      if (skipParameter != null) {
        for (String skipPrefix : skipParameter.split(",")) {
          skip.add(skipPrefix.trim());
        }
      }
      skip.addAll(defaultSkipPackages);
      for (Class<?> clazz : classes) {
        if (!clazz.isInterface()
            && !isAbstract(clazz.getModifiers())
            && (clazz.getEnclosingClass() == null)
            && !isSkipped(skip, clazz)) {
          scanned.add(clazz);
        }
      }
    }
  }

  private boolean isSkipped(List<String> forSkipping, Class<?> clazz) {
    final String clazzName = clazz.getName();
    for (String skipPrefix : forSkipping) {
      if (clazzName.startsWith(skipPrefix)) {
        return true;
      }
    }
    return false;
  }
}
