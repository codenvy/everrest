/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide cache for {@link JAXBContext}.
 *
 * @author andrew00x
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
@Produces({MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML, "text/*+xml"})
public class JAXBContextResolver implements ContextResolver<JAXBContextResolver> {
    /** JAXBContext cache. */
    private final ConcurrentHashMap<Class, JAXBContext> jaxbContexts = new ConcurrentHashMap<>();

    @Override
    public JAXBContextResolver getContext(Class<?> type) {
        return this;
    }

    /**
     * Return JAXBContext according to supplied type. If no one context found then try create new context and save it in cache.
     *
     * @param clazz
     *         class to be bound
     * @return JAXBContext
     * @throws JAXBException
     *         if JAXBContext creation failed
     */
    public JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(clazz);
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(clazz);
            jaxbContexts.put(clazz, jaxbContext);
        }
        return jaxbContext;
    }

    /**
     * Create and add in cache JAXBContext for supplied set of classes.
     *
     * @param clazz
     *         java class to be bound
     * @return JAXBContext
     * @throws JAXBException
     *         if JAXBContext for supplied classes can't be created in any reasons
     */
    public JAXBContext createJAXBContext(Class<?> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        addJAXBContext(jaxbContext, clazz);
        return jaxbContext;
    }

    /**
     * Add prepared JAXBContext that will be mapped to set of class. In this case this class works as cache for JAXBContexts.
     *
     * @param jaxbContext
     *         JAXBContext
     * @param clazz
     *         java classes to be bound
     */
    public void addJAXBContext(JAXBContext jaxbContext, Class<?> clazz) {
        jaxbContexts.put(clazz, jaxbContext);
    }
}
