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
package org.everrest.core.impl.uri;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.everrest.core.impl.uri.UriComponent.FRAGMENT;
import static org.everrest.core.impl.uri.UriComponent.HOST;
import static org.everrest.core.impl.uri.UriComponent.MATRIX_PARAM;
import static org.everrest.core.impl.uri.UriComponent.PATH;
import static org.everrest.core.impl.uri.UriComponent.PATH_SEGMENT;
import static org.everrest.core.impl.uri.UriComponent.PORT;
import static org.everrest.core.impl.uri.UriComponent.QUERY;
import static org.everrest.core.impl.uri.UriComponent.QUERY_STRING;
import static org.everrest.core.impl.uri.UriComponent.SCHEME;
import static org.everrest.core.impl.uri.UriComponent.SSP;
import static org.everrest.core.impl.uri.UriComponent.USER_INFO;
import static org.everrest.core.impl.uri.UriComponent.encode;
import static org.everrest.core.impl.uri.UriComponent.recognizeEncode;
import static org.everrest.core.impl.uri.UriComponent.validateUriComponent;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UriBuilderImpl extends UriBuilder {
  private String schema;
  private String authority;
  private String userInfo;
  private String host;
  private String port;
  private StringBuilder path = new StringBuilder();
  private StringBuilder query = new StringBuilder();
  private String fragment;
  private String ssp;
  private MultivaluedMap<String, Object> matrixParameters;

  public UriBuilderImpl() {}

  @Override
  public URI buildFromMap(Map<String, ?> values) {
    return buildFromMap(values, true);
  }

  @Override
  public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) {
    checkArgument(values != null, "Null values aren't allowed");

    String uri;
    encodeFragment();
    if (ssp != null) {
      uri = createUriFromSspWithValues(values, true);
    } else {
      preparePath();
      uri = createUriWithValues(values, true, encodeSlashInPath);
    }

    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new UriBuilderException(e);
    }
  }

  @Override
  public URI buildFromEncodedMap(Map<String, ?> values) {
    checkArgument(values != null, "Null values aren't allowed");

    String uri;
    encodeFragment();
    if (ssp != null) {
      uri = createUriFromSspWithValues(values, false);
    } else {
      preparePath();
      uri = createUriWithValues(values, false, false);
    }

    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new UriBuilderException(e);
    }
  }

  @Override
  public URI build(Object... values) {
    return build(values, true);
  }

  @Override
  public URI build(Object[] values, boolean encodeSlashInPath) {
    checkArgument(values != null, "Null values aren't allowed");

    String uri;
    encodeFragment();
    if (ssp != null) {
      uri = createUriFromSspWithValues(values, true);
    } else {
      preparePath();
      uri = createUriWithValues(values, true, encodeSlashInPath);
    }

    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new UriBuilderException(e);
    }
  }

  @Override
  public URI buildFromEncoded(Object... values) {
    checkArgument(values != null, "Null values aren't allowed");

    String uri;
    encodeFragment();
    if (ssp != null) {
      uri = createUriFromSspWithValues(values, false);
    } else {
      preparePath();
      uri = createUriWithValues(values, false, false);
    }

    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new UriBuilderException(e);
    }
  }

  private String createUriWithValues(Object[] values, boolean encode, boolean encodeSlashInPath) {
    return createUriWithValues(
        schema,
        authority,
        userInfo,
        host,
        port,
        path.toString(),
        query.toString(),
        fragment,
        values,
        encode,
        encodeSlashInPath,
        false);
  }

  private String createUriWithValues(
      Map<String, ?> values, boolean encode, boolean encodeSlashInPath) {
    return createUriWithValues(
        schema,
        authority,
        userInfo,
        host,
        port,
        path.toString(),
        query.toString(),
        fragment,
        values,
        encode,
        encodeSlashInPath,
        false);
  }

  private String createUriFromSspWithValues(Map<String, ?> values, boolean encode) {
    StringBuilder uri = new StringBuilder();
    appendUriPart(uri, ssp, SSP, values, encode, false);
    uri.append(':');
    appendUriPart(uri, ssp, SSP, values, encode, false);
    if (!isNullOrEmpty(fragment)) {
      uri.append('#');
      appendUriPart(uri, ssp, SSP, values, encode, false);
    }
    return uri.toString();
  }

  private String createUriFromSspWithValues(Object[] values, boolean encode) {
    StringBuilder uri = new StringBuilder();
    Map<String, String> m = new HashMap<>();
    int offset = 0;
    offset = appendUriPart(uri, schema, SCHEME, values, offset, m, encode, false);
    uri.append(':');
    offset = appendUriPart(uri, ssp, SSP, values, offset, m, encode, false);
    if (!isNullOrEmpty(fragment)) {
      uri.append('#');
      appendUriPart(uri, fragment, FRAGMENT, values, offset, m, encode, false);
    }
    return uri.toString();
  }

  @Override
  public String toTemplate() {
    return createUriWithValues(
        schema,
        authority,
        userInfo,
        host,
        port,
        path.toString(),
        query.toString(),
        fragment,
        new Object[0],
        false,
        false,
        true);
  }

  private void preparePath() {
    appendMatrixParametersToPath();
    if (path.length() == 0
        && !isNullOrEmpty(host)
        && (query.length() > 0 || !isNullOrEmpty(fragment))) {
      path.append('/');
    }
  }

  private void encodeFragment() {
    if (!isNullOrEmpty(fragment)) {
      fragment = recognizeEncode(fragment, FRAGMENT, true);
    }
  }

  @Override
  public UriBuilder clone() {
    return new UriBuilderImpl(this);
  }

  /**
   * For {@link #clone()} method.
   *
   * @param cloned current UriBuilder.
   */
  protected UriBuilderImpl(UriBuilderImpl cloned) {
    this.schema = cloned.schema;
    this.ssp = cloned.ssp;
    this.authority = cloned.authority;
    this.userInfo = cloned.userInfo;
    this.host = cloned.host;
    this.port = cloned.port;
    this.path = new StringBuilder(cloned.path);
    if (cloned.matrixParameters != null) {
      this.matrixParameters = new MultivaluedHashMap<String, Object>(cloned.matrixParameters);
    }
    this.query = new StringBuilder(cloned.query);
    this.fragment = cloned.fragment;
  }

  @Override
  public UriBuilder fragment(String fragment) {
    this.fragment = fragment == null ? null : encode(fragment, FRAGMENT, true);
    return this;
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value) {
    return resolveTemplate(name, value, false);
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
    checkArgument(name != null, "Null name of parameter isn't allowed");
    checkArgument(value != null, "Null value of parameter isn't allowed");

    Map<String, Object> params = new HashMap<>(4);
    params.put(name, value);
    return resolveTemplates(params, encodeSlashInPath);
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
  public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
      throws IllegalArgumentException {
    checkArgument(templateValues != null, "Null map isn't allowed");
    checkArgument(!templateValues.containsKey(null), "Null names in map aren't allowed");
    checkArgument(!templateValues.containsValue(null), "Null values in map aren't allowed");

    parseTemplate(
        createUriWithValues(
            schema,
            authority,
            userInfo,
            host,
            port,
            path.toString(),
            query.toString(),
            fragment,
            templateValues,
            false,
            encodeSlashInPath,
            true));
    return this;
  }

  @Override
  public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
    return resolveTemplates(templateValues, false);
  }

  private void parseTemplate(String uriTemplate) {
    UriParser uriParser = new UriParser(uriTemplate);
    uriParser.parse();

    if (uriParser.getScheme() != null) {
      this.schema = uriParser.getScheme();
    }
    if (uriParser.isOpaque() && uriParser.getSchemeSpecificPart() != null) {
      this.ssp = uriParser.getSchemeSpecificPart();
    }
    if (uriParser.getAuthority() != null) {
      this.authority = uriParser.getAuthority();
    }
    if (uriParser.getUserInfo() != null) {
      this.userInfo = uriParser.getUserInfo();
    }
    if (uriParser.getHost() != null) {
      this.host = uriParser.getHost();
    }
    if (uriParser.getPort() != null) {
      this.port = uriParser.getPort();
    }
    if (!isNullOrEmpty(uriParser.getPath())) {
      this.path.setLength(0);
      path(uriParser.getPath());
    }
    if (uriParser.getQuery() != null) {
      this.query.setLength(0);
      this.query.append(uriParser.getQuery());
    }
    if (uriParser.getFragment() != null) {
      this.fragment = uriParser.getFragment();
    }
  }

  @Override
  public UriBuilder host(String host) {
    if (host == null) {
      this.host = null;
      this.authority = null;
    } else if (host.isEmpty()) {
      throw new IllegalArgumentException("Invalid host ''");
    } else {
      this.host = recognizeEncode(host, HOST, true);
      this.authority = null;
    }
    return this;
  }

  @Override
  public UriBuilder matrixParam(String name, Object... values) {
    checkArgument(name != null, "Null name isn't allowed");
    checkArgument(values != null, "Null values aren't allowed");
    matrixParam(false, name, values);
    return this;
  }

  private void matrixParam(boolean replace, String name, Object... values) {
    if (matrixParameters == null) {
      if (values == null || values.length == 0) {
        return;
      }
      matrixParameters = new MultivaluedHashMap<>();
    }
    name = recognizeEncode(name, MATRIX_PARAM, true);
    if (replace) {
      matrixParameters.remove(name);
    }
    if (values != null && values.length > 0) {
      checkArgument(Arrays.stream(values).allMatch(o -> o != null), "Null value isn't allowed");
      matrixParameters.addAll(name, values);
    }
  }

  @Override
  public UriBuilder path(String addingPath) {
    checkArgument(addingPath != null, "Null path isn't allowed");

    if (!addingPath.isEmpty()) {
      appendMatrixParametersToPath();

      int startOfMatrixParamsInAddingPath = findStartOfMatrixParams(addingPath);

      String addingPathMatrixParams = null;
      boolean needAddTrailingSlash = false;
      if (startOfMatrixParamsInAddingPath != -1) {
        needAddTrailingSlash = charAtIs(addingPath, addingPath.length() - 1, '/');
        addingPathMatrixParams = addingPath.substring(startOfMatrixParamsInAddingPath + 1);
        addingPath = addingPath.substring(0, startOfMatrixParamsInAddingPath);
      }

      boolean currentPathHasTrailingSlash = charAtIs(path, path.length() - 1, '/');
      boolean addingPathHasLeadingSlash = charAtIs(addingPath, 0, '/');

      addingPath = recognizeEncode(addingPath, PATH, true);
      if (!isNullOrEmpty(addingPathMatrixParams)) {
        parseMatrixParams(addingPathMatrixParams);
      }

      if (currentPathHasTrailingSlash && addingPathHasLeadingSlash) {
        if (addingPath.length() > 1) {
          path.append(addingPath, 1, addingPath.length());
        }
      } else if (path.length() > 0 && !currentPathHasTrailingSlash && !addingPathHasLeadingSlash) {
        path.append('/');
        path.append(addingPath);
      } else {
        path.append(addingPath);
      }
      if (needAddTrailingSlash) {
        path.append('/');
      }
    }

    return this;
  }

  private void appendMatrixParametersToPath() {
    if (matrixParameters != null) {
      boolean pathEndsWithSlash = path.length() > 1 && charAtIs(path, path.length() - 1, '/');
      if (pathEndsWithSlash) {
        path.setLength(path.length() - 1);
      }
      for (Map.Entry<String, List<Object>> entry : matrixParameters.entrySet()) {
        List<Object> values = entry.getValue();
        if (!(values == null || values.isEmpty())) {
          String name = recognizeEncode(entry.getKey(), MATRIX_PARAM, true);
          for (Object value : values) {
            path.append(';');
            path.append(recognizeEncode(name, MATRIX_PARAM, true));
            path.append('=');
            path.append(recognizeEncode(String.valueOf(value), MATRIX_PARAM, true));
          }
        }
      }
      matrixParameters.clear();
      if (pathEndsWithSlash) {
        path.append('/');
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public UriBuilder path(Class resource) {
    checkArgument(resource != null, "Null resource class isn't allowed");

    Annotation pathAnnotation = resource.getAnnotation(Path.class);
    checkArgument(pathAnnotation != null, "Class is not annotated with jakarta.ws.rs.Path");

    return path(((Path) pathAnnotation).value());
  }

  @Override
  public UriBuilder path(Method method) {
    checkArgument(method != null, "Null method isn't allowed");

    Path pathAnnotation = method.getAnnotation(Path.class);
    return pathAnnotation == null ? this : path(pathAnnotation.value());
  }

  @Override
  public UriBuilder path(Class resource, String method) {
    checkArgument(resource != null, "Null resource class isn't allowed");
    checkArgument(method != null, "Null method name isn't allowed");

    Method[] methods = resource.getMethods();

    Method matched = null;
    for (int i = 0, length = methods.length; i < length; i++) {
      Method m = methods[i];
      if (m.getName().equals(method)) {
        if (matched != null && !(matched.isSynthetic() || m.isSynthetic())) {
          throw new IllegalArgumentException(
              String.format("More then one method with name %s found", method));
        } else {
          matched = m;
        }
      }
    }

    if (matched == null) {
      throw new IllegalArgumentException(
          String.format("Method %s not found at resource class %s", method, resource.getName()));
    }

    path(matched);

    return this;
  }

  @Override
  public UriBuilder port(int port) {
    checkArgument(port == -1 || (port >= 0 && port <= 65535), "Invalid port %d", port);
    if (port == -1) {
      this.port = null;
    } else {
      this.port = Integer.toString(port);
    }
    return this;
  }

  @Override
  public UriBuilder queryParam(String name, Object... values) {
    checkArgument(name != null, "Null name isn't allowed");
    checkArgument(values != null, "Null values aren't allowed");

    for (int i = 0, length = values.length; i < length; i++) {
      Object o = values[i];
      checkArgument(o != null, "Null value isn't allowed");
      if (query.length() > 0) {
        query.append('&');
      }
      query.append(recognizeEncode(name, QUERY, true));
      query.append('=');
      query.append(recognizeEncode(o.toString(), QUERY, true));
    }

    return this;
  }

  @Override
  public UriBuilder replaceMatrixParam(String name, Object... values) {
    checkArgument(name != null, "Null name isn't allowed");
    matrixParam(true, name, values);
    return this;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix) {
    if (matrixParameters != null) {
      matrixParameters.clear();
    }
    parseMatrixParams(matrix);
    return this;
  }

  private void parseMatrixParams(String matrixString) {
    if (!isNullOrEmpty(matrixString)) {
      int p = 0;
      final int length = matrixString.length();
      while (p < length) {
        if (charAtIs(matrixString, p, '=')) {
          throw new UriBuilderException("Matrix parameter name is empty");
        }

        int n = scan(matrixString, p, ';', length);

        if (n > p) {
          final String pair = matrixString.substring(p, n);
          final int eq = scan(pair, 0, '=', pair.length());
          if (eq == pair.length() || eq == (pair.length() - 1)) {
            matrixParam(false, pair);
          } else {
            matrixParam(false, pair.substring(0, eq), pair.substring(eq + 1));
          }
        }
        p = n + 1;
      }
    }
  }

  private int findStartOfMatrixParams(String path) {
    final int pathLength = path.length();
    int semicolonPos = -1;
    if (pathLength > 0) {
      int startLookUpFrom;
      if (charAtIs(path, pathLength - 1, '/')) {
        startLookUpFrom = pathLength - 2;
      } else {
        startLookUpFrom = pathLength - 1;
      }
      for (int i = startLookUpFrom; i >= 0 && !charAtIs(path, i, '/'); i--) {
        if (charAtIs(path, i, ';')) {
          semicolonPos = i;
        }
      }
    }
    return semicolonPos;
  }

  @Override
  public UriBuilder replacePath(String path) {
    this.path.setLength(0);
    if (!isNullOrEmpty(path)) {
      path(path);
    }
    return this;
  }

  @Override
  public UriBuilder replaceQueryParam(String name, Object... values) {
    checkArgument(name != null, "Null name isn't allowed");

    if (query.length() > 0) {
      int p = 0;
      final String queryString = query.toString();
      final int queryStringLength = queryString.length();
      query.setLength(0);
      name = recognizeEncode(name, QUERY, true);
      while (p < queryStringLength) {
        int n = scan(queryString, p, '&', queryStringLength);

        // Do nothing for sequence such as '&&'.
        if (n > p) {
          String pair = queryString.substring(p, n);
          int eq = scan(pair, 0, '=', pair.length());
          String pairName = eq == pair.length() ? pair : pair.substring(0, eq);

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

    if (values != null && values.length > 0) {
      queryParam(name, values);
    }

    return this;
  }

  @Override
  public UriBuilder replaceQuery(String queryString) {
    query.setLength(0);
    if (!isNullOrEmpty(queryString)) {
      validateQueryString(queryString);
      query.append(recognizeEncode(queryString, QUERY_STRING, true));
    }
    return this;
  }

  private void validateQueryString(String queryString) {
    int p = 0;
    while (p < queryString.length()) {
      if (charAtIs(queryString, p, '=')) {
        // something like a=x&=y
        throw new IllegalArgumentException(
            String.format("Invalid query string at %d. Query parameter name is empty.", p));
      }
      int n = scan(queryString, p, '&', queryString.length());
      p = n + 1;
    }
  }

  @Override
  public UriBuilder scheme(String schema) {
    this.schema = schema != null ? validateUriComponent(schema, SCHEME, true) : null;
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(String ssp) {
    checkArgument(ssp != null, "Null scheme specific part isn't allowed");

    this.ssp = recognizeEncode(ssp, SSP, true);
    authority = null;
    userInfo = null;
    host = null;
    port = null;
    path.setLength(0);

    return this;
  }

  @Override
  public UriBuilder segment(String... segments) {
    checkArgument(segments != null, "Null path segments aren't allowed");

    for (String segment : segments) {
      checkArgument(segment != null, "Null path segment isn't allowed");
      path(recognizeEncode(segment, PATH_SEGMENT, true));
    }

    return this;
  }

  @Override
  public UriBuilder uri(URI uri) {
    checkArgument(uri != null, "Null URI isn't allowed");

    if (uri.getScheme() != null) {
      schema = uri.getScheme();
    }

    if (uri.isOpaque()) {
      ssp = uri.getRawSchemeSpecificPart();
    } else {
      if (uri.getRawUserInfo() == null && uri.getHost() == null && uri.getPort() == -1) {
        if (uri.getRawAuthority() != null) {
          authority = uri.getRawAuthority();
        }
      } else {
        if (uri.getRawUserInfo() != null) {
          userInfo = uri.getRawUserInfo();
        }

        if (uri.getHost() != null) {
          host = uri.getHost();
        }

        if (uri.getPort() != -1) {
          port = Integer.toString(uri.getPort());
        }
      }

      if (!isNullOrEmpty(uri.getRawPath())) {
        path.setLength(0);
        path.append(uri.getRawPath());
      }

      if (!isNullOrEmpty(uri.getRawQuery())) {
        query.setLength(0);
        query.append(uri.getRawQuery());
      }

      if (uri.getRawFragment() != null) {
        fragment = uri.getRawFragment();
      }
    }

    return this;
  }

  @Override
  public UriBuilder uri(String uriTemplate) {
    checkArgument(uriTemplate != null, "Null URI template isn't allowed");
    parseTemplate(uriTemplate);
    return this;
  }

  @Override
  public UriBuilder userInfo(String userInfo) {
    this.userInfo = userInfo != null ? recognizeEncode(userInfo, USER_INFO, true) : null;
    return this;
  }

  /**
   * @param sb the StringBuilder for appending URI part
   * @param str URI part
   * @param component the URI component
   * @param values values map
   * @param encode if true then encode value before add it in URI, otherwise value must be validate
   *     to legal characters
   * @param asTemplate if true ignore absence value for any URI parameters
   */
  private void appendUriPart(
      StringBuilder sb,
      String str,
      int component,
      Map<String, ?> values,
      boolean encode,
      boolean asTemplate) {
    int p = 0;
    int n = 0;
    int length = str.length();
    while (p < length) {
      p = findStartOfUriTemplate(str, n);
      if (p > n) {
        sb.append(str, n, p);
      }
      if (charAtIs(str, p, '{')) {
        n = findEndOfUriTemplate(str, p);
        if (charAtIs(str, n, '}')) {
          String paramName = extractNameOfUriTemplate(str, p, n);
          Object paramValue = values.get(paramName);
          if (paramValue == null) {
            if (!asTemplate) {
              throw new IllegalArgumentException(
                  String.format("Not found corresponding value for parameter %s", paramName));
            }
            sb.append('{').append(paramName).append('}');
          } else {
            sb.append(
                encode
                    ? encode(paramValue.toString(), component, asTemplate)
                    : recognizeEncode(paramValue.toString(), component, asTemplate));
          }
        }
      }
      n++;
    }
  }

  /**
   * @param sb the StringBuilder for appending URI part
   * @param str URI part
   * @param component the URI component
   * @param sourceValues the source array of values
   * @param offset the offset in array
   * @param values values map, keep parameter/value pair which have been already found. From java
   *     docs:
   *     <p>All instances of the same template parameter will be replaced by the same value that
   *     corresponds to the position of the first instance of the template parameter. e.g. the
   *     template "{a}/{b}/{a}" with values {"x", "y", "z"} will result in the the URI "x/y/x",
   *     <i>not</i> "x/y/z".
   * @param encode if true then encode value before add it in URI, otherwise value must be validate
   *     to legal characters
   * @param asTemplate if true ignore absence value for any URI parameters
   * @return offset
   */
  private int appendUriPart(
      StringBuilder sb,
      String str,
      int component,
      Object[] sourceValues,
      int offset,
      Map<String, String> values,
      boolean encode,
      boolean asTemplate) {
    int p = 0;
    int n = 0;
    int length = str.length();
    while (p < length) {
      p = findStartOfUriTemplate(str, n);
      if (p > n) {
        sb.append(str, n, p);
      }
      if (charAtIs(str, p, '{')) {
        n = findEndOfUriTemplate(str, p);
        if (charAtIs(str, n, '}')) {
          String paramName = extractNameOfUriTemplate(str, p, n);
          String processedParamValue = values.get(paramName);
          if (processedParamValue != null) {
            sb.append(processedParamValue);
          } else {
            Object newParamValue = null;
            if (offset < sourceValues.length) {
              newParamValue = sourceValues[offset++];
            }

            if (newParamValue != null) {
              processedParamValue =
                  encode
                      ? encode(newParamValue.toString(), component, asTemplate)
                      : recognizeEncode(newParamValue.toString(), component, asTemplate);
              values.put(paramName, processedParamValue);
              sb.append(processedParamValue);
            } else {
              if (!asTemplate) {
                throw new IllegalArgumentException(
                    String.format("Not found corresponding value for parameter %s", paramName));
              }
              sb.append('{').append(paramName).append('}');
            }
          }
        }
      }
      n++;
    }
    return offset;
  }

  private int findStartOfUriTemplate(String str, int startFrom) {
    return scan(str, startFrom, '{', str.length());
  }

  private int findEndOfUriTemplate(String str, int startFrom) {
    return scan(str, startFrom, '}', str.length());
  }

  private String extractNameOfUriTemplate(String str, int startOfTemplate, int endOfTemplate) {
    int templateRegexSeparator = scan(str, startOfTemplate, ':', endOfTemplate);
    String name;
    if (templateRegexSeparator < endOfTemplate) {
      name = str.substring(startOfTemplate + 1, templateRegexSeparator);
    } else {
      name = str.substring(startOfTemplate + 1, endOfTemplate);
    }
    return name.trim();
  }

  /**
   * Create URI from URI part. Each URI part can contains templates.
   *
   * @param schema the schema URI part
   * @param userInfo the user info URI part
   * @param host the host name URI part
   * @param port the port number URI part
   * @param path the path URI part
   * @param query the query string URI part
   * @param fragment the fragment URI part
   * @param values the values which must be used instead templates parameters
   * @param encode if true then encode value before add it in URI, otherwise value must be validate
   *     to legal characters
   * @param asTemplate if true ignore absence value for any URI parameters
   * @return the URI string
   */
  private String createUriWithValues(
      String schema,
      String authority,
      String userInfo,
      String host,
      String port,
      String path,
      String query,
      String fragment,
      Map<String, ?> values,
      boolean encode,
      boolean encodeSlashInPath,
      boolean asTemplate) {
    StringBuilder sb = new StringBuilder();
    if (schema != null) {
      appendUriPart(sb, schema, SCHEME, values, false, asTemplate);
      sb.append(':');
    }
    if (authority != null || userInfo != null || host != null || port != null) {
      sb.append('/');
      sb.append('/');

      if (userInfo == null && host == null && port == null) {
        sb.append(authority);
      } else {

        if (!isNullOrEmpty(userInfo)) {
          appendUriPart(sb, userInfo, USER_INFO, values, encode, asTemplate);
          sb.append('@');
        }
        if (host != null) {
          appendUriPart(sb, host, HOST, values, encode, asTemplate);
        }

        if (port != null) {
          sb.append(':');
          appendUriPart(sb, port, PORT, values, encode, asTemplate);
        }
      }
    }

    if (!isNullOrEmpty(path)) {
      if (sb.length() > 0 && !charAtIs(path, 0, '/')) {
        sb.append('/');
      }
      appendUriPart(sb, path, encodeSlashInPath ? PATH_SEGMENT : PATH, values, encode, asTemplate);
    }

    if (!isNullOrEmpty(query)) {
      sb.append('?');
      appendUriPart(sb, query, QUERY, values, encode, asTemplate);
    }

    if (!isNullOrEmpty(fragment)) {
      sb.append('#');
      appendUriPart(sb, fragment, FRAGMENT, values, encode, asTemplate);
    }

    return sb.toString();
  }

  /**
   * Create URI from URI part. Each URI part can contains templates.
   *
   * @param schema the schema URI part
   * @param authority the authority
   * @param userInfo the user info URI part
   * @param host the host name URI part
   * @param port the port number URI part
   * @param path the path URI part
   * @param query the query string URI part
   * @param fragment the fragment URI part
   * @param values the values which must be used instead templates parameters
   * @param encode if true then encode value before add it in URI, otherwise value must be validate
   *     to legal characters
   * @param asTemplate if true ignore absence value for any URI parameters
   * @return the URI string
   */
  private String createUriWithValues(
      String schema,
      String authority,
      String userInfo,
      String host,
      String port,
      String path,
      String query,
      String fragment,
      Object[] values,
      boolean encode,
      boolean encodeSlashInPath,
      boolean asTemplate) {
    Map<String, String> m = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    int p = 0;

    if (schema != null) {
      p = appendUriPart(sb, schema, SCHEME, values, p, m, false, asTemplate);
      sb.append(':');
    }

    if (authority != null || !isNullOrEmpty(userInfo) || host != null || port != null) {
      sb.append('/');
      sb.append('/');

      if (!isNullOrEmpty(userInfo) || host != null || port != null) {
        if (!isNullOrEmpty(userInfo)) {
          p = appendUriPart(sb, userInfo, USER_INFO, values, p, m, encode, asTemplate);
          sb.append('@');
        }

        if (host != null) {
          p = appendUriPart(sb, host, HOST, values, p, m, encode, asTemplate);
        }

        if (port != null) {
          sb.append(':');
          p = appendUriPart(sb, port, PORT, values, p, m, encode, asTemplate);
        }
      } else {
        sb.append(authority);
      }
    }

    if (!isNullOrEmpty(path)) {
      if (host != null && sb.length() > 0 && !charAtIs(path, 0, '/')) {
        sb.append('/');
      }
      p =
          appendUriPart(
              sb, path, encodeSlashInPath ? PATH_SEGMENT : PATH, values, p, m, encode, asTemplate);
    }

    if (!isNullOrEmpty(query)) {
      sb.append('?');
      p = appendUriPart(sb, query, QUERY, values, p, m, encode, asTemplate);
    }

    if (!isNullOrEmpty(fragment)) {
      sb.append('#');
      appendUriPart(sb, fragment, FRAGMENT, values, p, m, encode, asTemplate);
    }

    return sb.toString();
  }
}
