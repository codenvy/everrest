/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Abstraction of method's, constructor's parameter or object field.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface Parameter {

    /** @return addition annotation */
    Annotation[] getAnnotations();

    /**
     * @return <i>main</i> annotation. It mind this annotation describe which
     *         value will be used for initialize parameter, e. g.
     *         {@link javax.ws.rs.PathParam}, {@link javax.ws.rs.QueryParam},
     *         etc.
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
