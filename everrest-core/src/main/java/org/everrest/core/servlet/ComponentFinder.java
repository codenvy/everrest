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
package org.everrest.core.servlet;

import org.everrest.core.Filter;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.lang.reflect.Modifier.isAbstract;
import static org.everrest.core.servlet.EverrestServletContextInitializer.EVERREST_SCAN_SKIP_PACKAGES;

/** @author andrew00x */
@HandlesTypes({Path.class, Provider.class, Filter.class})
public class ComponentFinder implements ServletContainerInitializer {

    private static Set<Class<?>> scanned = new LinkedHashSet<>();

    public static Set<Class<?>> findComponents() {
        return scanned;
    }

    private Set<String> defaultSkipPackages = new HashSet<>(Arrays.asList("org.everrest.core", "javax.ws.rs"));

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
