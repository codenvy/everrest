/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core;

/**
 * Should not be used by custom services. They have to use {@link javax.ws.rs.WebApplicationException} instead. UnhandledException is
 * used to propagate exception than can't be handled by this framework to top container (e.g. Servlet Container)
 *
 * @author andrew00x
 */
public class UnhandledException extends RuntimeException {
    private int responseStatus;

    public UnhandledException(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * @param throwable
     *         cause
     */
    public UnhandledException(Throwable throwable) {
        super(throwable);
    }

    public int getResponseStatus() {
        return responseStatus;
    }
}
