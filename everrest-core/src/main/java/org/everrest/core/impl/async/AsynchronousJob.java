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
package org.everrest.core.impl.async;

import org.everrest.core.resource.ResourceMethodDescriptor;

import java.util.Map;

/**
 * @author andrew00x
 */
public interface AsynchronousJob {
    Long getJobId();

    /** Get relative URI of this asynchronous job. */
    String getJobURI();

    long getExpirationDate();

    ResourceMethodDescriptor getResourceMethod();

    boolean isDone();

    boolean cancel();

    /**
     * Get result of job. If job is not done yet this method throws IllegalStateException.
     * Before call this method caller must check is job done or not with method {@link #isDone()} .
     *
     * @return result
     * @throws IllegalStateException
     *         if job is not done yet
     */
    Object getResult() throws IllegalStateException;

    /**
     * The storage for context attributes.
     *
     * @return map never <code>null</code>
     */
    Map<String, Object> getContext();
}