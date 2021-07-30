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
package org.everrest.core.impl.provider.json;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.everrest.core.impl.provider.JsonEntityProvider;

/**
 * Prevent processing field/type via Json framework. When applied to class, it indicates that the
 * class should not be processed by {@link JsonEntityProvider}.
 *
 * @author andrew00x
 */
@Retention(RUNTIME)
@Target({FIELD, TYPE})
public @interface JsonTransient {}
