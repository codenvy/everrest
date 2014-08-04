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
package org.everrest.exoplatform;

import org.everrest.core.BaseDependencySupplier;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.ComponentAdapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

/**
 * Get instance of requested type from ExoContainer.
 *
 * @author andrew00x
 */
public class ExoDependencySupplier extends BaseDependencySupplier {
    @Override
    public javax.inject.Provider<?> getProvider(Type providerType) {
        if (!(providerType instanceof ParameterizedType)) {
            throw new RuntimeException("Cannot inject provider without type parameter. ");
        }
        Type actualType = ((ParameterizedType)providerType).getActualTypeArguments()[0];
        return getProvider(ExoContainerContext.getCurrentContainer(), actualType);
    }

    @SuppressWarnings({"rawtypes"})
    private javax.inject.Provider<?> getProvider(final ExoContainer container, final Type entryType) {
        List injectionProviders = container.getComponentInstancesOfType(javax.inject.Provider.class);
        if (injectionProviders != null && !injectionProviders.isEmpty()) {
            for (Iterator i = injectionProviders.iterator(); i.hasNext(); ) {
                javax.inject.Provider provider = (javax.inject.Provider)i.next();
                try {
                    Type injectedType = provider.getClass().getMethod("get").getGenericReturnType();
                    if (entryType.equals(injectedType)) {
                        return provider;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Never happen since class implements javax.inject.Provider.
                }
            }
        }

        // Create javax.inject.Provider if instance of requested type may be produced by ExoContainer.
        if (entryType instanceof Class<?>) {
            final ComponentAdapter componentAdapter = container.getComponentAdapterOfType((Class<?>)entryType);
            if (componentAdapter != null) {
                return new javax.inject.Provider<Object>() {
                    @Override
                    public Object get() {
                        return componentAdapter.getComponentInstance(container);
                    }
                };
            }
        }

        return null;
    }

    @Override
    public Object getComponent(Class<?> type) {
        javax.inject.Provider<?> provider = getProvider(ExoContainerContext.getCurrentContainer(), type);
        if (provider != null) {
            return provider.get();
        }
        return null;
    }

    @Override
    public Object getComponentByName(String name) {
        return ExoContainerContext.getCurrentContainer().getComponentInstance(name);
    }
}
