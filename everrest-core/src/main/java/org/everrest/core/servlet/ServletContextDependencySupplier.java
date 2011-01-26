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
package org.everrest.core.servlet;

import org.everrest.core.BaseDependencySupplier;

import java.lang.annotation.Annotation;

import javax.servlet.ServletContext;

/**
 * Resolve dependency by look up instance of object in {@link ServletContext}.
 * Instance of object must be present in servlet context as attribute with name
 * which is the same as class or interface name of requested parameter, e.g.
 * instance of org.foo.bar.MyClass must be bound to attribute name
 * org.foo.bar.MyClass
 *
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ServletContextDependencySupplier.java 71 2010-10-25 15:19:37Z
 *          andrew00x $
 */
public class ServletContextDependencySupplier extends BaseDependencySupplier
{
   private final ServletContext ctx;

   public ServletContextDependencySupplier(ServletContext ctx, Class<? extends Annotation> injectAnnotation)
   {
      super(injectAnnotation);
      this.ctx = ctx;
   }

   public ServletContextDependencySupplier(ServletContext ctx)
   {
      super();
      this.ctx = ctx;
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponent(Class<?> type)
   {
      return ctx.getAttribute(type.getName());
   }
}
