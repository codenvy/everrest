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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @param <T> on of JAX-RS annotation that used for method parameters
 */
public abstract class ParameterResolver<T>
{

   /**
    * Create object which will be passed in resource method or locator. Object
    * is instance of {@link MethodParameterImpl#getParameterClass()}.
    * 
    * @param parameter See {@link org.everrest.core.Parameter}
    * @param context See {@link ApplicationContext}
    * @return newly created instance of class
    *         {@link MethodParameterImpl#getParameterClass()}
    * @throws Exception if any errors occurs
    */
   public abstract Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception;

}
