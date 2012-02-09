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
package org.everrest.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Faster not synchronized version of ByteArrayOutputStream. Method
 * {@link #getBytes()} gives direct access to byte buffer.
 *
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class NoSyncByteArrayOutputStream extends ByteArrayOutputStream
{
   public NoSyncByteArrayOutputStream()
   {
      this(32);
   }

   public NoSyncByteArrayOutputStream(int size)
   {
      super(size);
   }

   /**
    * Get original byte buffer instead create copy of it as {@link #toByteArray()} does.
    *
    * @return original byte buffer
    */
   public byte[] getBytes()
   {
      return buf;
   }

   /** {@inheritDoc} */
   @Override
   public void reset()
   {
      count = 0;
   }

   /** {@inheritDoc} */
   @Override
   public int size()
   {
      return count;
   }

   /** {@inheritDoc} */
   @Override
   public byte[] toByteArray()
   {
      byte[] newBuf = new byte[count];
      System.arraycopy(buf, 0, newBuf, 0, count);
      return newBuf;
   }

   /** {@inheritDoc} */
   @Override
   public String toString()
   {
      return new String(buf, 0, count);
   }

   /** {@inheritDoc} */
   @Override
   public String toString(String charsetName) throws UnsupportedEncodingException
   {
      return new String(buf, 0, count, charsetName);
   }

   /** {@inheritDoc} */
   @Override
   public void write(byte[] b)
   {
      if (b.length == 0)
      {
         return;
      }
      int pos = count + b.length;
      if (pos > buf.length)
      {
         expand(Math.max(buf.length << 1, pos));
      }
      System.arraycopy(b, 0, buf, count, b.length);
      count = pos;
   }

   /** {@inheritDoc} */
   @Override
   public void write(byte[] b, int off, int len)
   {
      if (len == 0)
      {
         return;
      }
      if ((off < 0) || (len < 0) || (off > b.length) || ((off + len) > b.length))
      {
         throw new IndexOutOfBoundsException();
      }
      int pos = count + len;
      if (pos > buf.length)
      {
         expand(Math.max(buf.length << 1, pos));
      }
      System.arraycopy(b, off, buf, count, len);
      count = pos;
   }

   /** {@inheritDoc} */
   @Override
   public void write(int b)
   {
      int pos = count + 1;
      if (count >= buf.length)
      {
         expand(Math.max(buf.length << 1, pos));
      }
      buf[count] = (byte)b;
      count = pos;
   }

   /** {@inheritDoc} */
   @Override
   public void writeTo(OutputStream out) throws IOException
   {
      out.write(buf, 0, count);
   }

   /**
    * Expand buffer size to <code>newSize</code>.
    *
    * @param newSize new buffer size
    */
   private void expand(int newSize)
   {
      byte[] newBuf = new byte[newSize];
      System.arraycopy(buf, 0, newBuf, 0, count);
      buf = newBuf;
   }
}
