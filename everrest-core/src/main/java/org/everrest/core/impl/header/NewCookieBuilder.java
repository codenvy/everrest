/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.Date;

/**
 * Builder for {@link NewCookie}
 */
public class NewCookieBuilder {
    public static NewCookieBuilder aNewCookie() {
        return new NewCookieBuilder();
    }

    private int version = Cookie.DEFAULT_VERSION;
    private int maxAge  = NewCookie.DEFAULT_MAX_AGE;
    private String  name;
    private String  value;
    private String  path;
    private String  domain;
    private String  comment;
    private boolean secure;
    private boolean httpOnly;
    private Date    expiry;

    public NewCookieBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public NewCookieBuilder withValue(String value) {
        this.value = value;
        return this;
    }
    public NewCookieBuilder withVersion(int version) {
        this.version = version;
        return this;
    }

    public NewCookieBuilder withMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public NewCookieBuilder withExpiry(Date expiry) {
        this.expiry = expiry;
        return this;
    }

    public NewCookieBuilder withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public NewCookieBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public NewCookieBuilder withSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public NewCookieBuilder withHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public NewCookieBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public NewCookie build() {
        return new NewCookie(name, value, path, domain, version, comment, maxAge, expiry, secure, httpOnly);
    }
}
