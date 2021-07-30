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

import static org.everrest.core.header.QualityValue.QVALUE;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

public class AcceptMediaTypeHeaderDelegate
    implements RuntimeDelegate.HeaderDelegate<AcceptMediaType> {

  @Override
  public AcceptMediaType fromString(String header) {
    if (header == null) {
      throw new IllegalArgumentException();
    }
    return new AcceptMediaType(MediaType.valueOf(header));
  }

  @Override
  public String toString(AcceptMediaType acceptMediaType) {
    if (acceptMediaType == null) {
      throw new IllegalArgumentException();
    }
    final Map<String, String> parameters = acceptMediaType.getParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.containsKey(QVALUE))) {
      return acceptMediaType.getMediaType().toString();
    }
    final Map<String, String> copyParameters = new LinkedHashMap<>(parameters);
    copyParameters.remove(QVALUE);
    return new MediaType(acceptMediaType.getType(), acceptMediaType.getSubtype(), copyParameters)
        .toString();
  }
}
