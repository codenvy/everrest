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
package org.everrest.core.impl.provider.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/** @author andrew00x */
public interface InputItem {

  String getName();

  String getFilename();

  MediaType getMediaType();

  MultivaluedMap<String, String> getHeaders();

  InputStream getBody() throws IOException;

  <T> T getBody(Class<T> type, Type genericType) throws IOException;

  String getBodyAsString() throws IOException;
}
