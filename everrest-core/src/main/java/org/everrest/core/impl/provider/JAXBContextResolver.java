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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provide cache for {@link JAXBContext}.
 *
 * @author andrew00x
 */
@Provider
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBContextResolver implements ContextResolver<JAXBContextResolver> {
  /** JAXBContext cache. */
  private final ConcurrentMap<Class, JAXBContext> jaxbContexts = new ConcurrentHashMap<>();

  @Override
  public JAXBContextResolver getContext(Class<?> type) {
    return this;
  }

  /**
   * Return JAXBContext according to supplied type. If no one context found then try create new
   * context and save it in cache.
   *
   * @param aClass class to be bound
   * @return JAXBContext
   * @throws JAXBException if JAXBContext creation failed
   */
  public JAXBContext getJAXBContext(Class<?> aClass) throws JAXBException {
    JAXBContext jaxbContext = jaxbContexts.get(aClass);
    if (jaxbContext == null) {
      jaxbContexts.putIfAbsent(aClass, JAXBContext.newInstance(aClass));
    }
    return jaxbContexts.get(aClass);
  }

  /**
   * Add prepared JAXBContext that will be mapped to set of class. In this case this class works as
   * cache for JAXBContexts.
   *
   * @param jaxbContext JAXBContext
   * @param aClass java classes to be bound
   */
  public void addJAXBContext(JAXBContext jaxbContext, Class<?> aClass) {
    jaxbContexts.put(aClass, jaxbContext);
  }

  public void removeJAXBContext(Class<?> aClass) {
    jaxbContexts.remove(aClass);
  }
}
