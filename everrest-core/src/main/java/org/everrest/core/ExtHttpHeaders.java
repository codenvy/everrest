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
package org.everrest.core;

import javax.ws.rs.core.HttpHeaders;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ExtHttpHeaders extends HttpHeaders
{

   /**
    * WebDav "Depth" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
    * HTTP Headers for Distributed Authoring</a> section 9 for more information.
    */
   public static final String DEPTH = "depth";

   /**
    * HTTP 1.1 "Accept-Ranges" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String ACCEPT_RANGES = "Accept-Ranges";

   /**
    * HTTP 1.1 "Allow" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String ALLOW = "Allow";

   /**
    * HTTP 1.1 "Authorization" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String AUTHORIZATION = "Authorization";

   /**
    * HTTP 1.1 "Content-Length" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String CONTENTLENGTH = "Content-Length";

   /**
    * HTTP 1.1 "Content-Range" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String CONTENTRANGE = "Content-Range";

   /**
    * HTTP 1.1 "Content-type" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String CONTENTTYPE = "Content-type";

   /**
    * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
    * HTTP Headers for Distributed Authoring</a> section 9 for more information.
    */
   public static final String DAV = "DAV";

   /**
    * HTTP 1.1 "Allow" header. See <a
    * href='http://msdn.microsoft.com/en-us/library/ms965954.aspx'> WebDAV/DASL
    * Request and Response Syntax</a> for more information.
    */
   public static final String DASL = "DASL";

   /**
    * MS-Author-Via Response Header. See <a
    * href='http://msdn.microsoft.com/en-us/library/cc250217.aspx'>
    * MS-Author-Via Response Header</a> for more information.
    */
   public static final String MSAUTHORVIA = "MS-Author-Via";

   /**
    * HTTP 1.1 "Range" header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
    * section 14 "Header Field Definitions"</a> for more information.
    */
   public static final String RANGE = "Range";

   /**
    * WebDav "Destination" header. See <a
    * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
    * Authoring</a> section 9 for more information.
    */
   public static final String DESTINATION = "Destination";

   /**
    * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
    * HTTP Headers for Distributed Authoring</a> section 9 for more information.
    */
   public static final String LOCKTOKEN = "lock-token";

   /**
    * WebDav "If" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
    * HTTP Headers for Distributed Authoring</a> section 9 for more information.
    */
   public static final String IF = "If";

   /**
    * WebDav "Timeout" header. See <a
    * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
    * Authoring</a> section 9 for more information.
    */
   public static final String TIMEOUT = "Timeout";

   /**
    * WebDav multipart/byteranges header.
    */
   public static final String MULTIPART_BYTERANGES = "multipart/byteranges; boundary=";

   /**
    * WebDav "Overwrite" header. See <a
    * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
    * Authoring</a> section 9 for more information.
    */
   public static final String OVERWRITE = "Overwrite";

   /**
    * X-HTTP-Method-Override header. See <a
    * href='http://code.google.com/apis/gdata/docs/2.0/basics.html'>here</a>.
    */
   public static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

   /**
    * User-Agent header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP Header
    * Field Definitions sec. 14.43 Transfer-Encoding</a>.
    */
   public static final String USERAGENT = "User-Agent";

   /**
    * Transfer-Encoding header. See <a
    * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP Header
    * Field Definitions sec. 14.41 Transfer-Encoding</a>.
    */
   public static final String TRANSFER_ENCODING = "Transfer-Encoding";

   /**
    * This header indicates that body is provided via JAXR framework. Value of
    * header MAY contain additional information about the nature of body's
    * content, for example: 'Error-Message'.
    */
   public static final String JAXRS_BODY_PROVIDED = "JAXRS-Body-Provided";
}
