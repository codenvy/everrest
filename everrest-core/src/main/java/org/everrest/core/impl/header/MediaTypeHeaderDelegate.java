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

import static org.everrest.core.impl.header.HeaderHelper.appendWithQuote;
import static org.everrest.core.impl.header.HeaderHelper.removeWhitespaces;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.charAtIsNot;
import static org.everrest.core.util.StringUtils.scan;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

/** @author andrew00x */
public class MediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate<MediaType> {
  private static final char SUB_TYPE_SEPARATOR = '/';
  private static final char PARAMS_SEPARATOR = ';';

  @Override
  public MediaType fromString(String header) {
    if (header == null) {
      throw new IllegalArgumentException();
    }

    try {
      int subTypeSeparatorIndex = scan(header, SUB_TYPE_SEPARATOR);
      int paramsSeparatorIndex = scan(header, PARAMS_SEPARATOR);

      String type;
      String subType;

      if (charAtIsNot(header, subTypeSeparatorIndex, SUB_TYPE_SEPARATOR)
          && charAtIsNot(header, paramsSeparatorIndex, PARAMS_SEPARATOR)) {

        return new MediaType(header, null);

      } else if (charAtIs(header, subTypeSeparatorIndex, SUB_TYPE_SEPARATOR)
          && charAtIsNot(header, paramsSeparatorIndex, PARAMS_SEPARATOR)) {

        return new MediaType(
            removeWhitespaces(header.substring(0, subTypeSeparatorIndex)),
            removeWhitespaces(header.substring(subTypeSeparatorIndex + 1)));

      } else if (charAtIsNot(header, subTypeSeparatorIndex, SUB_TYPE_SEPARATOR)
          && paramsSeparatorIndex == 0) {

        // string just start from ';'
        type = null;
        subType = null;

      } else if (charAtIsNot(header, subTypeSeparatorIndex, SUB_TYPE_SEPARATOR)
          && charAtIs(header, paramsSeparatorIndex, PARAMS_SEPARATOR)) {

        // there is no '/' but present ';'
        type = removeWhitespaces(header.substring(0, paramsSeparatorIndex));
        subType = null;

      } else {

        type = removeWhitespaces(header.substring(0, subTypeSeparatorIndex));
        subType = header.substring(subTypeSeparatorIndex + 1, paramsSeparatorIndex);
      }

      Map<String, String> params = new HeaderParameterParser().parse(header);
      return new MediaType(type, subType, params);

    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String toString(MediaType mime) {
    if (mime == null) {
      throw new IllegalArgumentException();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mime.getType()).append('/').append(mime.getSubtype());

    for (Entry<String, String> entry : mime.getParameters().entrySet()) {
      sb.append(';').append(entry.getKey()).append('=');
      appendWithQuote(sb, entry.getValue());
    }

    return sb.toString();
  }
}
