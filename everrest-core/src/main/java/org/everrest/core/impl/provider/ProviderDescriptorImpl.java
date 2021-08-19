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
package org.everrest.core.impl.provider;

import static org.everrest.core.impl.header.MediaTypeHelper.createConsumesList;
import static org.everrest.core.impl.header.MediaTypeHelper.createProducesList;

import com.google.common.base.MoreObjects;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.everrest.core.BaseObjectModel;
import org.everrest.core.provider.ProviderDescriptor;

/** @author andrew00x */
public class ProviderDescriptorImpl extends BaseObjectModel implements ProviderDescriptor {
  /** List of media types which this method can consume. See {@link jakarta.ws.rs.Consumes} */
  private final List<MediaType> consumes;

  /** List of media types which this method can produce. See {@link jakarta.ws.rs.Produces} */
  private final List<MediaType> produces;

  public ProviderDescriptorImpl(Class<?> providerClass) {
    super(providerClass);
    this.consumes = createConsumesList(providerClass.getAnnotation(Consumes.class));
    this.produces = createProducesList(providerClass.getAnnotation(Produces.class));
  }

  public ProviderDescriptorImpl(Object provider) {
    super(provider);
    final Class<?> providerClass = provider.getClass();
    this.consumes = createConsumesList(providerClass.getAnnotation(Consumes.class));
    this.produces = createProducesList(providerClass.getAnnotation(Produces.class));
  }

  @Override
  public List<MediaType> consumes() {
    return consumes;
  }

  @Override
  public List<MediaType> produces() {
    return produces;
  }

  public String toString() {
    return MoreObjects.toStringHelper(ProviderDescriptorImpl.class)
        .add("provider class", clazz)
        .add("produces media type", produces.isEmpty() ? null : produces)
        .add("consumes media type", consumes.isEmpty() ? null : consumes)
        .addValue(constructors.isEmpty() ? null : constructors)
        .addValue(fields.isEmpty() ? null : fields)
        .omitNullValues()
        .toString();
  }
}
