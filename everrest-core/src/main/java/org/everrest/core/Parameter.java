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
package org.everrest.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Abstraction of method's, constructor's parameter or object field.
 *
 * @author andrew00x
 */
public interface Parameter {

    /** @return addition annotation */
    Annotation[] getAnnotations();

    /**
     * @return <i>main</i> annotation. It mind this annotation describe which value will be used for initialize parameter, e. g. {@link
     * javax.ws.rs.PathParam}, {@link javax.ws.rs.QueryParam}, etc.
     */
    Annotation getAnnotation();

    /** @return true if parameter must not be decoded false otherwise */
    boolean isEncoded();

    /** @return default value for parameter */
    String getDefaultValue();

    /**
     * @return generic parameter type
     * @see java.lang.reflect.Method#getGenericParameterTypes()
     */
    Type getGenericType();

    /**
     * @return parameter class.
     * @see java.lang.reflect.Method#getParameterTypes()
     */
    Class<?> getParameterClass();
}
