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
package org.everrest.core.impl.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: IOHelper.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public final class IOHelper
{

   /** Default character set name. */
   static final String DEFAULT_CHARSET_NAME = "UTF-8";

   /** If character set was not specified then this will be used. */
   static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

   /** Constructor. */
   private IOHelper()
   {
   }

   /**
    * Write data from {@link InputStream} to {@link OutputStream}.
    *
    * @param in See {@link InputStream}
    * @param out See {@link OutputStream}
    * @throws IOException if i/o errors occurs
    */
   public static void write(InputStream in, OutputStream out) throws IOException
   {
      byte[] buf = new byte[1024];
      int rd = -1;
      while ((rd = in.read(buf)) != -1)
         out.write(buf, 0, rd);
   }

   /**
    * Write data from {@link Reader} to {@link Writer}.
    *
    * @param in See {@link Reader}
    * @param out See {@link Writer}
    * @throws IOException if i/o errors occurs
    */
   public static void write(Reader in, Writer out) throws IOException
   {
      char[] buf = new char[1024];
      int rd = -1;
      while ((rd = in.read(buf)) != -1)
         out.write(buf, 0, rd);
   }

   /**
    * Read String from given {@link InputStream}.
    *
    * @param in source stream for reading
    * @param cs character set, if null then {@link #DEFAULT_CHARSET} will be
    *        used
    * @return resulting String
    * @throws IOException if i/o errors occurs
    */
   public static String readString(InputStream in, String cs) throws IOException
   {
      Charset charset;
      // Must respect application specified character set.
      // For output if specified character set is not supported then UTF-8 should
      // be used instead.
      try
      {
         charset = cs != null ? Charset.forName(cs) : DEFAULT_CHARSET;
      }
      catch (Exception e)
      {
         charset = DEFAULT_CHARSET;
      }
      Reader r = new InputStreamReader(in, charset);
      char[] buf = new char[1024];
      StringBuilder sb = new StringBuilder();
      int rd = -1;
      while ((rd = r.read(buf)) != -1)
         sb.append(buf, 0, rd);

      return sb.toString();
   }

   /**
    * Write String to {@link OutputStream}.
    *
    * @param s String
    * @param out See {@link OutputStream}
    * @param cs character set, if null then {@link #DEFAULT_CHARSET} will be
    *        used
    * @throws IOException if i/o errors occurs
    */
   public static void writeString(String s, OutputStream out, String cs) throws IOException
   {
      Charset charset;
      // Must respect application specified character set.
      // For output if specified character set is not supported then UTF-8 should
      // be used instead.
      try
      {
         charset = cs != null ? Charset.forName(cs) : DEFAULT_CHARSET;
      }
      catch (Exception e)
      {
         charset = DEFAULT_CHARSET;
      }
      Writer w = new OutputStreamWriter(out, charset);
      try
      {
         w.write(s);
      }
      finally
      {
         w.flush();
         //w.close();
      }
   }

}
