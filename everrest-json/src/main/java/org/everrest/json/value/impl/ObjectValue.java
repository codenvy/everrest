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
package org.everrest.json.value.impl;

import org.everrest.json.JsonException;
import org.everrest.json.JsonWriter;
import org.everrest.json.impl.JsonUtils;
import org.everrest.json.value.JsonValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ObjectValue.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class ObjectValue extends JsonValue
{

   /**
    * Children.
    */
   private final Map<String, JsonValue> children = new HashMap<String, JsonValue>();

   /**
    * {@inheritDoc}
    */
   @Override
   public void addElement(String key, JsonValue child)
   {
      children.put(key, child);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isObject()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<String> getKeys()
   {
      return children.keySet().iterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public JsonValue getElement(String key)
   {
      return children.get(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append('{');
      int i = 0;
      for (String key : children.keySet())
      {
         if (i > 0)
            sb.append(',');
         i++;
         sb.append(JsonUtils.getJsonString(key));
         sb.append(':');
         sb.append(children.get(key).toString());
      }
      sb.append('}');
      return sb.toString();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void writeTo(JsonWriter writer) throws JsonException
   {
      writer.writeStartObject();
      for (String key : children.keySet())
      {
         writer.writeKey(key);
         JsonValue v = children.get(key);
         v.writeTo(writer);
      }
      writer.writeEndObject();
   }

}
