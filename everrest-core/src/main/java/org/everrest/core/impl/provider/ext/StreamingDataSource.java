/**
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

package org.everrest.core.impl.provider.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Implementation of DataSource which simply wrap stream. Note this
 * implementation is not completely conform with DataSource contract. It does
 * not return new <code>InputStream</code> for each call of method
 * {@link #getInputStream()}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StreamingDataSource implements DataSource
{
   private final InputStream stream;

   private final String contentType;

   public StreamingDataSource(InputStream stream, String contentType)
   {
      this.stream = stream;
      this.contentType = contentType;
   }

   /**
    * {@inheritDoc}
    */
   public String getContentType()
   {
      return contentType;
   }

   /**
    * {@inheritDoc}
    */
   public InputStream getInputStream() throws IOException
   {
      return stream;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public OutputStream getOutputStream() throws IOException
   {
      throw new UnsupportedOperationException();
   }
}
