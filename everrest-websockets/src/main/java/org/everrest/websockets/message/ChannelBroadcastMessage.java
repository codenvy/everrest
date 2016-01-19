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
 * This class intended for sending broadcast messages for all clients subscribed to the channel.
 *
 * @author andrew00x
 * @see org.everrest.websockets.WSConnection#subscribeToChannel(String)
 * @see org.everrest.websockets.WSConnection#unsubscribeFromChannel(String)
 * @see org.everrest.websockets.WSConnection#getChannels()
 * @see org.everrest.websockets.WSConnectionContext#sendMessage(ChannelBroadcastMessage)
 */
public class ChannelBroadcastMessage extends Message {
    public enum Type {
        ERROR("error"),
        NONE("none");

        private final String value;

        private Type(String value) {
            this.value = value;
        }

        @Override
        public final String toString() {
            return value;
        }
    }

    private String channel;
    private Type type = Type.NONE;

    /**
     * Get name of channel to send request.
     *
     * @return name of channel to send request.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Set name of channel to send request.
     *
     * @param channel
     *         name of channel to send request.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Get message type. All messages by default have type 'none' if different type is not set by method {@link
     * #setType(org.everrest.websockets.message.ChannelBroadcastMessage.Type)}.
     *
     * @return message type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set message type.
     *
     * @param type
     *         message type
     * @see #getType()
     */
    public void setType(Type type) {
        this.type = type;
    }
}
