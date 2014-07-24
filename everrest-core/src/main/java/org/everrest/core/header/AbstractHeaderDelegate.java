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
package org.everrest.core.header;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * @param <T>
 *         Java type for representing HTTP header
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AbstractHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public abstract class AbstractHeaderDelegate<T> implements HeaderDelegate<T> {

    /** @return the class which is supported by HeaderDelegate instance. */
    public abstract Class<T> support();

}
