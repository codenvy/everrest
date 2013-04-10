/*
 * Copyright (C) 2009 eXo Platform SAS.
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

import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.FieldInjector;
import org.everrest.core.impl.method.ParameterHelper;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.everrest.core.resource.ResourceDescriptorVisitor;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FieldInjectorImpl implements FieldInjector {
    ///** Logger. */
    //private static final Logger LOG = Logger.getLogger(FieldInjectorImpl.class);

    /** All annotations including JAX-RS annotation. */
    private final Annotation[] annotations;

    /** JAX-RS annotation. */
    private final Annotation annotation;

    /**
     * Default value for this parameter, default value can be used if there is
     * not found required parameter in request. See
     * {@link javax.ws.rs.DefaultValue}.
     */
    private final String defaultValue;

    /** See {@link javax.ws.rs.Encoded}. */
    private final boolean encoded;

    /** See {@link java.lang.reflect.Field} . */
    private final java.lang.reflect.Field jField;

    /**
     * Setter for field. If setter available it will be used for field initialization.
     * Otherwise field initialized directly.
     */
    private final Method setter;

    /**
     * @param resourceClass
     *         class that contains field <code>jField</code>
     * @param jField
     *         java.lang.reflect.Field
     */
    public FieldInjectorImpl(Class<?> resourceClass, java.lang.reflect.Field jField) {
        this.jField = jField;
        this.annotations = jField.getDeclaredAnnotations();
        this.setter = getSetter(resourceClass, jField);

        Annotation annotation = null;
        String defaultValue = null;
        boolean encoded = false;

        // is resource provider
        final boolean provider = resourceClass.getAnnotation(Provider.class) != null;
        List<String> allowedAnnotation = provider
                                         ? ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS
                                         : ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS;

        for (Annotation a : annotations) {
            Class<?> ac = a.annotationType();

            if (allowedAnnotation.contains(ac.getName())) {
                if (annotation != null) {
                    throw new RuntimeException(
                            "JAX-RS annotations on one of fields " + jField.toString() + " are equivocality. Annotations: "
                            + annotation.toString() + " and " + a.toString() + " can't be applied to one field. ");
                }
                annotation = a;
            } else if (ac == Encoded.class && !provider) {
                // @Encoded has not sense for Provider. Provider may use only @Context annotation for fields
                encoded = true;
            } else if (ac == DefaultValue.class && !provider) {
                // @Default has not sense for Provider. Provider may use only @Context annotation for fields
                defaultValue = ((DefaultValue)a).value();
            }
        }
        this.defaultValue = defaultValue;
        this.annotation = annotation;
        this.encoded = encoded || resourceClass.getAnnotation(Encoded.class) != null;
    }

    private static Method getSetter(Class<?> clazz, java.lang.reflect.Field jfield) {
        Method setter = null;
        try {
            String name = jfield.getName();
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            setter = clazz.getMethod(setterName, jfield.getType());
        } catch (NoSuchMethodException ignored) {
        }
        return setter;
    }

    /** {@inheritDoc} */
    public Annotation getAnnotation() {
        return annotation;
    }

    /** {@inheritDoc} */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    /** {@inheritDoc} */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** {@inheritDoc} */
    public Class<?> getParameterClass() {
        return jField.getType();
    }

    /** {@inheritDoc} */
    public Type getGenericType() {
        return jField.getGenericType();
    }

    /** {@inheritDoc} */
    public boolean isEncoded() {
        return encoded;
    }

    /** {@inheritDoc} */
    public String getName() {
        return jField.getName();
    }

    /** {@inheritDoc} */
    public void inject(Object resource, ApplicationContext context) {
        try {
            Object value = null;
            if (annotation != null) {
                value = ParameterResolverFactory.createParameterResolver(annotation).resolve(this, context);
            } else {
                DependencySupplier dependencies = context.getDependencySupplier();
                if (dependencies != null) {
                    value = dependencies.getComponent(this);
                }
            }

            if (value != null) {
                if (setter != null) {
                    setter.invoke(resource, value);
                } else {
                    if (!Modifier.isPublic(jField.getModifiers())) {
                        jField.setAccessible(true);
                    }
                    jField.set(resource, value);
                }
            }
        } catch (Exception e) {
            if (annotation != null) {
                Class<?> ac = annotation.annotationType();
                if (ac == PathParam.class || ac == QueryParam.class || ac == MatrixParam.class) {
                    throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND).build());
                }
                throw new WebApplicationException(e, Response.status(Response.Status.BAD_REQUEST).build());
            }
            throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    /** {@inheritDoc} */
    public void accept(ResourceDescriptorVisitor visitor) {
        visitor.visitFieldInjector(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ FieldInjectorImpl: ");
        sb.append("annotation: ");
        sb.append(getAnnotation());
        sb.append("; type: ");
        sb.append(getParameterClass());
        sb.append("; generic-type : ");
        sb.append(getGenericType());
        sb.append("; default-value: ");
        sb.append(getDefaultValue());
        sb.append("; encoded: ");
        sb.append(isEncoded());
        sb.append(" ]");
        return sb.toString();
    }

}
