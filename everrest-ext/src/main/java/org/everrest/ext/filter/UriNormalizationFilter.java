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
package org.everrest.ext.filter;

import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * This filter should be used for normalization URI according to rfc3986. For
 * details see http://www.unix.com.ua/rfc/rfc3986.html#s6.2.2 .
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: UriNormalizationFilter.java 301 2009-10-19 14:41:18Z aparfonov
 *          $
 */
@Filter
public final class UriNormalizationFilter implements RequestFilter
{

   /**
    * Should be used for normalization URI as described at
    * http://www.unix.com.ua/rfc/rfc3986.html#s6.2.2. {@inheritDoc}
    */
   public void doFilter(GenericContainerRequest request)
   {
      request.setUris(normalize(request.getRequestUri()), request.getBaseUri());
   }

   /**
    * @param uri source URI
    * @return normalized URI
    */
   private static URI normalize(URI uri)
   {
      String oldPath = uri.getRawPath();
      String normalizedPath = removeDotSegments(oldPath);
      if (normalizedPath.equals(oldPath))
      {
         // nothing to do, URI was normalized
         return uri;
      }
      return UriBuilder.fromUri(uri).replacePath(normalizedPath).build();
   }

   /**
    * Checks if the segment is a complete path segment
    * http://www.unix.com.ua/rfc/rfc3986.html#sB.
    * 
    * @param segment path segment
    * @param path whole path
    * @return true if segment is complete path segment false otherwise
    */
   private static boolean isComplitePathSeg(String segment, String path)
   {
      boolean result = false;
      int segPlace = path.indexOf(segment);
      if (path.equals("/" + segment))
      {
         result = true;
      }
      else if ((path.charAt(segPlace + segment.length()) == '/'))
      {
         result = true;
      }
      else
      {
         result = false;
      }
      return result;
   }

   /**
    * Removes Dot segments of URI as described in
    * http://www.unix.com.ua/rfc/rfc3986.html#s5.2.4.
    */
   private static String removeDotSegments(String path)
   {
      String inputBuffer = path;

      StringBuilder outputBuffer = new StringBuilder();

      if (inputBuffer.contains("//"))
      {
         inputBuffer = inputBuffer.replaceAll("//", "/");
      }

      while (inputBuffer.length() != 0)
      {
         // If the input buffer begins with a prefix of "../" or "./", then remove
         // that prefix
         // from the input buffer;
         // http://www.unix.com.ua/rfc/rfc3986.html#sA.
         if (inputBuffer.startsWith("../") || inputBuffer.startsWith("./"))
         {
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf("/") + 1, inputBuffer.length());
            continue;
         }
         else
         // if the input buffer begins with a prefix of "/./" or "/.", where "." is
         // a complete path
         // segment, then replace that prefix with "/" in the input buffer;
         // http://www.unix.com.ua/rfc/rfc3986.html#sB.
         if (inputBuffer.startsWith("/./") || (inputBuffer.startsWith("/.") && isComplitePathSeg(".", inputBuffer)))
         {
            if (inputBuffer.equals("/."))
            {
               inputBuffer = "";
               outputBuffer.append("/");
               continue;
            }
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf("/", 1), inputBuffer.length());
            continue;
         }
         else
         // if the input buffer begins with a prefix of "/../" or "/..", where ".."
         // is a complete
         // path segment, then replace that prefix with "/" in the input buffer and
         // remove the last
         // segment and its preceding "/" (if any) from the output buffer;
         // http://www.unix.com.ua/rfc/rfc3986.html#sC.
         if (inputBuffer.startsWith("/../") || (inputBuffer.startsWith("/..") && isComplitePathSeg("..", inputBuffer)))
         {
            if (inputBuffer.equals("/.."))
            {
               inputBuffer = "";
               outputBuffer.delete(outputBuffer.lastIndexOf("/") + 1, outputBuffer.length());
               continue;
            }
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf("/", 1), inputBuffer.length());
            outputBuffer.delete(outputBuffer.lastIndexOf("/"), outputBuffer.length());
            continue;
         }
         else
         // if the input buffer consists only of "." or "..", then remove that from
         // the input buffer;
         // http://www.unix.com.ua/rfc/rfc3986.html#sD.
         if (inputBuffer.equals(".") || inputBuffer.equals(".."))
         {
            inputBuffer = "";
            continue;
         }
         else
         // move the first path segment in the input buffer to the end of the
         // output buffer, including
         // the initial "/" character (if any) and any subsequent characters up to,
         // but not including,
         // the next "/" character or the end of the input buffer
         // http://www.unix.com.ua/rfc/rfc3986.html#sE.
         if (inputBuffer.indexOf("/") != inputBuffer.lastIndexOf('/'))
         {
            outputBuffer.append(inputBuffer.substring(0, inputBuffer.indexOf("/", 1)));
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf("/", 1));
         }
         else
         {
            outputBuffer.append(inputBuffer);
            inputBuffer = "";
         }
         continue;
      }
      return outputBuffer.toString();
   }

}
