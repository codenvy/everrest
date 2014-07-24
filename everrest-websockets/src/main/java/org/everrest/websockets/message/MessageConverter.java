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
package org.everrest.websockets.message;

/**
 * For convert websocket raw input message represented by String to Message and convert back Message to plain String.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface MessageConverter {
    /**
     * Convert raw String to Message.
     *
     * @param message
     *         raw message
     * @return message
     * @throws MessageConversionException
     *         if message conversion failed, e.g. if message malformed of not supported by implementation of this interface
     */
    <T extends Message> T fromString(String message, Class<T> clazz) throws MessageConversionException;

    /**
     * Convert Message to String.
     *
     * @param output
     *         output message
     * @return String that contains serialized OutputMessage
     * @throws MessageConversionException
     *         if message conversion failed
     */
    String toString(Message output) throws MessageConversionException;
}
