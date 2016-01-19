/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.uri;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.ext.RuntimeDelegate;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
public class LinkBuilderImpl implements Link.Builder {
    private URI                 baseUri;
    private UriBuilder          uriBuilder;
    private Map<String, String> params;

    public LinkBuilderImpl() {
        params = new HashMap<>();
    }

    @Override
    public Link.Builder link(Link link) {
        uriBuilder = UriBuilder.fromUri(link.getUri());
        this.params.clear();
        this.params.putAll(link.getParams());
        return this;
    }

    @Override
    public Link.Builder link(String link) {
        return link(LinkImpl.valueOf(link));
    }

    @Override
    public Link.Builder uriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder.clone();
        return this;
    }

    @Override
    public Link.Builder uri(URI uri) {
        uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    @Override
    public Link.Builder uri(String uri) throws IllegalArgumentException {
        uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    @Override
    public Link.Builder rel(String rel) {
        if (rel == null) {
            throw new IllegalArgumentException("Null rel isn't allowed");
        }
        String value = params.get(Link.REL);
        if (value == null) {
            param(Link.REL, rel);
        } else {
            param(Link.REL, (value + ' ' + rel));
        }
        return this;
    }

    @Override
    public Link.Builder title(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Null title isn't allowed");
        }
        param(Link.TITLE, title);
        return this;

    }

    @Override
    public Link.Builder type(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Null type isn't allowed");
        }
        param(Link.TYPE, type);
        return this;
    }

    @Override
    public Link.Builder param(String name, String value) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Null name of parameter isn't allowed");
        }
        if (value == null) {
            throw new IllegalArgumentException("Null value of parameter isn't allowed");
        }
        params.put(name, value);
        return this;
    }

    @Override
    public Link build(Object... values) throws UriBuilderException {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        if (values.length > 0 && uriBuilder == null) {
            throw new UriBuilderException("Can't construct URI. UriBuilder isn't defined.");
        }
        URI myUri;
        if (uriBuilder == null) {
            myUri = URI.create("");
        } else {
            myUri = uriBuilder.build(values);
        }
        return new LinkImpl(myUri).withParams(params);
    }

    @Override
    public Link buildRelativized(URI uri, Object... values) {
        if (uri == null) {
            throw new IllegalArgumentException("Null uri isn't allowed");
        }
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        if (values.length > 0 && uriBuilder == null) {
            throw new UriBuilderException("Can't construct URI. UriBuilder isn't defined.");
        }
        URI myUri;
        if (uriBuilder == null) {
            myUri = URI.create("");
        } else {
            if (baseUri != null) {
                myUri = baseUri.resolve(uriBuilder.build(values));
            } else {
                myUri = uriBuilder.build(values);
            }
        }
        return new LinkImpl(uri.relativize(myUri)).withParams(params);
    }

    @Override
    public Link.Builder baseUri(URI uri) {
        this.baseUri = uri;
        return this;
    }

    @Override
    public Link.Builder baseUri(String uri) {
        this.baseUri = URI.create(uri);
        return this;
    }

    /**
     * @author andrew00x
     */
    public static class LinkImpl extends Link {
        private static final RuntimeDelegate.HeaderDelegate<Link> DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(Link.class);
        private URI                 uri;
        private Map<String, String> params;

        LinkImpl(URI uri) {
            this.uri = uri;
            params = new HashMap<>();
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public UriBuilder getUriBuilder() {
            return UriBuilder.fromUri(uri);
        }

        @Override
        public String getRel() {
            return params.get(REL);
        }

        @Override
        public List<String> getRels() {
            String rel = getRel();
            if (rel == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(rel.split("\\s+"));
        }

        @Override
        public String getTitle() {
            return params.get(TITLE);
        }

        @Override
        public String getType() {
            return params.get(TYPE);
        }

        @Override
        public Map<String, String> getParams() {
            return Collections.unmodifiableMap(params);
        }

        @Override
        public String toString() {
            return DELEGATE.toString();
        }

        Link withParams(Map<String, String> params) {
            this.params.putAll(params);
            return this;
        }
    }
}
