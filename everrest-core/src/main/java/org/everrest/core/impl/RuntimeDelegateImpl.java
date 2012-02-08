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
package org.everrest.core.impl;

import org.everrest.core.header.AbstractHeaderDelegate;
import org.everrest.core.impl.header.AcceptLanguageHeaderDelegate;
import org.everrest.core.impl.header.AcceptMediaTypeHeaderDelegate;
import org.everrest.core.impl.header.CacheControlHeaderDelegate;
import org.everrest.core.impl.header.CookieHeaderDelegate;
import org.everrest.core.impl.header.DateHeaderDelegate;
import org.everrest.core.impl.header.EntityTagHeaderDelegate;
import org.everrest.core.impl.header.LocaleHeaderDelegate;
import org.everrest.core.impl.header.MediaTypeHeaderDelegate;
import org.everrest.core.impl.header.NewCookieHeaderDelegate;
import org.everrest.core.impl.header.RangeHeaderDelegate;
import org.everrest.core.impl.header.StringHeaderDelegate;
import org.everrest.core.impl.header.URIHeaderDelegate;
import org.everrest.core.impl.uri.UriBuilderImpl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RuntimeDelegateImpl extends RuntimeDelegate
{
   /** HeaderDelegate cache. */
   @SuppressWarnings("rawtypes")
   private final Map<Class<?>, HeaderDelegate> headerDelegates = new HashMap<Class<?>, HeaderDelegate>();

   /**
    * Should be used only once for initialize.
    *
    * @see RuntimeDelegate#setInstance(RuntimeDelegate)
    * @see RuntimeDelegate#getInstance()
    */
   public RuntimeDelegateImpl()
   {
      init();
   }

   private void init()
   {
      // JSR-311
      addHeaderDelegate(new MediaTypeHeaderDelegate());
      addHeaderDelegate(new CacheControlHeaderDelegate());
      addHeaderDelegate(new CookieHeaderDelegate());
      addHeaderDelegate(new NewCookieHeaderDelegate());
      addHeaderDelegate(new EntityTagHeaderDelegate());
      addHeaderDelegate(new DateHeaderDelegate());
      // external
      addHeaderDelegate(new AcceptLanguageHeaderDelegate());
      addHeaderDelegate(new AcceptMediaTypeHeaderDelegate());
      addHeaderDelegate(new StringHeaderDelegate());
      addHeaderDelegate(new URIHeaderDelegate());
      addHeaderDelegate(new LocaleHeaderDelegate());
      addHeaderDelegate(new RangeHeaderDelegate());
   }

   public void addHeaderDelegate(AbstractHeaderDelegate<?> header)
   {
      headerDelegates.put(header.support(), header);
   }

   /** End Points is not supported. {@inheritDoc} */
   @Override
   public <T> T createEndpoint(Application applicationConfig, Class<T> type)
   {
      throw new UnsupportedOperationException("End Points is not supported");
   }

   /** {@inheritDoc} */
   @SuppressWarnings("unchecked")
   @Override
   public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
   {
      return headerDelegates.get(type);
   }

   /** {@inheritDoc} */
   @Override
   public ResponseBuilder createResponseBuilder()
   {
      return new ResponseImpl.ResponseBuilderImpl();
   }

   /** {@inheritDoc} */
   @Override
   public UriBuilder createUriBuilder()
   {
      return new UriBuilderImpl();
   }

   /** {@inheritDoc} */
   @Override
   public VariantListBuilder createVariantListBuilder()
   {
      return new VariantListBuilderImpl();
   }
}
