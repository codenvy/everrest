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
package org.everrest.core.impl;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EverrestApplicationTest {
    private EverrestApplication everrestApplication;

    @Before
    public void setUp() throws Exception {
        everrestApplication = new EverrestApplication();
    }

    @Test
    public void returnsEmptySetOfClassesNeverNull() throws Exception {
        assertTrue(everrestApplication.getClasses().isEmpty());
    }

    @Test
    public void returnsEmptySetOfSingletonsNeverNull() throws Exception {
        assertTrue(everrestApplication.getSingletons().isEmpty());
    }

    @Test
    public void addsClassesFromNewApplicationToTheBeginningOfSetOfClasses() {
        Application applicationOne = mock(Application.class);
        Application applicationTwo = mock(Application.class);
        when(applicationOne.getClasses()).thenReturn(newHashSet(ResourceOne.class));
        when(applicationTwo.getClasses()).thenReturn(newHashSet(ResourceTwo.class));

        everrestApplication.addApplication(applicationTwo);
        assertEquals(newHashSet(ResourceTwo.class), everrestApplication.getClasses());
        everrestApplication.addApplication(applicationOne);
        assertEquals(newHashSet(ResourceOne.class, ResourceTwo.class), everrestApplication.getClasses());
    }

    @Test
    public void addsSingletonsFromNewApplicationToTheBeginningOfSetOfSingletons() {
        Application applicationOne = mock(Application.class);
        Application applicationTwo = mock(Application.class);
        ResourceOne resourceOne = new ResourceOne();
        ResourceTwo resourceTwo = new ResourceTwo();
        when(applicationOne.getSingletons()).thenReturn(newHashSet(resourceOne));
        when(applicationTwo.getSingletons()).thenReturn(newHashSet(resourceTwo));

        everrestApplication.addApplication(applicationTwo);
        assertEquals(newHashSet(resourceTwo), everrestApplication.getSingletons());
        everrestApplication.addApplication(applicationOne);
        assertEquals(newHashSet(resourceOne, resourceTwo), everrestApplication.getSingletons());
    }

    @Test
    public void addsClass() {
        everrestApplication.addClass(ResourceOne.class);
        assertEquals(newHashSet(ResourceOne.class), everrestApplication.getClasses());
    }

    @Test
    public void addsSingleton() {
        ResourceOne resourceOne = new ResourceOne();
        everrestApplication.addSingleton(resourceOne);
        assertEquals(newHashSet(resourceOne), everrestApplication.getSingletons());
    }

    @Test
    public void addsFactory() {
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        everrestApplication.addFactory(objectFactory);
        assertEquals(newHashSet(objectFactory), everrestApplication.getFactories());
    }

    @Test
    public void addsPerRequestResource() {
        everrestApplication.addResource("/a", ResourceOne.class);
        assertEquals(ImmutableMap.of("/a", ResourceOne.class), everrestApplication.getResourceClasses());
    }

    @Test
    public void addsSingletonResource() {
        ResourceOne resourceOne = new ResourceOne();
        everrestApplication.addResource("/a", resourceOne);
        assertEquals(ImmutableMap.of("/a", resourceOne), everrestApplication.getResourceSingletons());
    }

    @Test
    public void addsFactoriesFromNewEverrestApplicationToTheBeginningOfSetOfFactories() {
        EverrestApplication applicationOne = mock(EverrestApplication.class);
        EverrestApplication applicationTwo = mock(EverrestApplication.class);
        ObjectFactory objectFactoryOne = mock(ObjectFactory.class);
        ObjectFactory objectFactoryTwo = mock(ObjectFactory.class);

        when(applicationOne.getFactories()).thenReturn(newHashSet(objectFactoryOne));
        when(applicationTwo.getFactories()).thenReturn(newHashSet(objectFactoryTwo));

        everrestApplication.addApplication(applicationTwo);
        assertEquals(newHashSet(objectFactoryTwo), everrestApplication.getFactories());
        everrestApplication.addApplication(applicationOne);
        assertEquals(newHashSet(objectFactoryOne, objectFactoryTwo), everrestApplication.getFactories());
    }

    @Test
    public void addsPerRequestResourcesFromNewEverrestApplicationToTheBeginningOfMapOfResources() {
        EverrestApplication applicationOne = mock(EverrestApplication.class);
        EverrestApplication applicationTwo = mock(EverrestApplication.class);
        when(applicationOne.getResourceClasses()).thenReturn(ImmutableMap.of("/a", ResourceOne.class));
        when(applicationTwo.getResourceClasses()).thenReturn(ImmutableMap.of("/b", ResourceTwo.class));

        Map<String, Class> expectedMap = new LinkedHashMap<>();

        expectedMap.put("/b", ResourceTwo.class);
        everrestApplication.addApplication(applicationTwo);
        assertEquals(expectedMap, everrestApplication.getResourceClasses());

        expectedMap.put("/a", ResourceOne.class);
        everrestApplication.addApplication(applicationOne);
        assertEquals(expectedMap, everrestApplication.getResourceClasses());
    }

    @Test
    public void addsSingletonResourcesFromNewEverrestApplicationToTheBeginningOfMapOfResources() {
        EverrestApplication applicationOne = mock(EverrestApplication.class);
        EverrestApplication applicationTwo = mock(EverrestApplication.class);
        ResourceOne resourceOne = new ResourceOne();
        ResourceTwo resourceTwo = new ResourceTwo();
        when(applicationOne.getResourceSingletons()).thenReturn(ImmutableMap.of("/a", resourceOne));
        when(applicationTwo.getResourceSingletons()).thenReturn(ImmutableMap.of("/b", resourceTwo));

        Map<String, Object> expectedMap = new LinkedHashMap<>();

        expectedMap.put("/b", resourceTwo);
        everrestApplication.addApplication(applicationTwo);
        assertEquals(expectedMap, everrestApplication.getResourceSingletons());

        expectedMap.put("/a", resourceOne);
        everrestApplication.addApplication(applicationOne);
        assertEquals(expectedMap, everrestApplication.getResourceSingletons());
    }

    public static class ResourceOne {
    }

    public static class ResourceTwo {
    }
}