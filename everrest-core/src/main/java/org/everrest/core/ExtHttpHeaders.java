/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core;

import javax.ws.rs.core.HttpHeaders;

/**
 * @author andrew00x
 */
public interface ExtHttpHeaders extends HttpHeaders {

    /**
     * HTTP 1.1 "Accept-Ranges" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
     * section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * HTTP 1.1 "Content-Range" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
     * section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String CONTENT_RANGE = "Content-Range";

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
     * WebDav "Depth" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
     * HTTP Headers for Distributed Authoring</a> section 9 for more information.
     */
    public static final String DEPTH = "depth";

    /**
     * WebDav "Destination" header. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
     * Authoring</a> section 9 for more information.
     */
    public static final String DESTINATION = "Destination";

    /**
     * Forwarded Host header. See
     * <a href='http://tools.ietf.org/html/rfc7239#section-5.3'>
     * Forwarded HTTP Extension sec. 5.3 Forwarded Host</a>.
     */
    public static final String FORWARDED_HOST = "X-Forwarded-Host";

    /**
     * Forwarded Protocol header. See
     * <a href='http://tools.ietf.org/html/rfc7239#section-5.4'>
     * Forwarded HTTP Extension sec. 5.4 Forwarded Proto</a>.
     */
    public static final String FORWARDED_PROTO = "X-Forwarded-Proto";

    /**
     * WebDav "If" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
     * HTTP Headers for Distributed Authoring</a> section 9 for more information.
     */
    public static final String IF = "If";

    /**
     * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>
     * HTTP Headers for Distributed Authoring</a> section 9 for more information.
     */
    public static final String LOCKTOKEN = "lock-token";

    /**
     * WebDav "Overwrite" header. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
     * Authoring</a> section 9 for more information.
     */
    public static final String OVERWRITE = "Overwrite";

    /**
     * MS-Author-Via Response Header. See <a
     * href='http://msdn.microsoft.com/en-us/library/cc250217.aspx'>
     * MS-Author-Via Response Header</a> for more information.
     */
    public static final String MSAUTHORVIA = "MS-Author-Via";

    /** WebDav multipart/byteranges header. */
    public static final String MULTIPART_BYTERANGES = "multipart/byteranges; boundary=";

    /**
     * HTTP 1.1 "Range" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1
     * section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String RANGE = "Range";

    /**
     * WebDav "Timeout" header. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for Distributed
     * Authoring</a> section 9 for more information.
     */
    public static final String TIMEOUT = "Timeout";

    /**
     * Transfer-Encoding header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP Header
     * Field Definitions sec. 14.41 Transfer-Encoding</a>.
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * X-HTTP-Method-Override header. See <a
     * href='http://code.google.com/apis/gdata/docs/2.0/basics.html'>here</a>.
     */
    public static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

    /**
     * This header indicates that body is provided via JAXR framework. Value of
     * header MAY contain additional information about the nature of body's
     * content, for example: 'Error-Message'.
     */
    @Deprecated
    public static final String JAXRS_BODY_PROVIDED = "JAXRS-Body-Provided";
}
