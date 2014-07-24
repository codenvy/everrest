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
package org.everrest.core.resource;

import org.everrest.core.ConstructorDescriptor;
import org.everrest.core.FieldInjector;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.provider.ProviderDescriptor;

/**
 * Can be used for validation next resource descriptors
 * {@link AbstractResourceDescriptor}, {@link ResourceMethodDescriptor},
 * {@link SubResourceMethodDescriptor}, {@link SubResourceLocatorDescriptor},
 * {@link ConstructorDescriptor}, {@link FieldInjector},
 * {@link ProviderDescriptor}, {@link FilterDescriptor}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceDescriptorVisitor.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public interface ResourceDescriptorVisitor {

    /**
     * @param ard
     *         See {@link AbstractResourceDescriptor}
     */
    void visitAbstractResourceDescriptor(AbstractResourceDescriptor ard);

    /**
     * @param rmd
     *         See {@link ResourceMethodDescriptor}
     */
    void visitResourceMethodDescriptor(ResourceMethodDescriptor rmd);

    /**
     * @param srmd
     *         See {@link SubResourceMethodDescriptor}
     */
    void visitSubResourceMethodDescriptor(SubResourceMethodDescriptor srmd);

    /**
     * @param srld
     *         See {@link SubResourceLocatorDescriptor}
     */
    void visitSubResourceLocatorDescriptor(SubResourceLocatorDescriptor srld);

    /**
     * @param ci
     *         ConstructorInjector
     */
    void visitConstructorInjector(ConstructorDescriptor ci);

    /**
     * @param fi
     *         FieldInjector
     */
    void visitFieldInjector(FieldInjector fi);

    /**
     * @param pd
     *         ProviderDescriptor
     */
    void visitProviderDescriptor(ProviderDescriptor pd);

    /**
     * @param fd
     *         FilterDescriptor
     */
    void visitFilterDescriptor(FilterDescriptor fd);

}
