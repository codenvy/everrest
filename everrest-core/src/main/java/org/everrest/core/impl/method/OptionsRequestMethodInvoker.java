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

import javax.ws.rs.core.Response;
import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.wadl.WadlProcessor;
import org.everrest.core.wadl.research.Application;

public class OptionsRequestMethodInvoker implements MethodInvoker {
  private WadlProcessor wadlProcessor;

  public OptionsRequestMethodInvoker(WadlProcessor wadlProcessor) {
    this.wadlProcessor = wadlProcessor;
  }

  @Override
  public Object invokeMethod(
      Object resource, GenericResourceMethod genericResourceMethod, ApplicationContext context) {
    Application wadlApplication =
        wadlProcessor.process(genericResourceMethod.getParentResource(), context.getBaseUri());
    return Response.ok(wadlApplication, MediaTypeHelper.WADL_TYPE).build();
  }
}
