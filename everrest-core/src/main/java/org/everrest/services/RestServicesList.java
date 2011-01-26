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
package org.everrest.services;

import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.resource.AbstractResourceDescriptor;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
@Path("/")
public class RestServicesList
{

   //
   public static class RootResource
   {
      private String fqn;

      private String path;

      private String regex;

      public RootResource(String fqn, String path, String regex)
      {
         this.fqn = fqn;
         this.path = path;
         this.regex = regex;
      }

      public String getFqn()
      {
         return fqn;
      }

      public String getPath()
      {
         return path;
      }

      public String getRegex()
      {
         return regex;
      }
   }

   //

   public static class RootResourcesList
   {
      private List<RootResource> rootResources;

      public RootResourcesList(List<RootResource> rootResources)
      {
         this.rootResources = rootResources;
      }

      public List<RootResource> getRootResources()
      {
         return rootResources;
      }
   }

   //

   private final ResourceBinder binder;

   public RestServicesList(ResourceBinder resources)
   {
      this.binder = resources;
   }

   @GET
   @Produces({MediaType.TEXT_HTML})
   public byte[] listHTML()
   {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try
      {
         XMLStreamWriter xsw = factory.createXMLStreamWriter(output, "UTF-8");
         xsw.writeStartDocument("UTF-8", "1.0");
         xsw.writeDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
         xsw.writeCharacters("\n");
         xsw.writeStartElement("html");
         xsw.writeDefaultNamespace("http://www.w3.org/1999/xhtml");
         xsw.writeStartElement("head");
         xsw.writeStartElement("title");
         xsw.writeCharacters("eXo JAXRS Implementation");
         xsw.writeEndElement(); // </title>
         xsw.writeEndElement(); // </head>
         xsw.writeStartElement("body");
         //
         xsw.writeStartElement("h3");
         xsw.writeAttribute("style", "text-align:center;");
         xsw.writeCharacters("Root resources");
         xsw.writeEndElement();
         // table
         xsw.writeStartElement("table");
         xsw.writeAttribute("width", "90%");
         xsw.writeAttribute("style", "table-layout:fixed;");
         // table header
         xsw.writeStartElement("tr");
         xsw.writeStartElement("th");
         xsw.writeCharacters("Path");
         xsw.writeEndElement(); // </th>
         xsw.writeStartElement("th");
         xsw.writeCharacters("Regex");
         xsw.writeEndElement(); // </th>
         xsw.writeStartElement("th");
         xsw.writeCharacters("FQN");
         xsw.writeEndElement(); // </th>
         xsw.writeEndElement(); // </tr>
         // end table header
         for (RootResource r : rootResources().getRootResources())
         {
            xsw.writeStartElement("tr");
            xsw.writeStartElement("td");
            xsw.writeCharacters(r.getPath());
            xsw.writeEndElement(); // </td>
            xsw.writeStartElement("td");
            xsw.writeCharacters(r.getRegex());
            xsw.writeEndElement(); // </td>
            xsw.writeStartElement("td");
            xsw.writeCharacters(r.getFqn());
            xsw.writeEndElement(); // </td>
            xsw.writeEndElement(); // </tr>
         }
         xsw.writeEndElement(); // </table>
         xsw.writeEndElement(); // </body>
         xsw.writeEndDocument();
      }
      catch (XMLStreamException xmle)
      {
         throw new WebApplicationException(xmle, //
            Response.status(500) //
               .entity("Unable write to output stream. " + xmle.getMessage()) //
               .type(MediaType.TEXT_PLAIN) //
               .build());
      }
      return output.toByteArray();
   }

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public RootResourcesList listJSON()
   {
      return rootResources();
   }

   protected RootResourcesList rootResources()
   {
      List<RootResource> resources = new ArrayList<RootResource>();
      for (ObjectFactory<AbstractResourceDescriptor> om : binder.getResources())
      {
         AbstractResourceDescriptor descriptor = om.getObjectModel();
         resources.add(new RootResource(descriptor.getObjectClass().getName(), //
            descriptor.getPathValue().getPath(), //
            descriptor.getUriPattern().getRegex()));
      }
      return new RootResourcesList(resources);
   }

}
