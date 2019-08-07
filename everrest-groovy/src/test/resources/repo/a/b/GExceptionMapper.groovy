/**
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package a.b

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

@javax.ws.rs.ext.Provider
class GExceptionMapper implements ExceptionMapper<GRuntimeException> {
    Response toResponse(GRuntimeException e) { Response.status(200).entity('GExceptionMapper').type('text/plain').build() }
}