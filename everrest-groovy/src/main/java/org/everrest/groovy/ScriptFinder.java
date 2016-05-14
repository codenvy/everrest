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
package org.everrest.groovy;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Look up script files.
 *
 * @author andrew00x
 */
public interface ScriptFinder {
    /**
     * Find all scripts that are acceptable by <code>filter</code> in specified root URL.
     *
     * @param filter
     *         URL filter
     * @param root
     *         root URL from which look up should be stated
     * @return URLs of all resources that were found, may return whether <code>null</code> or empty array if scripts were not found
     * @throws MalformedURLException
     *         if the URL is invalid
     */
    URL[] find(URLFilter filter, URL root) throws MalformedURLException;
}