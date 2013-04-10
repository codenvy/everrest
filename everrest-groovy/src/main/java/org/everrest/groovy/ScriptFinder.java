/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.groovy;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Look up script files.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ScriptFinder {

    /**
     * Find all scripts that are acceptable by <code>filter</code> in specified
     * root URL.
     *
     * @param filter
     *         URL filter
     * @param root
     *         root URL from which look up should be stated
     * @return URLs of all resources that were found, may return whether
     *         <code>null</code> or empty array if scripts were not found
     * @throws MalformedURLException
     *         if the URL is invalid
     */
    URL[] find(URLFilter filter, URL root) throws MalformedURLException;

}