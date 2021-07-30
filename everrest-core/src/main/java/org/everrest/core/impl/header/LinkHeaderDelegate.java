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

import javax.ws.rs.core.Link;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import org.everrest.core.impl.uri.LinkBuilderImpl;

public class LinkHeaderDelegate implements HeaderDelegate<Link> {
  @Override
  public Link fromString(String value) {
    return new LinkBuilderImpl().link(value).build();
  }

  @Override
  public String toString(Link value) {
    return value.toString();
  }
}
