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
package org.everrest.websockets.client;

/**
 * Base implementation of ClientMessageListener. Implemented methods do nothing. Extend this class instead implement
 * ClientMessageListener if need just few of methods declared in ClientMessageListener.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class BaseClientMessageListener implements ClientMessageListener
{
   @Override
   public void onMessage(String data)
   {
   }

   @Override
   public void onMessage(byte[] data)
   {
   }

   @Override
   public void onOpen(WSClient client)
   {
   }

   @Override
   public void onClose(int status, String message)
   {
   }
}
