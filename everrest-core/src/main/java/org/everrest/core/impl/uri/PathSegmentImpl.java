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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.everrest.core.impl.uri.UriComponent.PATH_SEGMENT;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

import com.google.common.base.MoreObjects;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import java.util.Objects;
import org.everrest.core.impl.MultivaluedMapImpl;

public final class PathSegmentImpl implements PathSegment {
  /** Path. */
  private final String path;

  /** Matrix parameters. */
  private final MultivaluedMap<String, String> matrixParameters;

  /**
   * @param path Path
   * @param matrixParameters Matrix parameters
   */
  PathSegmentImpl(String path, MultivaluedMap<String, String> matrixParameters) {
    this.path = path;
    this.matrixParameters = matrixParameters;
  }

  /**
   * Create instance of PathSegment from given string.
   *
   * @param pathSegment string which represents PathSegment
   * @param decode true if character must be decoded false otherwise
   * @return instance of PathSegment
   */
  public static PathSegment fromString(String pathSegment, boolean decode) {
    String path = "";
    MultivaluedMap<String, String> matrixParameters = new MultivaluedMapImpl();
    if (isNullOrEmpty(pathSegment)) {
      return new PathSegmentImpl(path, matrixParameters);
    }

    int p, n, k;

    p = scan(pathSegment, ';');

    boolean hasMatrixParameters = charAtIs(pathSegment, p, ';');
    if (hasMatrixParameters) {
      if (p > 0) {
        path = pathSegment.substring(0, p);
      }
    } else {
      path = pathSegment;
    }

    if (decode) {
      path = UriComponent.decode(path, PATH_SEGMENT);
    }

    if (!hasMatrixParameters) {
      return new PathSegmentImpl(path, matrixParameters);
    }

    ++p;
    int length = pathSegment.length();
    while (p < length) {
      n = scan(pathSegment, p, ';');
      String name;
      String value = "";
      k = scan(pathSegment, p, '=', n);
      if (charAtIs(pathSegment, k, '=')) {
        name = pathSegment.substring(p, k);
        value = pathSegment.substring(k + 1, n);
      } else {
        name = pathSegment.substring(p, n);
      }
      if (!name.isEmpty()) {
        matrixParameters.add(
            UriComponent.decode(name, PATH_SEGMENT),
            decode ? UriComponent.decode(value, PATH_SEGMENT) : value);
      }
      p = n + 1;
    }
    return new PathSegmentImpl(path, matrixParameters);
  }

  @Override
  public MultivaluedMap<String, String> getMatrixParameters() {
    return matrixParameters;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PathSegmentImpl)) {
      return false;
    }

    PathSegmentImpl other = (PathSegmentImpl) o;
    return Objects.equals(path, other.path)
        && Objects.equals(matrixParameters, other.matrixParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, matrixParameters);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("path", path)
        .add("matrixParameters", matrixParameters)
        .toString();
  }
}
