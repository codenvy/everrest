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
package org.everrest.core.header;

/**
 * Implementation of this interface is useful for sort accepted media type and
 * languages by quality factor. For example see
 * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">HTTP/1.1 documentation</a>}
 * .
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: QualityValue.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public interface QualityValue
{

   /**
    * Default quality value. It should be used if quality value is not specified
    * in accept token.
    */
   public static final float DEFAULT_QUALITY_VALUE = 1.0F;

   /**
    * Quality value.
    */
   public static final String QVALUE = "q";

   /**
    * @return value of quality parameter
    */
   float getQvalue();

}
