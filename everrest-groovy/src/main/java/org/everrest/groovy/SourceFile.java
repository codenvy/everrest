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

import java.net.URL;

/**
 * Describe location of Groovy source file.
 *
 * @author andrew00x
 */
public class SourceFile extends ClassPathEntry {
    public SourceFile(URL path) {
        super(path);
    }
}
