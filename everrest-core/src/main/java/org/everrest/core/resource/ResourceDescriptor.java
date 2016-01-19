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
package org.everrest.core.resource;

/**
 * Common essence for all resource descriptors.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ResourceDescriptor {

    /**
     * Method is useful for validation.
     *
     * @param visitor
     *         See {@link ResourceDescriptorVisitor}
     */
    void accept(ResourceDescriptorVisitor visitor);

}
