/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import javax.ws.rs.core.Cookie;

/**
 * Builder for {@link Cookie}
 */
public class CookieBuilder {
    public static CookieBuilder aCookie() {
        return new CookieBuilder();
    }

    /** Cookie version. */
    private int version = Cookie.DEFAULT_VERSION;
    /** Cookie name. */
    private String name;
    /** Cookie value. */
    private String value;
    /** Cookie path. */
    private String path;
    /** Cookie domain. */
    private String domain;

    public CookieBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CookieBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public CookieBuilder withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public CookieBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public CookieBuilder withVersion(int version) {
        this.version = version;
        return this;
    }

    public Cookie build() {
        return new Cookie(name, value, path, domain, version);
    }
}
