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
package org.everrest.core.impl.provider.json;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ObjectValue extends JsonValue
{

   /** Children. */
   private final Map<String, JsonValue> children = new LinkedHashMap<String, JsonValue>();

   /** {@inheritDoc} */
   @Override
   public void addElement(String key, JsonValue child)
   {
      children.put(key, child);
   }

   /** {@inheritDoc} */
   @Override
   public boolean isObject()
   {
      return true;
   }

   /** {@inheritDoc} */
   @Override
   public Iterator<String> getKeys()
   {
      return children.keySet().iterator();
   }

   /** {@inheritDoc} */
   @Override
   public JsonValue getElement(String key)
   {
      return children.get(key);
   }

   /** {@inheritDoc} */
   @Override
   public String toString()
   {
      StringWriter w = new StringWriter();
      JsonWriter jw = new JsonWriter(w);
      try
      {
         writeTo(jw);
      }
      catch (JsonException e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
      return w.toString();
   }

   /** {@inheritDoc} */
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
