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
package org.everrest.ext.util;

import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS.<br/> Can add xlink:href into given Element
 * of DOM structure.<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: XlinkHref.java 301 2009-10-19 14:41:18Z aparfonov $
 */
public class XlinkHref
{

   /**
    * xlinks:href attribute.
    */
   private static final String XLINK_HREF = "xlinks:href";

   /**
    * Default namespace for xlinks:href.
    */
   private static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";

   /**
    * URI.
    */
   private String uri;

   /**
    * @param u new uri.
    */
   public XlinkHref(String u)
   {
      this.uri = u;
   }

   /**
    * Get uri.
    * 
    * @return uri current uri.
    */
   public final String getURI()
   {
      return uri;
   }

   /**
    * Add xlink to given element of DOM structure.
    * 
    * @param parent element.
    */
   public void putToElement(Element parent)
   {
      parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri);
   }

   /**
    * Add external suffix to uri an d then insert xlink into element of DOM.
    * 
    * @param parent element.
    * @param extURI external suffix for uri.
    */
   public void putToElement(Element parent, String extURI)
   {
      parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri + extURI);
   }

}
