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
package a.b

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

@javax.ws.rs.ext.Provider
class GExceptionMapper implements ExceptionMapper<GRuntimeException> {
    Response toResponse(GRuntimeException e) { Response.status(200).entity('GExceptionMapper').type('text/plain').build() }
}