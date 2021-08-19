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

import jakarta.ws.rs.core.CacheControl;
import java.util.List;
import java.util.Map;

public class CacheControlBuilder {
  public static CacheControlBuilder aCacheControl() {
    return new CacheControlBuilder();
  }

  private boolean privateFlag;
  private List<String> privateFields;
  private boolean noCache;
  private List<String> noCacheFields;
  private boolean noStore;
  private boolean noTransform;
  private boolean mustRevalidate;
  private boolean proxyRevalidate;
  private Map<String, String> cacheExtension;
  private int maxAge = -1;
  private int sMaxAge = -1;

  public CacheControlBuilder withMaxAge(int maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  public CacheControlBuilder withMustRevalidate(boolean mustRevalidate) {
    this.mustRevalidate = mustRevalidate;
    return this;
  }

  public CacheControlBuilder withNoCache(boolean noCache) {
    this.noCache = noCache;
    return this;
  }

  public CacheControlBuilder withNoStore(boolean noStore) {
    this.noStore = noStore;
    return this;
  }

  public CacheControlBuilder withNoTransform(boolean noTransform) {
    this.noTransform = noTransform;
    return this;
  }

  public CacheControlBuilder withPrivate(boolean flag) {
    privateFlag = flag;
    return this;
  }

  public CacheControlBuilder withProxyRevalidate(boolean proxyRevalidate) {
    this.proxyRevalidate = proxyRevalidate;
    return this;
  }

  public CacheControlBuilder withSMaxAge(int sMaxAge) {
    this.sMaxAge = sMaxAge;
    return this;
  }

  public CacheControlBuilder withPrivateFields(List<String> privateFields) {
    this.privateFields = privateFields;
    return this;
  }

  public CacheControlBuilder withNoCacheFields(List<String> noCacheFields) {
    this.noCacheFields = noCacheFields;
    return this;
  }

  public CacheControlBuilder withCacheExtension(Map<String, String> cacheExtension) {
    this.cacheExtension = cacheExtension;
    return this;
  }

  public CacheControl build() {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMustRevalidate(mustRevalidate);
    cacheControl.setProxyRevalidate(proxyRevalidate);
    cacheControl.setMaxAge(maxAge);
    cacheControl.setSMaxAge(sMaxAge);
    cacheControl.setNoCache(noCache);
    cacheControl.setPrivate(privateFlag);
    cacheControl.setNoTransform(noTransform);
    cacheControl.setNoStore(noStore);
    if (privateFields != null) {
      cacheControl.getPrivateFields().addAll(privateFields);
    }
    if (noCacheFields != null) {
      cacheControl.getNoCacheFields().addAll(noCacheFields);
    }
    if (cacheExtension != null) {
      cacheControl.getCacheExtension().putAll(cacheExtension);
    }
    return cacheControl;
  }
}
