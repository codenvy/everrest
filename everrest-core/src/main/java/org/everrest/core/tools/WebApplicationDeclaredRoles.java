/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.everrest.core.tools;

import org.everrest.core.UnhandledException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Describes roles declared for web application in web.xml file.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WebApplicationDeclaredRoles
{
   private final Set<String> declaredRoles;

   public WebApplicationDeclaredRoles(ServletContext servletContext)
   {
      declaredRoles = Collections.unmodifiableSet(loadRoles(servletContext));
   }

   protected Set<String> loadRoles(ServletContext servletContext) throws UnhandledException
   {
      InputStream input = servletContext.getResourceAsStream("/WEB-INF/web.xml");
      if (input == null)
      {
         return Collections.emptySet();
      }
      try
      {
         DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document dom = documentBuilder.parse(input);
         XPathFactory xpathFactory = XPathFactory.newInstance();
         XPath xpath = xpathFactory.newXPath();
         NodeList all = (NodeList)xpath.evaluate("/web-app/security-role/role-name", dom, XPathConstants.NODESET);
         int length = all.getLength();
         Set<String> roles = new LinkedHashSet<String>(length);
         for (int i = 0; i < length; i++)
         {
            roles.add(all.item(i).getTextContent());
         }
         return roles;
      }
      catch (ParserConfigurationException e)
      {
         throw new UnhandledException(e);
      }
      catch (SAXException e)
      {
         throw new UnhandledException(e);
      }
      catch (XPathExpressionException e)
      {
         throw new UnhandledException(e);
      }
      catch (IOException e)
      {
         throw new UnhandledException(e);
      }
      finally
      {
         try
         {
            input.close();
         }
         catch (IOException ignored)
         {
         }
      }
   }

   public Set<String> getDeclaredRoles()
   {
      return declaredRoles;
   }
}
