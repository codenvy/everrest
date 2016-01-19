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
package org.everrest.core.impl.header;

import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author andrew00x
 */
public class StringHeaderDelegate implements RuntimeDelegate.HeaderDelegate<String> {

    @Override
    public String fromString(String value) {
        return value;
    }


    @Override
    public String toString(String value) {
        return value;
    }
}
