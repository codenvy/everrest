package org.everrest.core.impl;

import org.everrest.core.impl.method.filter.SecurityConstraint;

/**
 * Helper class provide method to reset state of protected fields.
 */
public class ApplicationProviderBinderHelper {
    public static void resetApplicationProviderBinder(ApplicationProviderBinder binder){

        binder.writeProviders.clear();
        binder.readProviders.clear();
        binder.exceptionMappers.clear();
        binder.contextResolvers.clear();
        binder.readProviders.clear();
        binder.responseFilters.clear();
        binder.invokerFilters.clear();
        binder.addMethodInvokerFilter(new SecurityConstraint());
    }
}
