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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class DoubleValue extends NumericValue
{

   /** Value. */
   private final double value;

   /**
    * Constructs new DoubleValue.
    * 
    * @param value the value.
    */
   public DoubleValue(double value)
   {
      this.value = value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isDouble()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getStringValue()
   {
      return Double.toString(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public byte getByteValue()
   {
      return (byte)value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public short getShortValue()
   {
      return (short)value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getIntValue()
   {
      return (int)value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long getLongValue()
   {
      return (long)value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public float getFloatValue()
   {
      return (float)value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public double getDoubleValue()
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return getStringValue();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void writeTo(JsonWriter writer) throws JsonException
   {
      writer.writeValue(value);
   }

}
