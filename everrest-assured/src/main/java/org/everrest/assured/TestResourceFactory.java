/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.everrest.assured;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/** Get instance of the REST resource from test class in request time. */
public class TestResourceFactory implements ObjectFactory<ResourceDescriptor> {

    private static final Logger LOG = LoggerFactory.getLogger(TestResourceFactory.class);
    private final Object   testParent;
    private final Field    resourceField;
    private final AbstractResourceDescriptor resourceDescriptor;

    public TestResourceFactory(Class<?> resourceClass, Object testParent, Field resourceField) {
        this.testParent = testParent;
        this.resourceField = resourceField;
        resourceDescriptor = new AbstractResourceDescriptor(resourceClass);
    }

    /** @see org.everrest.core.ObjectFactory#getInstance(org.everrest.core.ApplicationContext) */
    @Override
    public Object getInstance(ApplicationContext context) {
        try {
            resourceField.setAccessible(true);
            return resourceField.get(testParent);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /** @see org.everrest.core.ObjectFactory#getObjectModel() */
    @Override
    public ResourceDescriptor getObjectModel() {
        return resourceDescriptor;
    }

}
