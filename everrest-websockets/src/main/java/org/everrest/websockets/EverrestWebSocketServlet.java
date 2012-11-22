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
package org.everrest.websockets;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.tools.DummySecurityContext;
import org.everrest.core.util.Logger;
import org.everrest.websockets.message.JsonMessageConverter;
import org.everrest.websockets.message.MessageConverter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EverrestWebSocketServlet extends WebSocketServlet
{
   public static final String EVERREST_PROCESSOR_ATTRIBUTE = EverrestProcessor.class.getName();
   public static final String PROVIDER_BINDER_ATTRIBUTE = ApplicationProviderBinder.class.getName();
   public static final String MESSAGE_CONVERTER_ATTRIBUTE = MessageConverter.class.getName();

   private static final Logger LOG = Logger.getLogger(EverrestWebSocketServlet.class);

   private EverrestProcessor processor;
   private AsynchronousJobPool asynchronousPool;
   private MessageConverter messageConverter;
   private final Collection<String> declaredRoles = new HashSet<String>();

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      processor = getEverrestProcessor();
      asynchronousPool = getAsynchronousJobPool();
      messageConverter = getMessageConverter();
      if (messageConverter == null)
      {
         messageConverter = new JsonMessageConverter();
      }
      try
      {
         declaredRoles.addAll(getApplicationDeclaredRoles());
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
      }
   }

   @Override
   protected StreamInbound createWebSocketInbound(String s, HttpServletRequest request)
   {
      Principal principal = request.getUserPrincipal();
      SecurityContext securityContext = null;
      // Workaround to get user roles.
      // It is not possible to get roles from HTTP request or at least it needs deep integration with web container.
      // First we simply read roles declared in web.xml file when this servlet deployed.
      // Now check which roles user has.
      if (principal != null)
      {
         Set<String> roles = new HashSet<String>();
         roles.addAll(declaredRoles);
         for (Iterator<String> iterator = roles.iterator(); iterator.hasNext(); )
         {
            String role = iterator.next();
            if (!request.isUserInRole(role))
            {
               iterator.remove();
            }
         }
         securityContext = new DummySecurityContext(principal, roles);
      }

      WSConnectionImpl connection = WSConnectionContext.open(request.getSession().getId(), messageConverter);
      WS2RESTAdapter restAdapter = new WS2RESTAdapter(connection, securityContext, processor, asynchronousPool);
      WSConnectionContext.registerConnectionListener(restAdapter);
      connection.registerMessageReceiver(restAdapter);
      return connection;
   }

   protected EverrestProcessor getEverrestProcessor()
   {
      return (EverrestProcessor)getServletContext().getAttribute(EVERREST_PROCESSOR_ATTRIBUTE);
   }

   protected MessageConverter getMessageConverter()
   {
      return (MessageConverter)getServletContext().getAttribute(MESSAGE_CONVERTER_ATTRIBUTE);
   }

   protected AsynchronousJobPool getAsynchronousJobPool()
   {
      ProviderBinder providers = ((ProviderBinder)getServletContext().getAttribute(PROVIDER_BINDER_ATTRIBUTE));
      if (providers != null)
      {
         ContextResolver<AsynchronousJobPool> asyncJobsResolver =
            providers.getContextResolver(AsynchronousJobPool.class, null);
         if (asyncJobsResolver != null)
         {
            return asyncJobsResolver.getContext(null);
         }
      }
      throw new IllegalStateException(
         "Unable get web socket connection. Asynchronous jobs feature is not configured properly. ");
   }

   /** Get list of roles declared for web application. */
   protected Collection<String> getApplicationDeclaredRoles()
      throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
   {
      DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
      InputStream input = getServletContext().getResourceAsStream("/WEB-INF/web.xml");
      Document dom;
      try
      {
         dom = documentBuilder.parse(input);
      }
      finally
      {
         input.close();
      }
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      NodeList all = (NodeList)xpath.evaluate("/web-app/security-role/role-name", dom, XPathConstants.NODESET);
      int length = all.getLength();
      Set<String> result = new HashSet<String>(length);
      for (int i = 0; i < length; i++)
      {
         result.add(all.item(i).getTextContent());
      }
      return result;
   }
}
