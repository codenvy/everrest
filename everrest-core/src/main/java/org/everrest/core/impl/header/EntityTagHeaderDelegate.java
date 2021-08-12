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

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.ext.RuntimeDelegate;

/** @author andrew00x */
public class EntityTagHeaderDelegate implements RuntimeDelegate.HeaderDelegate<EntityTag> {

  @Override
  public EntityTag fromString(String header) {
    if (header == null) {
      throw new IllegalArgumentException();
    }

    boolean isWeak = header.startsWith("W/");

    String value;
    if (isWeak) {
      value = cutWeakPrefix(header);
    } else {
      value = header;
    }
    value = value.substring(1, value.length() - 1);
    value = HeaderHelper.removeQuoteEscapes(value);

    return new EntityTag(value, isWeak);
  }

  private String cutWeakPrefix(String header) {
    return header.substring(2);
  }

  @Override
  public String toString(EntityTag entityTag) {
    if (entityTag == null) {
      throw new IllegalArgumentException();
    }
    StringBuilder sb = new StringBuilder();
    if (entityTag.isWeak()) {
      sb.append('W').append('/');
    }

    sb.append('"');
    HeaderHelper.appendEscapeQuote(sb, entityTag.getValue());
    sb.append('"');

    return sb.toString();
  }
}
