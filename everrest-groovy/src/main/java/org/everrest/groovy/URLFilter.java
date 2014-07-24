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
public interface URLFilter {

    /**
     * Tests whether or not the specified URL should be included in URL list.
     *
     * @param url
     *         URL to be tested
     * @return <code>true</code> if specified URL must be include in result set
     *         of URLs and <code>false</code> otherwise
     */
    boolean accept(URL url);

}
