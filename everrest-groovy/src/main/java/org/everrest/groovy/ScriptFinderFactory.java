/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andrew00x
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
