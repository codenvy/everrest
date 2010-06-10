/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core;

/**
 * Implementation of DependencySupplier should be able to provide objects that
 * required for constructors or fields of Resource or Provider.
 * 
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: DependencySupplier.java -1 $
 */
public interface DependencySupplier
{

   /**
    * Get object that is approach do description <code>parameter</code>.
    * 
    * @param parameter required parameter description
    * @return object of required type or null if instance described by
    *         <code>parameter</code> may not be produced
    */
   Object getComponent(Parameter parameter);

}
