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
package org.everrest.core.impl.method;

import org.everrest.core.Property;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParameterHelper {
    /**
     * Collections of annotation that allowed to be used on fields on any type of Provider.
     *
     * @see javax.ws.rs.ext.Provider
     * @see javax.ws.rs.ext.Providers
     */
    public static final List<String> PROVIDER_FIELDS_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on constructor's parameters of any type of Provider.
     *
     * @see javax.ws.rs.ext.Provider
     * @see javax.ws.rs.ext.Providers
     */
    public static final List<String> PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

    /**
     * Collections of annotation that allowed to be used on fields of resource class.
     */
    public static final List<String> RESOURCE_FIELDS_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on constructor's parameters of resource class.
     */
    public static final List<String> RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS;

    /**
     * Collections of annotation than allowed to be used on method's parameters of resource class.
     */
    public static final List<String> RESOURCE_METHOD_PARAMETER_ANNOTATIONS;

    static {
        PROVIDER_FIELDS_ANNOTATIONS =
                Collections.unmodifiableList(Arrays.asList(Context.class.getName(), Property.class.getName()));

        PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS =
                Collections.unmodifiableList(Arrays.asList(Context.class.getName(), Property.class.getName()));

        List<String> annotations = new ArrayList<>(7);
        annotations.add(CookieParam.class.getName());
        annotations.add(Context.class.getName());
        annotations.add(HeaderParam.class.getName());
        annotations.add(MatrixParam.class.getName());
        annotations.add(PathParam.class.getName());
        annotations.add(QueryParam.class.getName());
        annotations.add(Property.class.getName());
        RESOURCE_FIELDS_ANNOTATIONS = Collections.unmodifiableList(annotations);
        RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS = Collections.unmodifiableList(annotations);

        List<String> annotations2 = new ArrayList<>(annotations);
        annotations2.add(FormParam.class.getName());
        RESOURCE_METHOD_PARAMETER_ANNOTATIONS = Collections.unmodifiableList(annotations2);
    }
}
