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

import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class UriBuilderImpl extends UriBuilder {
    /** Scheme, e.g. http, https, etc. */
    private String schema;

    /** User info, such as user:password. */
    private String userInfo;

    /** Host name. */
    private String host;

    /** Server port. */
    private int port = -1;

    /** Path. */
    private StringBuilder path = new StringBuilder();

    /** Query string. */
    private StringBuilder query = new StringBuilder();

    /** Fragment. */
    private String fragment;

    public UriBuilderImpl() {
    }


    @Override
    public URI buildFromMap(Map<String, ?> values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        encode();
        String uri = UriPattern.createUriWithValues(
                schema,
                userInfo,
                host,
                port,
                path.toString(),
                query.toString(),
                fragment,
                values,
                true);
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }

    @Override
    public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
        return null;
    }


    @Override
    public URI buildFromEncodedMap(Map<String, ?> values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        encode();
        String uri = UriPattern.createUriWithValues(
                schema,
                userInfo,
                host,
                port,
                path.toString(),
                query.toString(),
                fragment,
                values,
                false);
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }


    @Override
    public URI build(Object... values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        encode();
        String uri = UriPattern.createUriWithValues(
                schema,
                userInfo,
                host,
                port,
                path.toString(),
                query.toString(),
                fragment,
                values,
                true);
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }

    @Override
    public URI build(Object[] values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        return null;
    }


    @Override
    public URI buildFromEncoded(Object... values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values aren't allowed");
        }
        encode();
        String uri = UriPattern.createUriWithValues(
                schema,
                userInfo,
                host,
                port,
                path.toString(),
                query.toString(),
                fragment,
                values,
                false);
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }

    @Override
    public String toTemplate() {
        return UriPattern.createUriWithValues(
                schema,
                userInfo,
                host,
                port,
                path.toString(),
                query.toString(),
                fragment,
                new Object[0],
                false,
                true);
    }

    /** Encode URI path, query and fragment components. */
    private void encode() {
        // Should do this even path all segments already encoded. The reason is
        // matrix parameters that is not encoded yet. Not able encode it just
        // after adding via particular method(s) since updating/removing of it may
        // be requested later. If matrix parameters encoded just after adding then
        // original name of parameters may be changed and need play with
        // encoding/decoding again when updating/removing of matrix parameter
        // performed.
        encodePath();
        encodeQuery();
        encodeFragment();
    }

    /** Encode URI path. */
    private void encodePath() {
        if (path.length() > 0) {
            // We are assumes matrix parameters is parameters added to last segment
            // of URI. Check  ';' after last '/'.
            int p = path.lastIndexOf("/");
            p = path.indexOf(";", p < 0 ? 0 : p);
            // If ';' not found then not need encode since path segments itself already encoded.
            if (p >= 0) {
                String t = path.toString();
                path.setLength(0);
                path.append(UriComponent.recognizeEncode(t, UriComponent.PATH, true));
            }
        }
    }

    /** Encode query parameters. */
    private void encodeQuery() {
        if (query.length() > 0) {
            String str = query.toString();
            query.setLength(0);
            int p = 0;
            while (p < str.length()) {
                if (str.charAt(p) == '=') // something like a=x&=y
                {
                    throw new UriBuilderException("Query parameter name is empty. ");
                }

                int n = str.indexOf('&', p);
                if (n < 0) {
                    n = str.length();
                }

                // skip empty pair, like a=x&&b=y
                if (n > p) {
                    final String pair = str.substring(p, n);
                    if (query.length() > 0) {
                        query.append('&');
                    }

                    final int eq = pair.indexOf('=');
                    if (eq == -1) {
                        // no value
                        query.append(UriComponent.recognizeEncode(pair, UriComponent.QUERY, true));
                    } else if (eq == (pair.length() - 1)) {
                        // no value but '=' present
                        query.append(UriComponent.recognizeEncode(pair.substring(0, eq), UriComponent.QUERY, true));
                    } else {
                        // encode key and value and keep delimiter '='
                        query.append(UriComponent.recognizeEncode(pair.substring(0, eq), UriComponent.QUERY, true));
                        query.append('=');
                        query.append(UriComponent.recognizeEncode(pair.substring(eq + 1), UriComponent.QUERY, true));
                    }
                }
                p = n + 1;
            }
        }
    }

    /** Encode URI fragment. */
    private void encodeFragment() {
        if (!(fragment == null || fragment.isEmpty())) {
            fragment = UriComponent.recognizeEncode(fragment, UriComponent.FRAGMENT, true);
        }
    }


    @Override
    public UriBuilder clone() {
        return new UriBuilderImpl(this);
    }

    /**
     * For #clone() method.
     *
     * @param cloned
     *         current UriBuilder.
     */
    protected UriBuilderImpl(UriBuilderImpl cloned) {
        this.schema = cloned.schema;
        this.userInfo = cloned.userInfo;
        this.host = cloned.host;
        this.port = cloned.port;
        this.path = new StringBuilder(cloned.path);
        this.query = new StringBuilder(cloned.query);
        this.fragment = cloned.fragment;
    }


    @Override
    public UriBuilder fragment(String fragment) {
        this.fragment = fragment == null ? null : UriComponent.encode(fragment, UriComponent.FRAGMENT, true);
        return this;
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value) {
        return resolveTemplate(name, value, false);
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        if (name == null) {
            throw new IllegalArgumentException("Null name of parameter isn't allowed");
        }
        if (value == null) {
            throw new IllegalArgumentException("Null value of parameter isn't allowed");
        }

        Map<String, Object> params = new HashMap<>(4);
        if (encodeSlashInPath) {
            params.put(name, UriComponent.encode(value.toString(), UriComponent.PATH_SEGMENT, false));
        } else {
            params.put(name, value);
        }
        return resolveTemplates(params);
    }

    @Override
    public UriBuilder resolveTemplateFromEncoded(String name, Object value) {
        return resolveTemplate(name, value, false);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues) {
        return resolveTemplates(templateValues, false);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) throws IllegalArgumentException {
        if (templateValues == null) {
            throw new IllegalArgumentException("Null map isn't allowed");
        }
        if (templateValues.containsKey(null)) {
            throw new IllegalArgumentException("Null names in map aren't allowed");
        }
        if (templateValues.containsValue(null)) {
            throw new IllegalArgumentException("Null values in map aren't allowed");
        }

        if (encodeSlashInPath) {
            for (Map.Entry<String, Object> entry : templateValues.entrySet()) {
                entry.setValue(UriComponent.encode(entry.getValue().toString(), UriComponent.PATH_SEGMENT, false));
            }
        }

        UriBuilderImpl resolved = UriComponent.parseTemplate(
                UriPattern.createUriWithValues(
                        schema,
                        userInfo,
                        host,
                        port,
                        path.toString(),
                        query.toString(),
                        fragment,
                        templateValues,
                        false,
                        true)
                                                            );
        this.schema = resolved.schema;
        this.userInfo = resolved.userInfo;
        this.host = resolved.host;
        this.port = resolved.port;
        this.path = new StringBuilder(resolved.path);
        this.query = new StringBuilder(resolved.query);
        this.fragment = resolved.fragment;
        return this;
    }

    @Override
    public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return resolveTemplates(templateValues, false);
    }


    @Override
    public UriBuilder host(String host) {
        this.host = host == null ? null : UriComponent.recognizeEncode(host, UriComponent.HOST, true);
        return this;
    }


    @Override
    public UriBuilder matrixParam(String name, Object... values) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values are null");
        }

        for (int i = 0, length = values.length; i < length; i++) {
            Object o = values[i];
            if (o == null) {
                throw new IllegalArgumentException("Value is null");
            }
            path.append(';');
            path.append(name);
            path.append('=');
            path.append(o.toString());
        }
        return this;
    }


    @Override
    public UriBuilder path(String p) {
        if (p == null) {
            throw new IllegalArgumentException("Path segments are null");
        }

        if (!p.isEmpty()) {
            // each segment can contains own '/' DO NOT escape it (UriComponent.PATH)
            p = UriComponent.recognizeEncode(p, UriComponent.PATH, true);

            boolean finalSlash = path.length() > 0 && path.charAt(path.length() - 1) == '/';
            boolean startSlash = p.charAt(0) == '/';

            if (finalSlash && startSlash) {
                if (p.length() > 1) {
                    path.append(p.substring(1));
                }
            } else if (path.length() > 0 && !finalSlash && !startSlash) {
                path.append('/');
                path.append(p);
            } else {
                path.append(p);
            }
        }
        return this;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public UriBuilder path(Class resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is null");
        }

        Annotation annotation = resource.getAnnotation(Path.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class is not annotated with javax.ws.rs.Path");
        }

        return path(((Path)annotation).value());
    }


    @Override
    public UriBuilder path(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method is null");
        }
        Path p = method.getAnnotation(Path.class);
        return p == null ? this : path(p.value());
    }


    @Override
    public UriBuilder path(Class resource, String method) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is null");
        }

        if (method == null) {
            throw new IllegalArgumentException("Method name is null");
        }

        Method[] methods = resource.getMethods();

        Method matched = null;
        for (int i = 0, length = methods.length; i < length; i++) {
            Method m = methods[i];
            if (matched != null && m.getName().equals(method)) {
                throw new IllegalArgumentException("More then one method with name " + method + " found");
            } else if (m.getName().equals(method)) {
                matched = m;
            }
        }

        // no one method found
        if (matched == null) {
            throw new IllegalArgumentException("Method " + method + " not found at resource class " + resource.getName());
        }

        path(matched);

        return this;
    }


    @Override
    public UriBuilder port(int port) {
        if (port < -1) {
            throw new IllegalArgumentException("Invalid port " + port);
        }
        this.port = port;
        return this;
    }


    @Override
    public UriBuilder queryParam(String name, Object... values) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values are null");
        }

        // Add values.
        for (int i = 0, length = values.length; i < length; i++) {
            Object o = values[i];
            if (o == null) {
                throw new IllegalArgumentException("Value is null");
            }
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(name);
            query.append('=');
            query.append(o.toString());
        }

        return this;
    }


    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        if (path.length() > 0) {
            String str = path.toString();
            int p = str.lastIndexOf('/');
            // Start of matrix params.
            p = str.indexOf(';', p < 0 ? 0 : p);

            if (p >= 0) {
                // Trim current matrix parameters.
                path.setLength(p);

                // Append all parameters we need to keep to the path.
                while (p < str.length()) {
                    int n = str.indexOf(';', p);
                    if (n < 0) {
                        n = str.length();
                    }

                    if (n > p) {
                        String pair = str.substring(p, n);
                        int eq = pair.indexOf('=');
                        String pairName = eq > 0 ? pair.substring(0, eq) : pair;

                        if (!name.equals(pairName)) {
                            if (path.length() > 0) {
                                path.append(';');
                            }
                            path.append(pair);
                        }
                    }
                    p = n + 1;
                }
            }
        }

        if (values != null && values.length > 0) {
            matrixParam(name, values);
        }

        return this;
    }


    @Override
    public UriBuilder replaceMatrix(String matrix) {
        // Search ';' which goes after '/' at final path segment.
        if (path.length() > 0) {
            int p = path.lastIndexOf("/");
            // If slash not found , then start search for ; from begin of string.
            p = path.indexOf(";", p < 0 ? 0 : p);
            if (p >= 0) {
                // Trim current matrix parameters.
                path.setLength(p);
            }
        }

        // If have values add them.
        if (!(matrix == null || matrix.isEmpty())) {
            path.append(';');
            path.append(matrix);
        }

        return this;
    }


    @Override
    public UriBuilder replacePath(String p) {
        path.setLength(0);
        if (!(p == null || p.isEmpty())) {
            path(p);
        }
        return this;
    }


    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        if (query.length() > 0) {
            int p = 0;
            // Freeze state of query string.
            String str = query.toString();
            // Erase raw query.
            query.setLength(0);
            while (p < str.length()) {
                // Split frozen string by '&' and copy in StringBuilder the pairs we need to keep.
                int n = str.indexOf('&', p);
                if (n < 0) {
                    n = str.length();
                }

                // Do nothing for sequence such as '&&'.
                if (n > p) {
                    String pair = str.substring(p, n);
                    int eq = pair.indexOf('=');
                    String pairName = eq > 0 ? pair.substring(0, eq) : pair;

                    if (!name.equals(pairName)) {
                        if (query.length() > 0) {
                            query.append('&');
                        }
                        query.append(pair);
                    }
                }
                p = n + 1;
            }
        }

        // Add new parameters to the end of raw query.
        if (values != null && values.length > 0) {
            queryParam(name, values);
        }

        return this;
    }


    @Override
    public UriBuilder replaceQuery(String queryString) {
        query.setLength(0);

        if (!(queryString == null || queryString.isEmpty())) {
            query.append(queryString);
        }

        return this;
    }


    @Override
    public UriBuilder scheme(String schema) {
        this.schema = schema != null ? UriComponent.validate(schema, UriComponent.SCHEME, true) : null;
        return this;
    }


    @Override
    public UriBuilder schemeSpecificPart(String ssp) {
        if (ssp == null) {
            throw new IllegalArgumentException("Scheme specific part (ssp) is null");
        }

        StringBuilder sb = new StringBuilder();

        if (schema != null) {
            sb.append(schema);
            sb.append(':');
            sb.append(UriComponent.recognizeEncode(ssp, UriComponent.SSP, true));
        }

        if (!(fragment == null || fragment.isEmpty())) {
            sb.append('#');
            sb.append(fragment);
        }

        URI uri;
        try {
            uri = new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        userInfo = uri.getRawUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path.setLength(0);
        path.append(uri.getRawPath());
        query.setLength(0);
        query.append(uri.getRawQuery() != null ? uri.getRawQuery() : "");

        return this;
    }


    @Override
    public UriBuilder segment(String... segments) {
        if (segments == null) {
            throw new IllegalArgumentException("Path segments is null");
        }

        for (int i = 0, length = segments.length; i < length; i++) {
            path(segments[i]);
        }

        return this;
    }


    @Override
    public UriBuilder uri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }

        if (uri.getScheme() != null) {
            schema = uri.getScheme();
        }

        if (uri.getRawUserInfo() != null) {
            userInfo = uri.getRawUserInfo();
        }

        if (uri.getHost() != null) {
            host = uri.getHost();
        }

        if (uri.getPort() != -1) {
            port = uri.getPort();
        }

        if (!(uri.getRawPath() == null || uri.getRawPath().isEmpty())) {
            path.setLength(0);
            path.append(uri.getRawPath());
        }

        if (!(uri.getRawQuery() == null || uri.getRawQuery().isEmpty())) {
            query.setLength(0);
            query.append(uri.getRawQuery());
        }

        if (uri.getRawFragment() != null) {
            fragment = uri.getRawFragment();
        }

        return this;
    }

    @Override
    public UriBuilder uri(String uriTemplate) {
        if (uriTemplate == null) {
            throw new IllegalArgumentException("URI template is null");
        }
        return uri(URI.create(uriTemplate));
    }


    @Override
    public UriBuilder userInfo(String userInfo) {
        this.userInfo = userInfo != null ? UriComponent.recognizeEncode(userInfo, UriComponent.USER_INFO, true) : null;
        return this;
    }
}
