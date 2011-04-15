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

import java.net.URL;

/**
 * URL's filter.
 * <p>
 * Instances of this interface may be passed to the
 * {@link ScriptFinder#find(URLFilter, URL)}.
 * </p>
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface URLFilter
{

   /**
    * Tests whether or not the specified URL should be included in URL list.
    *
    * @param url URL to be tested
    * @return <code>true</code> if specified URL must be include in result set
    *         of URLs and <code>false</code> otherwise
    */
   boolean accept(URL url);

}
