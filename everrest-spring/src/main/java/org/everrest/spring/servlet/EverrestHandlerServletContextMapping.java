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

package org.everrest.spring.servlet;

import org.everrest.core.impl.EverrestProcessor;
import org.everrest.spring.EverrestHandlerMapping;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * HandlerMapping for EverrestProcessor which obtains instance of
 * <code>EverrestProcessor</code> from ServletContext.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestHandlerServletContextMapping extends EverrestHandlerMapping implements ServletContextAware
{

   private ServletContext servletContext;

   public void setServletContext(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }

   @Override
   protected EverrestProcessor getProcessor()
   {
      if (processor == null)
      {
         processor = (EverrestProcessor)servletContext.getAttribute(EverrestProcessor.class.getName());
      }
      return processor;
   }

}
