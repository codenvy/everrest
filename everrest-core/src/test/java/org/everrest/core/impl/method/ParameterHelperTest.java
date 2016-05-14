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
package org.everrest.core.impl.method;

import org.everrest.core.Property;
import org.junit.Test;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ParameterHelperTest {
    @Test
    public void annotationsThatMightBeAppliedToProviderFields() {
        assertEquals(2, ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS.size());
        assertThat(ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS,
                   hasItems(Context.class.getName(), Property.class.getName()));
    }

    @Test
    public void annotationsThatMightBeAppliedToProviderConstructorParameters() {
        assertEquals(2, ParameterHelper.PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS.size());
        assertThat(ParameterHelper.PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS,
                   hasItems(Context.class.getName(), Property.class.getName()));
    }

    @Test
    public void annotationsThatMightBeAppliedToResourceFields() {
        assertEquals(7, ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS.size());
        assertThat(ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS,
                   hasItems(CookieParam.class.getName(), Context.class.getName(), Property.class.getName(),
                            QueryParam.class.getName(), PathParam.class.getName(), MatrixParam.class.getName(),
                            HeaderParam.class.getName())
                  );
    }

    @Test
    public void annotationsThatMightBeAppliedToResourceConstructorParameters() {
        assertEquals(7, ParameterHelper.RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS.size());
        assertThat(ParameterHelper.RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS,
                   hasItems(CookieParam.class.getName(), Context.class.getName(), Property.class.getName(),
                            QueryParam.class.getName(), PathParam.class.getName(), MatrixParam.class.getName(),
                            HeaderParam.class.getName())
                  );
    }

    @Test
    public void annotationsThatMightBeAppliedToResourceMethodParameters() {
        assertEquals(8, ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS.size());
        assertThat(ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS,
                   hasItems(CookieParam.class.getName(), Context.class.getName(), Property.class.getName(),
                            QueryParam.class.getName(), PathParam.class.getName(), MatrixParam.class.getName(),
                            HeaderParam.class.getName(), FormParam.class.getName())
                  );
    }
}