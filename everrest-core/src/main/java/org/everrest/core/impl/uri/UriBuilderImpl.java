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
package org.everrest.core.impl.uri;

import org.everrest.core.uri.UriPattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriBuilderImpl extends UriBuilder
{

   /**
    * Scheme, e.g. http, https, etc.
    */
   private String schema;

   /**
    * User info, such as user:password.
    */
   private String userInfo;

   /**
    * Host name.
    */
   private String host;

   /**
    * Server port.
    */
   private int port = -1;

   /**
    * Path.
    */
   private StringBuilder path = new StringBuilder();

   /**
    * Query string.
    */
   private StringBuilder query = new StringBuilder();

   /**
    * Fragment.
    */
   private String fragment;

   /**
    * Default constructor.
    */
   public UriBuilderImpl()
   {

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URI buildFromMap(Map<String, ? extends Object> values)
   {
      encode();
      String uri =
         UriPattern.createUriWithValues(schema, userInfo, host, port, path.toString(), query.toString(), fragment,
            values, true);
      try
      {
         return new URI(uri);
      }
      catch (URISyntaxException e)
      {
         throw new UriBuilderException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URI buildFromEncodedMap(Map<String, ? extends Object> values)
   {
      encode();
      String uri =
         UriPattern.createUriWithValues(schema, userInfo, host, port, path.toString(), query.toString(), fragment,
            values, false);
      try
      {
         return new URI(uri);
      }
      catch (URISyntaxException e)
      {
         throw new UriBuilderException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URI build(Object... values)
   {
      encode();
      String uri =
         UriPattern.createUriWithValues(schema, userInfo, host, port, path.toString(), query.toString(), fragment,
            values, true);
      try
      {
         return new URI(uri);
      }
      catch (URISyntaxException e)
      {
         throw new UriBuilderException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URI buildFromEncoded(Object... values)
   {
      encode();
      String uri =
         UriPattern.createUriWithValues(schema, userInfo, host, port, path.toString(), query.toString(), fragment,
            values, false);
      try
      {
         return new URI(uri);
      }
      catch (URISyntaxException e)
      {
         throw new UriBuilderException(e);
      }
   }

   /**
    * Encode URI path, query and fragment components.
    */
   private void encode()
   {
      // Should do this even path all segments already encoded. The reason is
      // matrix parameters that is not encoded yet. Not able encode it just
      // after adding via particular method(s) since updating/removing of it may
      // be requested later. If matrix parameters encoded just after adding then
      // original name of parameters may be changed and need play with
      // encoding/decoding again when updating/removing of matrix parameter
      // performed.
      encodePath();

      encodeQuery();
      encodeFragment();
   }

   /**
    * Encode URI path.
    */
   private void encodePath()
   {
      if (path.length() == 0)
         return;
      // We are assumes matrix parameters is parameters added to last segment
      // of URI. Check  ';' after last '/'.
      int p = path.lastIndexOf("/");
      p = path.indexOf(";", p < 0 ? 0 : p);
      // If ';' not found then not need encode since path segments itself already encoded.
      if (p < 0)
         return;
      String t = path.toString();
      path.setLength(0);
      path.append(UriComponent.recognizeEncode(t, UriComponent.PATH, true));
   }

   /**
    * Encode query parameters.
    */
   private void encodeQuery()
   {
      if (query.length() == 0)
         return;

      String str = query.toString();
      query.setLength(0);
      int p = 0;
      int n = 0;
      while (p < str.length())
      {

         if (str.charAt(p) == '=') // something like a=x&=y
            throw new UriBuilderException("Query parameter length is 0");

         n = str.indexOf('&', p);
         if (n < 0)
            n = str.length();

         if (n > p)
         {
            // skip empty pair, like a=x&&b=y
            String pair = str.substring(p, n);
            if (query.length() > 0)
               query.append('&');

            if (pair.charAt(pair.length() - 1) == '=')
               pair = pair.substring(0, pair.length() - 1);
            // decode string and keep special character '='
            query.append(UriComponent.recognizeEncode(pair, UriComponent.QUERY, true));
         }
         p = n + 1;
      }
   }

   /**
    * Encode URI fragment.
    */
   private void encodeFragment()
   {
      if (fragment == null || fragment.length() == 0)
         return;
      fragment = UriComponent.recognizeEncode(fragment, UriComponent.FRAGMENT, true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder clone()
   {
      return new UriBuilderImpl(this);
   }

   /**
    * For #clone() method.
    *
    * @param cloned current UriBuilder.
    */
   private UriBuilderImpl(UriBuilderImpl cloned)
   {
      this.schema = cloned.schema;
      this.userInfo = cloned.userInfo;
      this.host = cloned.host;
      this.port = cloned.port;
      this.path = new StringBuilder().append(cloned.path);
      this.query = new StringBuilder().append(cloned.query);
      this.fragment = cloned.fragment;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder fragment(String fragment)
   {
      if (fragment == null)
      {
         this.fragment = null;
         return this;
      }
      //this.fragment = fragment;
      this.fragment = UriComponent.encode(fragment, UriComponent.FRAGMENT, true);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder host(String host)
   {
      if (host != null)
         this.host = UriComponent.recognizeEncode(host, UriComponent.HOST, true);
      else
         this.host = null;

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder matrixParam(String name, Object... values)
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");
      if (values == null)
         throw new IllegalArgumentException("Values are null");

      if (path.length() > 0)
         path.append(';');

      int length = values.length;

      for (int i = 0; i < length; i++)
      {
         Object o = values[i];
         if (o == null)
            throw new IllegalArgumentException("Value is null");

         String value = o.toString();

         path.append(name).append('=');

         if (value.length() > 0)
            path.append(value);

         // don't add ';' after last matrix parameter
         if (i < length - 1)
            path.append(';');
      }

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder path(String p)
   {
      if (path == null)
         throw new IllegalArgumentException("Path segments are null");

      if (p.length() == 0)
         return this;

      // each segment can contains own '/' DO NOT escape it (UriComponent.PATH)
      p = UriComponent.recognizeEncode(p, UriComponent.PATH, true);

      boolean finalSlash = path.length() > 0 && path.charAt(path.length() - 1) == '/';
      boolean startSlash = p.charAt(0) == '/';

      if (finalSlash && startSlash)
      {
         if (p.length() > 1)
            path.append(p.substring(1));
      }
      else if (path.length() > 0 && !finalSlash && !startSlash)
      {
         path.append('/').append(p);
      }
      else
      {
         path.append(p);
      }

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   @Override
   public UriBuilder path(Class resource)
   {
      if (resource == null)
         throw new IllegalArgumentException("Resource is null");

      Annotation annotation = resource.getAnnotation(Path.class);
      if (annotation == null)
         throw new IllegalArgumentException("Resource is not annotated with javax.ws.rs.Path");

      return path(((Path)annotation).value());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder path(Method method)
   {
      if (method == null)
         throw new IllegalArgumentException("Methods are null");

      Path p = method.getAnnotation(Path.class);
      if (p == null)
         throw new IllegalArgumentException("Method " + method.getName() + " is not annotated with javax.ws.rs.Path");

      return path(p.value());
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   @Override
   public UriBuilder path(Class resource, String method)
   {
      if (resource == null)
         throw new IllegalArgumentException("Resource is null");

      if (method == null)
         throw new IllegalArgumentException("Method name is null");

      path(resource);
      boolean found = false;
      Method[] methods = resource.getMethods();

      for (Method m : methods)
      {
         if (found && m.getName().equals(method))
         {
            throw new IllegalArgumentException("More then one method with name " + method + " found");
         }
         else if (m.getName().equals(method))
         {
            path(m);
            found = true;
         }
      }

      // no one method found
      if (!found)
         throw new IllegalArgumentException("Method " + method + " not found at resource class " + resource.getName());

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder port(int port)
   {
      if (port < -1)
         throw new IllegalArgumentException();
      this.port = port;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder queryParam(String name, Object... values)
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");
      if (values == null)
         throw new IllegalArgumentException("Values are null");

      if (query.length() > 0)
         query.append('&');

      int length = values.length;

      // add values
      for (int i = 0; i < length; i++)
      {
         Object o = values[i];
         if (o == null)
            throw new IllegalArgumentException("Value is null");

         String s = o.toString();

         query.append(name).append('=');

         if (s.length() > 0)
            query.append(s);

         // don't add '&' after last query pair
         if (i < length - 1)
            query.append('&');
      }

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder replaceMatrixParam(String name, Object... values)
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      if (path.length() > 0)
      {
         int p = path.lastIndexOf("/");

         // slash not found , then start search for ; from begin of string
         if (p == -1)
            p = 0;

         p = path.indexOf(";", p); // <<<<<<< start point for processing

         while ((p = path.indexOf(name, p)) > 0)
         {
            int n = path.indexOf(";", p);

            if (n == -1)
               n = path.length();

            p = p > 0 ? p - 1 : 0; // p decrements, because want remove previous ';'
            path.replace(p, n, "");
         }
      }

      if (values != null && values.length > 0)
         matrixParam(name, values);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder replaceMatrix(String matrix)
   {
      // search ';' which goes after '/' at final path segment
      if (path.length() > 0)
      {
         int p = path.lastIndexOf("/");

         // slash not found , then start search for ; from begin of string
         if (p == -1)
            p = 0;

         p = path.indexOf(";", p);
         path.setLength(p + 1);
      }

      // if have values add it
      if (matrix != null && matrix.length() > 0)
         path.append(matrix);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder replacePath(String p)
   {
      path.setLength(0);
      path(p);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder replaceQueryParam(String name, Object... values)
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      // first remove old parameters, they can go not one by one,
      // try found all of it
      int p = 0;
      while ((p = query.indexOf(name, p)) >= 0)
      {
         int n = query.indexOf("&", p);

         if (n < 0)
            n = query.length();

         p = p > 0 ? p - 1 : 0; // want remove previous '&' if it exists
         query.replace(p, n, "");

         // remove first '&' if presents
         if (query.charAt(0) == '&')
            query.deleteCharAt(0);
      }

      // now add new one
      if (values != null && values.length > 0)
         queryParam(name, values);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder replaceQuery(String queryString)
   {
      query.setLength(0);

      if (queryString != null && queryString.length() > 0)
         query.append(queryString);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder scheme(String schema)
   {
      if (schema != null)
         this.schema = UriComponent.validate(schema, UriComponent.SCHEME, true);
      else
         this.schema = null;

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder schemeSpecificPart(String ssp)
   {
      if (ssp == null)
         throw new IllegalArgumentException("Scheme specific part (ssp) is null");

      StringBuilder sb = new StringBuilder();

      if (schema != null)
         sb.append(schema).append(':').append(UriComponent.recognizeEncode(ssp, UriComponent.SSP, true));

      if (fragment != null && fragment.length() > 0)
         sb.append('#').append(fragment);

      URI uri;
      try
      {
         uri = new URI(sb.toString());
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException(e);
      }

      userInfo = uri.getRawUserInfo();
      host = uri.getHost();
      port = uri.getPort();
      path.setLength(0);
      path.append(uri.getRawPath());
      query.setLength(0);
      query.append(uri.getRawQuery() != null ? uri.getRawQuery() : "");

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder segment(String... segments)
   {
      if (segments == null)
         throw new IllegalArgumentException("Path segments is null");

      for (String p : segments)
         path(p);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder uri(URI uri)
   {
      if (uri == null)
         throw new IllegalArgumentException("URI is null");

      if (uri.getScheme() != null)
         schema = uri.getScheme();

      if (uri.getRawUserInfo() != null)
         userInfo = uri.getRawUserInfo();

      if (uri.getHost() != null)
         host = uri.getHost();

      if (uri.getPort() != -1)
         port = uri.getPort();

      if (uri.getRawPath() != null && uri.getRawPath().length() > 0)
      {
         path.setLength(0);
         path.append(uri.getRawPath());
      }

      if (uri.getRawQuery() != null && uri.getRawQuery().length() > 0)
      {
         query.setLength(0);
         query.append(uri.getRawQuery());
      }

      if (uri.getRawFragment() != null)
         fragment = uri.getRawFragment();

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UriBuilder userInfo(String userInfo)
   {
      if (userInfo != null)
         this.userInfo = UriComponent.recognizeEncode(userInfo, UriComponent.USER_INFO, true);
      else
         this.userInfo = null;

      return this;
   }

}
