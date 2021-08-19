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

import static org.everrest.core.impl.header.HeaderHelper.parseQualityValue;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.util.Map;
import org.everrest.core.header.QualityValue;

public class AcceptMediaType implements QualityValue {
  /** Default accepted media type, it minds any content type is acceptable. */
  public static final AcceptMediaType DEFAULT = new AcceptMediaType("*", "*");

  /** Quality value for 'accepted' HTTP headers, e. g. text/plain;q=0.9 */
  private final float qValue;

  /**
   * Creates a new instance of AcceptedMediaType by parsing the supplied string.
   *
   * @param header accepted media type string
   * @return AcceptedMediaType
   */
  public static AcceptMediaType valueOf(String header) {
    return RuntimeDelegate.getInstance()
        .createHeaderDelegate(AcceptMediaType.class)
        .fromString(header);
  }

  private final MediaType mediaType;

  /**
   * Creates a new instance of MediaType, both type and sub-type are wildcards and set quality value
   * to default quality value.
   */
  public AcceptMediaType() {
    this(new MediaType());
  }

  /**
   * Constructs AcceptedMediaType with supplied quality value. If map parameters is null or does not
   * contain value with key 'q' then default quality value will be used.
   *
   * @param type media type
   * @param subtype media sub-type
   * @param parameters addition header parameters
   */
  public AcceptMediaType(String type, String subtype, Map<String, String> parameters) {
    this(new MediaType(type, subtype, parameters));
  }

  /**
   * Constructs AcceptedMediaType with default quality value.
   *
   * @param type media type
   * @param subtype media sub-type
   */
  public AcceptMediaType(String type, String subtype) {
    this(new MediaType(type, subtype));
  }

  public AcceptMediaType(MediaType mediaType) {
    this.mediaType = mediaType;
    if (mediaType.getParameters() != null && mediaType.getParameters().get(QVALUE) != null) {
      this.qValue = parseQualityValue(mediaType.getParameters().get(QVALUE));
    } else {
      this.qValue = DEFAULT_QUALITY_VALUE;
    }
  }

  public AcceptMediaType(MediaType mediaType, float qValue) {
    this.mediaType = mediaType;
    this.qValue = qValue;
  }

  public MediaType getMediaType() {
    return mediaType;
  }

  public String getType() {
    return mediaType.getType();
  }

  public boolean isWildcardType() {
    return mediaType.isWildcardType();
  }

  public String getSubtype() {
    return mediaType.getSubtype();
  }

  public boolean isWildcardSubtype() {
    return mediaType.isWildcardSubtype();
  }

  public Map<String, String> getParameters() {
    return mediaType.getParameters();
  }

  public boolean isCompatible(MediaType other) {
    return mediaType.isCompatible(other);
  }

  public boolean isCompatible(AcceptMediaType other) {
    return isCompatible(other.getMediaType());
  }

  @Override
  public float getQvalue() {
    return qValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AcceptMediaType)) {
      return false;
    }

    AcceptMediaType other = (AcceptMediaType) o;
    return Float.compare(other.qValue, qValue) == 0 && mediaType.equals(other.mediaType);
  }

  @Override
  public int hashCode() {
    int hashcode = 8;
    hashcode = hashcode * 31 + (qValue == 0.0F ? 0 : Float.floatToIntBits(qValue));
    hashcode = hashcode * 31 + mediaType.hashCode();
    return hashcode;
  }

  @Override
  public String toString() {
    return RuntimeDelegate.getInstance().createHeaderDelegate(AcceptMediaType.class).toString(this);
  }
}
