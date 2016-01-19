/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core;

import org.everrest.core.impl.ConstructorDescriptorImpl;
import org.everrest.core.impl.FieldInjectorImpl;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseObjectModel implements ObjectModel {
    protected final Class<?> clazz;

    /** Optional data. */
    protected MultivaluedMapImpl properties;

    /** Resource class constructors. */
    protected final List<ConstructorDescriptor> constructors;

    /** Resource class fields. */
    protected final List<FieldInjector> fields;

    public BaseObjectModel(Class<?> clazz) {
        this.clazz = clazz;
        this.constructors = new ArrayList<>();
        this.fields = new ArrayList<>();
        processConstructors();
        processFields();
    }

    protected void processConstructors() {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            constructors.add(new ConstructorDescriptorImpl(clazz, constructor));
        }
        if (constructors.size() == 0) {
            String msg = "Not found accepted constructors for provider class " + clazz.getName();
            throw new RuntimeException(msg);
        }
        // Sort constructors in number parameters order
        if (constructors.size() > 1) {
            Collections.sort(constructors, ConstructorDescriptorImpl.CONSTRUCTOR_COMPARATOR);
        }
    }

    protected void processFields() {
        for (java.lang.reflect.Field jField : clazz.getDeclaredFields()) {
            fields.add(new FieldInjectorImpl(clazz, jField));
        }
        Class<?> sc = clazz.getSuperclass();
        while (sc != null && sc != Object.class) {
            for (java.lang.reflect.Field jField : sc.getDeclaredFields()) {
                FieldInjector inj = new FieldInjectorImpl(clazz, jField);
                // Skip not annotated field. They will be not injected from container.
                if (inj.getAnnotation() != null) {
                    fields.add(inj);
                }
            }
            sc = sc.getSuperclass();
        }
    }

    public BaseObjectModel(Object instance) {
        this.clazz = instance.getClass();
        this.constructors = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    @Override
    public Class<?> getObjectClass() {
        return clazz;
    }

    @Override
    public List<ConstructorDescriptor> getConstructorDescriptors() {
        return constructors;
    }

    @Override
    public List<FieldInjector> getFieldInjectors() {
        return fields;
    }

    @Override
    public MultivaluedMap<String, String> getProperties() {
        if (properties == null) {
            properties = new MultivaluedMapImpl();
        }
        return properties;
    }

    @Override
    public List<String> getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        }
        return null;
    }
}
