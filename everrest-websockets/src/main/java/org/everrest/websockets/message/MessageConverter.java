/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
