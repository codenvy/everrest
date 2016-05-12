/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
     * Return JAXBContext according to supplied type. If no one context found then try create new context and save it in cache.
     *
     * @param aClass
     *         class to be bound
     * @return JAXBContext
     * @throws JAXBException
     *         if JAXBContext creation failed
     */
    public JAXBContext getJAXBContext(Class<?> aClass) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(aClass);
        if (jaxbContext == null) {
            jaxbContexts.putIfAbsent(aClass, JAXBContext.newInstance(aClass));
        }
        return jaxbContexts.get(aClass);
    }

    /**
     * Add prepared JAXBContext that will be mapped to set of class. In this case this class works as cache for JAXBContexts.
     *
     * @param jaxbContext
     *         JAXBContext
     * @param aClass
     *         java classes to be bound
     */
    public void addJAXBContext(JAXBContext jaxbContext, Class<?> aClass) {
        jaxbContexts.put(aClass, jaxbContext);
    }

    public void removeJAXBContext(Class<?> aClass) {
        jaxbContexts.remove(aClass);
    }
}
