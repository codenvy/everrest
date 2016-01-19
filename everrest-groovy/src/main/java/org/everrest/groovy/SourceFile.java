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
