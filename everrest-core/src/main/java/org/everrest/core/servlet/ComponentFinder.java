/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.everrest.core.servlet;

import org.everrest.core.Filter;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** @author andrew00x */
@HandlesTypes({Path.class, Provider.class, Filter.class})
public class ComponentFinder implements ServletContainerInitializer {

    private static Set<Class<?>> scanned = new LinkedHashSet<Class<?>>();

    public static Set<Class<?>> findComponents() {
        //System.out.println("\n"+scanned+"\n");
        return scanned;
    }

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if (c != null) {
            List<String> skip = new LinkedList<String>();
            String skipParameter = ctx.getInitParameter(EverrestServletContextInitializer.EVERREST_SCAN_SKIP_PACKAGES);
            if (skipParameter != null) {
                for (String s : skipParameter.split(",")) {
                    skip.add(s.trim());
                }
            }
            skip.add("org.everrest.core");
            skip.add("javax.ws.rs");
            for (Class<?> clazz : c) {
                if (!clazz.isInterface()                            // skip interfaces
                    && !Modifier.isAbstract(clazz.getModifiers())   // skip abstract classes
                    && (clazz.getEnclosingClass() == null)          // skip anonymous and local classes
                    && !isSkipped(skip, clazz)) {                   // skip internal stuff
                    scanned.add(clazz);
                }
            }
        }
    }

    private boolean isSkipped(List<String> skip, Class<?> clazz) {
        final String clazzName = clazz.getName();
        for (String s : skip) {
            if (clazzName.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
