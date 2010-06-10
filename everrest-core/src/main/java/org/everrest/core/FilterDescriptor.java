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

import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.uri.UriPattern;

/**
 * Description of filter.
 * 
 * @see Filter
 * @see RequestFilter
 * @see ResponseFilter
 * @see MethodInvokerFilter
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: FilterDescriptor.java -1 $
 */
public interface FilterDescriptor extends ResourceDescriptor, ObjectModel
{

   /**
    * @return See {@link PathValue}
    */
   PathValue getPathValue();

   /**
    * UriPattern build in same manner as for resources. For detail see section
    * 3.4 URI Templates in JAX-RS specification.
    * 
    * @return See {@link UriPattern}
    */
   UriPattern getUriPattern();

}
