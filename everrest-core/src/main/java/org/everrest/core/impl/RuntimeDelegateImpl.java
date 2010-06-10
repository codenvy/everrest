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

import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptLanguageHeaderDelegate;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.AcceptMediaTypeHeaderDelegate;
import org.everrest.core.impl.header.CacheControlHeaderDelegate;
import org.everrest.core.impl.header.CookieHeaderDelegate;
import org.everrest.core.impl.header.DateHeaderDelegate;
import org.everrest.core.impl.header.EntityTagHeaderDelegate;
import org.everrest.core.impl.header.LocaleHeaderDelegate;
import org.everrest.core.impl.header.MediaTypeHeaderDelegate;
import org.everrest.core.impl.header.NewCookieHeaderDelegate;
import org.everrest.core.impl.header.StringHeaderDelegate;
import org.everrest.core.impl.header.URIHeaderDelegate;
import org.everrest.core.impl.uri.UriBuilderImpl;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: RuntimeDelegateImpl.java -1 $
 */
public class RuntimeDelegateImpl extends RuntimeDelegate
{

   /**
    * HeaderDelegate cache.
    */
   @SuppressWarnings("unchecked")
   private final Map<Class<?>, HeaderDelegate> headerDelegates = new HashMap<Class<?>, HeaderDelegate>();

   /**
    * Should be used only once for initialize.
    * 
    * @see RuntimeDelegate#setInstance(RuntimeDelegate)
    * @see RuntimeDelegate#getInstance()
    */
   public RuntimeDelegateImpl()
   {
      // JSR-311
      headerDelegates.put(MediaType.class, new MediaTypeHeaderDelegate());
      headerDelegates.put(CacheControl.class, new CacheControlHeaderDelegate());
      headerDelegates.put(Cookie.class, new CookieHeaderDelegate());
      headerDelegates.put(NewCookie.class, new NewCookieHeaderDelegate());
      headerDelegates.put(EntityTag.class, new EntityTagHeaderDelegate());
      headerDelegates.put(Date.class, new DateHeaderDelegate());
      // external
      headerDelegates.put(AcceptLanguage.class, new AcceptLanguageHeaderDelegate());
      headerDelegates.put(AcceptMediaType.class, new AcceptMediaTypeHeaderDelegate());
      headerDelegates.put(String.class, new StringHeaderDelegate());
      headerDelegates.put(URI.class, new URIHeaderDelegate());
      headerDelegates.put(Locale.class, new LocaleHeaderDelegate());
   }

   /**
    * End Points is not supported. {@inheritDoc}
    */
   @Override
   public <T> T createEndpoint(Application applicationConfig, Class<T> type)
   {
      throw new UnsupportedOperationException("End Points is not supported");
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
   {
      // TODO mechanism for use external HeaderDelegate
      return (HeaderDelegate<T>)headerDelegates.get(type);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ResponseBuilder createResponseBuilder()
   {
      return new ResponseImpl.ResponseBuilderImpl();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder createUriBuilder()
   {
      return new UriBuilderImpl();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VariantListBuilder createVariantListBuilder()
   {
      return new VariantListBuilderImpl();
   }

}
