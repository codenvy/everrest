/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform.container;

import org.exoplatform.container.ExoContainerContext;

import javax.inject.Provider;

/**
 * Provider may be used to get instance of RestfulContainer. It can be used for:
 * <ul>
 * <li>lazy initialization of RestfulContainer</li>
 * <li>if have circular dependency problem</li>
 * </ul>
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RestfulContainerProvider implements Provider<RestfulContainer> {
    @Override
    public RestfulContainer get() {
        return (RestfulContainer)ExoContainerContext.getCurrentContainer().getComponentInstance("everrest");
    }
}
