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
package org.everrest.core.tools;

import org.everrest.core.FieldInjector;
import org.everrest.core.Parameter;
import org.everrest.core.impl.ConstructorParameter;
import org.everrest.core.impl.FieldInjectorImpl;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author andrew00x
 */
public class DependencySupplierImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void providesInstanceByParameter() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        Object instance = dependencySupplier.getInstance(createParameter(String.class, null, null));
        assertEquals("foo", instance);
    }

    @Test
    public void providesInstanceProviderByParameter() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        ParameterizedType stringProviderParameterizedType = newParameterizedType(Provider.class, String.class);

        Object instance = dependencySupplier.getInstance(createParameter(Provider.class, stringProviderParameterizedType, null));

        assertTrue(
                String.format("Instance of javax.inject.Provider expected but %s found", instance == null ? "null" : instance.getClass()),
                instance instanceof Provider);
        assertEquals("foo", ((Provider)instance).get());
    }

    @Test
    public void providesInstanceByType() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        Object instance = dependencySupplier.getInstance(String.class);
        assertEquals("foo", instance);
    }

    public static class SomeType {
        @Inject
        private String annotated;

        @MyInject
        private String annotatedWithCustomAnnotation;

        @Inject
        @Named("SomeName")
        private String annotatedAndNamed;

        private String notInjectable;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface MyInject {
    }

    @Test
    public void providesInstanceForInjectAnnotatedField() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        java.lang.reflect.Field field = SomeType.class.getDeclaredField("annotated");
        FieldInjector fieldInjector = createFieldInjector(field);
        Object instance = dependencySupplier.getInstance(fieldInjector);
        assertEquals("foo", instance);
    }

    @Test
    public void looksUpInstanceForInjectAnnotatedNamedField() throws Exception {
        DependencySupplierImpl dependencySupplier = spy(createDependencySupplier("foo", -1));
        java.lang.reflect.Field field = SomeType.class.getDeclaredField("annotatedAndNamed");
        FieldInjector fieldInjector = createFieldInjector(field);
        dependencySupplier.getInstance(fieldInjector);
        verify(dependencySupplier).getInstance(fieldInjector);
    }

    @Test
    public void providesInstanceForInjectFieldAnnotatedWithCustomAnnotation() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier(MyInject.class, "foo", -1);
        java.lang.reflect.Field field = SomeType.class.getDeclaredField("annotatedWithCustomAnnotation");
        FieldInjector fieldInjector = createFieldInjector(field);
        Object instance = dependencySupplier.getInstance(fieldInjector);
        assertEquals("foo", instance);
    }

    @Test
    public void providesNullInstanceForNotAnnotatedField() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        java.lang.reflect.Field field = SomeType.class.getDeclaredField("notInjectable");
        FieldInjector fieldInjector = createFieldInjector(field);
        Object instance = dependencySupplier.getInstance(fieldInjector);
        assertNull(instance);
    }

    @Test
    public void providesNullInstanceForFieldsAnnotatedWithNotSupportedAnnotation() throws Exception {
        DependencySupplierImpl dependencySupplier = createDependencySupplier("foo", -1);
        java.lang.reflect.Field field = SomeType.class.getDeclaredField("annotatedWithCustomAnnotation");
        FieldInjector fieldInjector = createFieldInjector(field);
        Object instance = dependencySupplier.getInstance(fieldInjector);
        assertNull(instance);
    }

    @Test
    public void failsCreateDependencySupplierWithNullInjectAnnotation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Inject annotation class may not be null");
        new DependencySupplierImpl(null);
    }

    private Parameter createParameter(Class<?> clazz, Type genericType, Annotation[] annotations) {
        return new ConstructorParameter(null,
                                        annotations == null ? new Annotation[0] : annotations,
                                        clazz,
                                        genericType,
                                        null,
                                        false);
    }

    private FieldInjector createFieldInjector(java.lang.reflect.Field field) {
        return new FieldInjectorImpl(field, new ParameterResolverFactory());
    }

    private DependencySupplierImpl createDependencySupplier(Object... instances) {
        DependencySupplierImpl dependencySupplier = new DependencySupplierImpl();
        for (Object instance : instances) {
            dependencySupplier.addInstance(instance.getClass(), instance);
        }
        return dependencySupplier;
    }

    private DependencySupplierImpl createDependencySupplier(Class<? extends Annotation> annotationType,
                                                            Object... instances) {
        DependencySupplierImpl dependencySupplier = new DependencySupplierImpl(annotationType);
        for (Object instance : instances) {
            dependencySupplier.addInstance(instance.getClass(), instance);
        }
        return dependencySupplier;
    }
}
