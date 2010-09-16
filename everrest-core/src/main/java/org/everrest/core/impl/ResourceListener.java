/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.core.impl;

import org.everrest.core.ResourceBinder;
import org.everrest.core.resource.AbstractResourceDescriptor;

/**
 * Listener for adding/removing JAX-RS resources.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceListener.java 2763 2010-07-09 14:46:40Z aparfonov $
 */
public interface ResourceListener
{

   /**
    * Method will be called if new resource add in {@link ResourceBinder} .
    *
    * @param resource added resource
    */
   void resourceAdded(AbstractResourceDescriptor resource);

   /**
    * Method will be called if resource removed from {@link ResourceBinder} .
    *
    * @param resource removed resource
    */
   void resourceRemoved(AbstractResourceDescriptor resource);

}
