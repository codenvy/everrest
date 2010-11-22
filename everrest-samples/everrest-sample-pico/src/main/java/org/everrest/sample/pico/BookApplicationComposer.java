package org.everrest.sample.pico;

import org.everrest.pico.EverrestComposer;
import org.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookApplicationComposer extends EverrestComposer
{
   @Override
   protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext)
   {
      container.addComponent(BookNotFoundExceptionMapper.class);
      container.addComponent(BookStorage.class);
   }

   @Override
   protected void doComposeRequest(MutablePicoContainer container)
   {
      container.addComponent(BookService.class);
   }

   @Override
   protected void doComposeSession(MutablePicoContainer container)
   {
   }
}
