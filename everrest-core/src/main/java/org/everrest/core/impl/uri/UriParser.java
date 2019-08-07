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
package org.everrest.core.impl.uri;

import org.everrest.core.util.StringUtils;

import static org.everrest.core.impl.uri.UriComponent.PORT;
import static org.everrest.core.impl.uri.UriComponent.isUriComponentContainsValidCharacters;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.charAtIsNot;

class UriParser {
    private final String uri;

    private String scheme;
    private String authority;
    private String userInfo;
    private String host;
    private String port;
    private String path;
    private String query;
    private String fragment;
    private String schemeSpecificPart;

    UriParser(String uri) {
        this.uri = uri;
    }

    String getUri() {
        return uri;
    }

    String getScheme() {
        return scheme;
    }

    String getAuthority() {
        return authority;
    }

    String getUserInfo() {
        return userInfo;
    }

    String getHost() {
        return host;
    }

    String getPort() {
        return port;
    }

    String getPath() {
        return path;
    }

    String getQuery() {
        return query;
    }

    String getFragment() {
        return fragment;
    }

    String getSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    boolean isOpaque() {
        return path == null;
    }

    void parse() {
        int sspStart = 0;
        int p = parseScheme();
        if (scheme != null) {
            ++p;
            sspStart = p;
            if (charAtIs(uri, p, '/')) {
                p = parseHierarchical(p);
            } else {
                p = StringUtils.scan(uri, p, '#');
            }
        } else {
            p = parseHierarchical(p);
        }
        schemeSpecificPart = uri.substring(sspStart, p);
        parseFragment(p);
    }

    private int parseHierarchical(int begin) {
        int p = begin;
        if (charAtIs(uri, p, '/') && charAtIs(uri, p + 1, '/')) {
            p = parseAuthority(p + 2);
        }
        p = parsePath(p);
        p = parseQuery(p);
        return p;
    }

    private int parseScheme() {
        final int len = uri.length();
        int p = find(0, ":/?#", len);
        if (charAtIsNot(uri, p, ':')) {
            return 0;
        }
        if (p == 0 && charAtIs(uri, p, ':')) {
            throw new IllegalArgumentException(
                    String.format("Invalid template %s. Illegal character at %d. Scheme name expected", uri, p));
        }
        if (p < len) {
            scheme = uri.substring(0, p);
        }
        return p;
    }

    private int parseAuthority(int begin) {
        final int end = StringUtils.scan(uri, begin, '/');
        authority = uri.substring(begin, end);
        int p = parseUserInfo(begin, end);
        p = parseHost(p, end);
        p = parsePort(p, end);
        if (isPortInvalid()) {
            host = null;
            port = null;
            userInfo = null;
        }
        return p;
    }

    private boolean isPortInvalid() {
        return !(port == null || isTemplate(port) || isUriComponentContainsValidCharacters(PORT, port));
    }

    private boolean isTemplate(String str) {
        return charAtIs(str, 0, '{') && charAtIs(str, str.length() - 1, '}');
    }

    private int parseUserInfo(int begin, int end) {
        int p = find(begin, "@/?#", end);
        if (charAtIs(uri, p, '@')) {
            userInfo = uri.substring(begin, p);
            return p + 1;
        }
        return begin;
    }

    private int parseHost(int begin, int end) {
        int p = StringUtils.scan(uri, begin, ':', end);
        if (p > begin) {
            host = uri.substring(begin, p);
            return p + 1;
        }
        return begin;
    }

    private int parsePort(int begin, int end) {
        if (begin < end) {
            port = uri.substring(begin, end);
        }
        return end;
    }

    private int parsePath(int begin) {
        int p = find(begin, "?#", uri.length());
        path = uri.substring(begin, p);
        return p;
    }

    private int parseQuery(int begin) {
        if (charAtIs(uri, begin, '?')) {
            int p = StringUtils.scan(uri, begin, '#');
            if (p > begin) {
                query = uri.substring(begin + 1, p);
                return p;
            }
        }
        return begin;
    }

    private void parseFragment(int begin) {
        if (charAtIs(uri, begin, '#')) {
            if (begin < uri.length()) {
                fragment = uri.substring(begin + 1);
            }
        }
    }


    private int find(int begin, String findOneOfChars, int end) {
        for (int i = begin; i < end; i++) {
            if (StringUtils.contains(findOneOfChars, uri.charAt(i))) {
                return i;
            }
        }
        return end;
    }
}
