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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import org.everrest.core.Property;
import org.junit.Test;

public class ParameterHelperTest {
  @Test
  public void annotationsThatMightBeAppliedToProviderFields() {
    assertEquals(2, ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS.size());
    assertThat(
        ParameterHelper.PROVIDER_FIELDS_ANNOTATIONS,
        hasItems(Context.class.getName(), Property.class.getName()));
  }

  @Test
  public void annotationsThatMightBeAppliedToProviderConstructorParameters() {
    assertEquals(2, ParameterHelper.PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS.size());
    assertThat(
        ParameterHelper.PROVIDER_CONSTRUCTOR_PARAMETER_ANNOTATIONS,
        hasItems(Context.class.getName(), Property.class.getName()));
  }

  @Test
  public void annotationsThatMightBeAppliedToResourceFields() {
    assertEquals(7, ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS.size());
    assertThat(
        ParameterHelper.RESOURCE_FIELDS_ANNOTATIONS,
        hasItems(
            CookieParam.class.getName(),
            Context.class.getName(),
            Property.class.getName(),
            QueryParam.class.getName(),
            PathParam.class.getName(),
            MatrixParam.class.getName(),
            HeaderParam.class.getName()));
  }

  @Test
  public void annotationsThatMightBeAppliedToResourceConstructorParameters() {
    assertEquals(7, ParameterHelper.RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS.size());
    assertThat(
        ParameterHelper.RESOURCE_CONSTRUCTOR_PARAMETER_ANNOTATIONS,
        hasItems(
            CookieParam.class.getName(),
            Context.class.getName(),
            Property.class.getName(),
            QueryParam.class.getName(),
            PathParam.class.getName(),
            MatrixParam.class.getName(),
            HeaderParam.class.getName()));
  }

  @Test
  public void annotationsThatMightBeAppliedToResourceMethodParameters() {
    assertEquals(8, ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS.size());
    assertThat(
        ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS,
        hasItems(
            CookieParam.class.getName(),
            Context.class.getName(),
            Property.class.getName(),
            QueryParam.class.getName(),
            PathParam.class.getName(),
            MatrixParam.class.getName(),
            HeaderParam.class.getName(),
            FormParam.class.getName()));
  }
}
