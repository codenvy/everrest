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
package org.everrest.ext.provider;

import org.everrest.common.util.HierarchicalProperty;
import org.everrest.core.provider.EntityProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Stack;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Converts stream to {@link HierarchicalProperty} and serializes
 * {@link HierarchicalProperty} in given stream.
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: HierarchicalPropertyEntityProvider.java
 */
@Provider
public class HierarchicalPropertyEntityProvider implements EntityProvider<HierarchicalProperty>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(HierarchicalPropertyEntityProvider.class.getName());

   /**
    * {@inheritDoc}
    */
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return type == HierarchicalProperty.class;
   }

   /**
    * {@inheritDoc}
    */
   public HierarchicalProperty readFrom(Class<HierarchicalProperty> t, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
      WebApplicationException
   {
      HierarchicalProperty rootProperty = null;
      Stack<HierarchicalProperty> curProperty = new Stack<HierarchicalProperty>();

      try
      {
         XMLInputFactory factory = XMLInputFactory.newInstance();
         XMLEventReader reader = factory.createXMLEventReader(entityStream);
         while (reader.hasNext())
         {
            XMLEvent event = reader.nextEvent();
            switch (event.getEventType())
            {
               case XMLEvent.START_ELEMENT :
                  StartElement element = event.asStartElement();
                  QName name = element.getName();
                  HierarchicalProperty prop = new HierarchicalProperty(name);
                  if (!curProperty.empty())
                     curProperty.peek().addChild(prop);
                  else
                     rootProperty = prop;
                  curProperty.push(prop);
                  break;
               case XMLEvent.END_ELEMENT :
                  curProperty.pop();
                  break;
               case XMLEvent.CHARACTERS :
                  String chars = event.asCharacters().getData();
                  curProperty.peek().setValue(chars);
                  break;
               default :
                  break;
            }
         }

         return rootProperty;
      }
      catch (FactoryConfigurationError e)
      {
         throw new IOException(e.getMessage());
      }
      catch (XMLStreamException e)
      {
         if (LOG.isDebugEnabled())
            e.printStackTrace();
         return null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getSize(HierarchicalProperty t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType)
   {

      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return HierarchicalProperty.class.isAssignableFrom(type);
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(HierarchicalProperty t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
      WebApplicationException
   {

      Document e = (Document)t;
      try
      {
         TransformerFactory.newInstance().newTransformer().transform(new DOMSource(e), new StreamResult(entityStream));
      }
      catch (TransformerException tre)
      {
         throw new IOException("Can't write to output stream " + tre);
      }
   }

}
