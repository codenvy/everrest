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

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonTransient;
import org.everrest.core.impl.provider.json.JsonUtils;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.impl.provider.json.JsonUtils.Types;
import org.everrest.core.provider.EntityProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.activation.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonEntityProvider.java 285 2009-10-15 16:21:30Z aparfonov $
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class JsonEntityProvider<T> implements EntityProvider<T>
{

   // It is common task for #isReadable() and #isWriteable
   // TODO Not sure it is required but ...
   // Investigation about checking can type be write as JSON (useful JSON).
   // Probably should be better added this checking in JSON framework.
   // Or probably enough check only content type 'application/json'
   // and if this content type set trust it and try parse/write

   /** Do not process via JSON "known" JAX-RS types and some other. */
   private static final Class<?>[] IGNORED =
      new Class<?>[]{byte[].class, char[].class, DataSource.class, DOMSource.class, File.class, InputStream.class,
         OutputStream.class, JAXBElement.class, MultivaluedMap.class, Reader.class, Writer.class, SAXSource.class,
         StreamingOutput.class, StreamSource.class, String.class};

   private static boolean isIgnored(Class<?> type)
   {
      if (type.getAnnotation(JsonTransient.class) != null)
      {
         return true;
      }
      for (Class<?> c : IGNORED)
      {
         if (c.isAssignableFrom(type))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      // say as support all objects, see _TODO_ above
      //return Object.class.isAssignableFrom(type);
      return !isIgnored(type);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
   {
      try
      {
         JsonParser jsonParser = new JsonParser();
         jsonParser.parse(entityStream);
         JsonValue jsonValue = jsonParser.getJsonObject();
         Types jtype = JsonUtils.getType(type);
         if (jtype == Types.ARRAY_BOOLEAN || jtype == Types.ARRAY_BYTE || jtype == Types.ARRAY_SHORT
            || jtype == Types.ARRAY_INT || jtype == Types.ARRAY_LONG || jtype == Types.ARRAY_FLOAT
            || jtype == Types.ARRAY_DOUBLE || jtype == Types.ARRAY_CHAR || jtype == Types.ARRAY_STRING
            || jtype == Types.ARRAY_OBJECT)
         {
            return (T)ObjectBuilder.createArray(type, jsonValue);
         }
         if (jtype == Types.COLLECTION)
         {
            Class c = type;
            return (T)ObjectBuilder.createCollection(c, genericType, jsonValue);
         }
         if (jtype == Types.MAP)
         {
            Class c = type;
            return (T)ObjectBuilder.createObject(c, genericType, jsonValue);
         }
         return ObjectBuilder.createObject(type, jsonValue);
      }
      catch (JsonException e)
      {
         //         e.printStackTrace();
         throw new IOException("Can't read from input stream " + e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      // say as support all objects, see _TODO_ above
      //return Object.class.isAssignableFrom(type);
      return !isIgnored(type);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      try
      {
         JsonValue jsonValue = null;
         Types jtype = JsonUtils.getType(type);
         if (jtype == Types.ARRAY_BOOLEAN || jtype == Types.ARRAY_BYTE || jtype == Types.ARRAY_SHORT
            || jtype == Types.ARRAY_INT || jtype == Types.ARRAY_LONG || jtype == Types.ARRAY_FLOAT
            || jtype == Types.ARRAY_DOUBLE || jtype == Types.ARRAY_CHAR || jtype == Types.ARRAY_STRING
            || jtype == Types.ARRAY_OBJECT)
         {
            jsonValue = JsonGenerator.createJsonArray(t);
         }
         else if (jtype == Types.COLLECTION)
         {
            jsonValue = JsonGenerator.createJsonArray((Collection<?>)t);
         }
         else if (jtype == Types.MAP)
         {
            jsonValue = JsonGenerator.createJsonObjectFromMap((Map<String, ?>)t);
         }
         else
         {
            jsonValue = JsonGenerator.createJsonObject(t);
         }

         JsonWriter jsonWriter = new JsonWriter(entityStream);
         jsonValue.writeTo(jsonWriter);
         jsonWriter.flush();
      }
      catch (JsonException e)
      {
         e.printStackTrace();
         throw new IOException("Can't write to output stream " + e);
      }
   }

}
