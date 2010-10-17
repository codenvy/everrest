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
package org.everrest.core.impl.provider;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Provide cache for {@link JAXBContext}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JAXBContextResolver.java 285 2009-10-15 16:21:30Z aparfonov $
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBContextResolver implements ContextResolver<JAXBContextResolver>
{

   /** JAXBContext cache. */
   @SuppressWarnings("unchecked")
   private final ConcurrentHashMap<Class, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class, JAXBContext>();

   /**
    * {@inheritDoc}
    */
   public JAXBContextResolver getContext(Class<?> type)
   {
      return this;
   }

   /**
    * Return JAXBContext according to supplied type. If no one context found
    * then try create new context and save it in cache.
    *
    * @param classes classes to be bound
    * @return JAXBContext
    * @throws JAXBException if JAXBContext creation failed
    */
   public JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException
   {
      JAXBContext jaxbctx = jaxbContexts.get(clazz);
      if (jaxbctx == null)
      {
         jaxbctx = JAXBContext.newInstance(clazz);
         jaxbContexts.put(clazz, jaxbctx);
      }
      return jaxbctx;
   }

   /**
    * Create and add in cache JAXBContext for supplied set of classes.
    *
    * @param classes set of java classes to be bound
    * @return JAXBContext
    * @throws JAXBException if JAXBContext for supplied classes can't be created
    *         in any reasons
    */
   public JAXBContext createJAXBContext(Class<?> clazz) throws JAXBException
   {
      JAXBContext jaxbctx = JAXBContext.newInstance(clazz);
      addJAXBContext(jaxbctx, clazz);
      return jaxbctx;
   }

   /**
    * Add prepared JAXBContext that will be mapped to set of class. In this case
    * this class works as cache for JAXBContexts.
    *
    * @param jaxbctx JAXBContext
    * @param classes set of java classes to be bound
    */
   public void addJAXBContext(JAXBContext jaxbctx, Class<?> clazz)
   {
      jaxbContexts.put(clazz, jaxbctx);
   }

}
