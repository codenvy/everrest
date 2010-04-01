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
package org.everrest.common.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.namespace.QName;

/**
 * Created by The eXo Platform SAS .<br/> DOM - like (but lighter) property
 * representation
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class HierarchicalProperty
{

   /**
    * Child properties.
    */
   protected List<HierarchicalProperty> children;

   /**
    * Property name. 
    */
   protected QName name;

   /**
    * Property value.
    */
   protected String value;

   protected HashMap<String, String> attributes = new HashMap<String, String>();

   /**
    * Constructor accepting String as property name, both prefixed (i.e.
    * prefix:local) and not (i.e. local) are accepted
    * 
    * @param name
    *          property name
    * @param value
    *          property value (can be null)
    */
   public HierarchicalProperty(String name, String value, String namespaceURI)
   {
      String[] tmp = name.split(":");
      if (tmp.length > 1)
      {
         this.name = new QName(namespaceURI, tmp[1], tmp[0]);
      }
      else
      {
         if (namespaceURI == null)
            this.name = new QName(tmp[0]);
         else
            this.name = new QName(namespaceURI, tmp[0]);
      }
      this.value = value;
      this.children = new ArrayList<HierarchicalProperty>();
   }

   /**
    * @param name
    * @param value
    */
   public HierarchicalProperty(QName name, String value)
   {
      this.name = name;
      this.value = value;
      this.children = new ArrayList<HierarchicalProperty>();
   }

   /**
    * @param name
    * @param dateValue
    * @param formatPattern
    */
   public HierarchicalProperty(QName name, Calendar dateValue, String formatPattern)
   {
      this(name, null);
      SimpleDateFormat dateFormat = new SimpleDateFormat(formatPattern, Locale.ENGLISH);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      this.value = dateFormat.format(dateValue.getTime());
   }

   /**
    * Shortcut for XMLProperty(name, null).
    * 
    * @param name
    */
   public HierarchicalProperty(QName name)
   {
      this(name, null);
   }

   /**
    * Adds prop as a children to this property.
    * 
    * @param prop
    * @return added property
    */
   public HierarchicalProperty addChild(HierarchicalProperty prop)
   {
      children.add(prop);
      return prop;
   }

   /**
    * @return child properties of this property
    */
   public List<HierarchicalProperty> getChildren()
   {
      return this.children;
   }

   /**
    * Retrieves child property by name.
    * 
    * @param name
    * @return property or null if not found
    */
   public HierarchicalProperty getChild(QName name)
   {
      for (HierarchicalProperty child : children)
      {
         if (child.getName().equals(name))
            return child;
      }
      return null;
   }

   /**
    * Retrieves child property by 0 based index.
    * 
    * @param index
    * @return
    */
   public HierarchicalProperty getChild(int index)
   {
      return children.get(index);
   }

   /**
    * @return property name
    */
   public QName getName()
   {
      return name;
   }

   /**
    * @return property value
    */
   public String getValue()
   {
      return value;
   }

   /**
    * sets the property value
    * 
    * @param value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * sets the attribute
    * 
    * @param attributeName
    * @param attributeValue
    */
   public void setAttribute(String attributeName, String attributeValue)
   {
      attributes.put(attributeName, attributeValue);
   }

   /**
    * sets the attribute
    * 
    * @param attributeName
    * @param attributeValue
    */
   public void setAttribute(QName attributeName, String attributeValue)
   {
      attributes.put(makeStringFromQName(attributeName), attributeValue);
   }

   /**
    * @param attributeName
    * @return attribute
    */
   public String getAttribute(String attributeName)
   {
      return attributes.get(attributeName);
   }

   /**
    * @return all attributes
    */
   public HashMap<String, String> getAttributes()
   {
      return attributes;
   }

   /**
    * @return name as string prefix:localPart
    */
   public String getStringName()
   {
      return makeStringFromQName(name);
   }

   private String makeStringFromQName(QName qname)
   {
      String str = "";
      if (qname.getPrefix() != null && qname.getPrefix().length() > 0)
         str += qname.getPrefix() + ":";
      return str + qname.getLocalPart();
   }
}
