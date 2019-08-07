/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
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
 * @author andrew00x
 */
public interface URLFilter {
    /**
     * Tests whether or not the specified URL should be included in URL list.
     *
     * @param url
     *         URL to be tested
     * @return <code>true</code> if specified URL must be include in result set
     * of URLs and <code>false</code> otherwise
     */
    boolean accept(URL url);
}
