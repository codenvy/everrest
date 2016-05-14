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
package org.everrest.websockets.message;

/**
 * Output message.
 *
 * @author andrew00x
 */
public class OutputMessage extends Message {
    private int responseCode;

    /**
     * Get response code.
     *
     * @return response code.
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Get response code.
     *
     * @param responseCode
     *         response code.
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
