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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ScriptFinderFactory {

    private static final Map<String, ScriptFinder> all = new ConcurrentHashMap<String, ScriptFinder>();

    static {
        all.put("file", new FileSystemScriptFinder());
    }

    public static ScriptFinder getScriptFinder(String protocol) {
        return all.get(protocol);
    }

    public static void addScriptFilder(String protocol, ScriptFinder finder) {
        all.put(protocol, finder);
    }

    private ScriptFinderFactory() {
    }

}
