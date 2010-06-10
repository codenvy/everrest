/*
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
package org.everrest.ext.proxy;

import org.everrest.common.http.client.HTTPResponse;
import org.everrest.common.http.client.ModuleException;
import org.everrest.common.http.client.ParseException;
import org.everrest.common.http.client.ProtocolNotSuppException;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: Connector.java 1886 2010-02-18 10:34:09Z max_shaposhnik $
 */
public abstract class Connector
{

   /** The connect timeout. */
   protected static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;

   /**
    * Do GET proxy request.
    * 
    * @param httpRequest the HttpServletRequest
    * @param url the url to request
    * @return response HTTPResponse
    */
   abstract HTTPResponse fetchGet(HttpServletRequest httpRequest, String url) throws MalformedURLException,
      ProtocolNotSuppException, IOException, ModuleException, ParseException;

   /**
    * Do POST proxy request.
    * 
    * @param httpRequest the HttpServletRequest
    * @param url the url to request
    * @return response HTTPResponse
    */
   abstract HTTPResponse fetchPost(HttpServletRequest httpRequest, String url) throws MalformedURLException,
      ProtocolNotSuppException, IOException, ModuleException, ParseException;

   /**
    * Do PUT proxy request.
    * 
    * @param httpRequest the HttpServletRequest
    * @param url the url to request
    * @return response HTTPResponse
    */
   abstract HTTPResponse doPut(HttpServletRequest httpRequest, String url) throws MalformedURLException,
      ProtocolNotSuppException, IOException, ModuleException, ParseException;

   /**
    * Do DELETE proxy request.
    * 
    * @param httpRequest the HttpServletRequest
    * @param url the url to request
    * @return response HTTPResponse
    */
   abstract HTTPResponse doDelete(HttpServletRequest httpRequest, String url) throws MalformedURLException,
      ProtocolNotSuppException, IOException, ModuleException, ParseException;

}
