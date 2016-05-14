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
package org.everrest.core;

import org.everrest.core.method.MethodInvokerFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an implementation of an extension interface. Filters may contain
 * {@link javax.ws.rs.Path} annotation for restriction filter scope. If
 * &#64;Path is not specified then filter will be applied for all requests
 * (RequestFilter), responses (ResponseFilter) or all resource methods
 * (MethodInvokerFilter).
 *
 * @author andrew00x
 * @see RequestFilter
 * @see ResponseFilter
 * @see MethodInvokerFilter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
}
