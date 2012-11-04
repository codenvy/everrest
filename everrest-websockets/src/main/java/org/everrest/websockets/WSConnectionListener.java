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
package org.everrest.websockets;

/**
 * Notified when WSConnection opened and closed. Implementation of this interface should be registered in
 * WSConnectionContext context.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * @see WSConnectionContext#registerConnectionListener(WSConnectionListener)
 * @see WSConnectionContext#removeConnectionListener(WSConnectionListener)
 */
public interface WSConnectionListener
{
   /**
    * Called when new connection opened.
    *
    * @param connection
    *    new connection
    */
   void onOpen(WSConnection connection);

   /**
    * Called when connection closed.
    *
    * @param connectionId
    *    connection identifier
    * @param code
    *    code with represent the connection close status
    * @see org.everrest.websockets.WSConnection#getConnectionId()
    */
   void onClose(Long connectionId, int code);
}
