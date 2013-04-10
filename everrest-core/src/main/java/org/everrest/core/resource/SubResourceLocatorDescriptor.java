/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.resource;

import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;

/**
 * Describe sub-resource locator. Sub-resource locator is
 * {@link java.lang.reflect.Method} of resource class which has own {@link Path}
 * annotation and has not {@link javax.ws.rs.HttpMethod} annotation. This method
 * can't handle request by self but produce object and this object can handle
 * request or maybe has other resource locators.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SubResourceLocatorDescriptor.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public interface SubResourceLocatorDescriptor extends GenericMethodResource, ResourceDescriptor {

    /** @return See {@link PathValue} */
    PathValue getPathValue();

    /** @return See {@link UriPattern} */
    UriPattern getUriPattern();

}
