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
package org.everrest.core.impl.provider.multipart;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
public interface InputItem {

    String getName();

    String getFilename();

    MediaType getMediaType();

    MultivaluedMap<String, String> getHeaders();

    InputStream getBody() throws IOException;

    <T> T getBody(Class<T> type, Type genericType) throws IOException;

    String getBodyAsString() throws IOException;
}
