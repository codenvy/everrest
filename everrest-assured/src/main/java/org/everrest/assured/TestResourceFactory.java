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
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/** Get instance of the REST resource from test class in request time. */
public class TestResourceFactory implements ObjectFactory<AbstractResourceDescriptor> {

    private static final Logger LOG = LoggerFactory.getLogger(TestResourceFactory.class);
    private final Object                         testParent;
    private final Field                          resourceField;
    private final AbstractResourceDescriptorImpl model;
    //private final Class<?> resourceClass;

    public TestResourceFactory(Object testParent, Field resourceField) {
        this.testParent = testParent;
        this.resourceField = resourceField;
        this.resourceField.setAccessible(true);
        this.model = new AbstractResourceDescriptorImpl(resourceField.getType());
    }

    /** @see org.everrest.core.ObjectFactory#getInstance(org.everrest.core.ApplicationContext) */
    @Override
    public Object getInstance(ApplicationContext context) {
        try {

            Object object = resourceField.get(testParent);
            if (object != null) {
                ProviderDescriptor descriptor = new ProviderDescriptorImpl(object);
                descriptor.accept(ResourceDescriptorValidator.getInstance());
                return new SingletonObjectFactory<>(descriptor, object).getInstance(context);
            } else {
                ProviderDescriptor descriptor = new ProviderDescriptorImpl(resourceField.getType());
                descriptor.accept(ResourceDescriptorValidator.getInstance());
                return new PerRequestObjectFactory<>(descriptor).getInstance(context);
            }

        } catch (IllegalArgumentException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /** @see org.everrest.core.ObjectFactory#getObjectModel() */
    @Override
    public AbstractResourceDescriptor getObjectModel() {
        return new AbstractResourceDescriptorImpl(resourceField.getType());
    }

}
