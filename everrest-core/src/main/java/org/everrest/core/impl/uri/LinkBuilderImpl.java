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

import com.google.common.base.Objects;

import org.everrest.core.impl.header.HeaderParameterParser;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static org.everrest.core.impl.uri.UriComponent.normalize;
import static org.everrest.core.impl.uri.UriComponent.resolve;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

/**
 * @author andrew00x
 */
public class LinkBuilderImpl implements Link.Builder {
    private URI                 baseUri;
    private UriBuilder          uriBuilder;
    private Map<String, String> params;

    public LinkBuilderImpl() {
        uriBuilder = new UriBuilderImpl();
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
        checkArgument(link != null, "Link string might not be null");
        this.params.clear();
        int p = scan(link, '<');
        checkArgument(charAtIs(link, p, '<'), "Link string must start with '<'");
        int n = scan(link, p, '>');
        checkArgument(charAtIs(link, n, '>'), String.format("Missing '>' in link: %s", link));
        String uri = link.substring(p + 1, n).trim();

        p = scan(link, n, ';');
        if (charAtIs(link, p, ';')) {
            try {
                Map<String, String> params = new HeaderParameterParser().parse(link);
                uriBuilder = UriBuilder.fromUri(uri);
                this.params.putAll(params);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else {
            uriBuilder = UriBuilder.fromUri(uri);
        }
        return this;
    }

    @Override
    public Link.Builder uriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = UriBuilder.fromUri(uriBuilder.toTemplate());
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
        checkArgument(rel != null, "Null rel isn't allowed");
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
        checkArgument(title != null, "Null title isn't allowed");
        param(Link.TITLE, title);
        return this;

    }

    @Override
    public Link.Builder type(String type) {
        checkArgument(type != null, "Null type isn't allowed");
        param(Link.TYPE, type);
        return this;
    }

    @Override
    public Link.Builder param(String name, String value) throws IllegalArgumentException {
        checkArgument(name != null, "Null name of parameter isn't allowed");
        checkArgument(value != null, "Null value of parameter isn't allowed");
        params.put(name, value);
        return this;
    }

    @Override
    public Link build(Object... values) throws UriBuilderException {
        checkArgument(values != null, "Null values aren't allowed");
        URI myUri = resolveLinkUri(values);
        return new LinkImpl(myUri, params);
    }

    @Override
    public Link buildRelativized(URI uri, Object... values) {
        checkArgument(uri != null, "Null uri isn't allowed");
        checkArgument(values != null, "Null values aren't allowed");
        URI myUri = resolveLinkUri(values);
        return new LinkImpl(uri.relativize(myUri), new HashMap<>(params));
    }

    private URI resolveLinkUri(Object... values) {
        URI result = uriBuilder.build(values);
        if (baseUri == null || result.isAbsolute()) {
            result = normalize(result);
        } else {
            result = resolve(baseUri, result);
        }
        return result;
    }

    @Override
    public Link.Builder baseUri(URI uri) {
        this.baseUri = uri;
        return this;
    }

    @Override
    public Link.Builder baseUri(String uri) {
        return baseUri(URI.create(uri));
    }

    public static class LinkImpl extends Link {
        private URI                 uri;
        private Map<String, String> params;

        LinkImpl(URI uri, Map<String, String> params) {
            this.uri = uri;
            this.params = params;
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
            StringBuilder sb = new StringBuilder();
            sb.append('<');
            sb.append(uri);
            sb.append('>');

            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(';');
                sb.append(entry.getKey());
                sb.append("=\"");
                sb.append(entry.getValue());
                sb.append('"');
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Link)) {
                return false;
            }
            Link link = (Link)o;
            return Objects.equal(uri, link.getUri()) &&
                   Objects.equal(params, link.getParams());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uri, params);
        }
    }
}
