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
package org.everrest.core.provider;

import org.everrest.core.ObjectModel;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Descriptor of Provider. Provider is annotated with &#64;Provider and implement interface defined by JAX-RS.
 *
 * @author andrew00x
 */
public interface ProviderDescriptor extends ObjectModel {

    /**
     * Get list of {@link MediaType} which current provider consumes.
     *
     * @return list of media types
     */
    List<MediaType> consumes();

    /**
     * Get list of {@link MediaType} which current provider produces.
     *
     * @return list of media types
     */
    List<MediaType> produces();

}
