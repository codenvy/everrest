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
package org.everrest.core.impl.provider.ext;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;

import static org.junit.Assert.assertArrayEquals;

public class InMemoryFileItemTest {
    private static final int MAX_ALLOWED_CONTENT_LENGTH = 256;

    private InMemoryFileItem inMemoryFileItem;

    @Before
    public void setUp() throws Exception {
        inMemoryFileItem = new InMemoryFileItem("text/plain", "filed", false, "file.txt", MAX_ALLOWED_CONTENT_LENGTH);
    }

    @Test
    public void acceptsContentWhenSizeDoesNotExceedMaxLimit() throws Exception {
        byte[] content = "__TEST__".getBytes();
        inMemoryFileItem.getOutputStream().write(content);
        byte[] readContent = ByteStreams.toByteArray(inMemoryFileItem.getInputStream());
        assertArrayEquals(content, readContent);
    }

    @Test(expected = WebApplicationException.class)
    public void doesNotAcceptContentWhenSizeExceedsMaxLimit() throws Exception {
        byte[] content = new byte[MAX_ALLOWED_CONTENT_LENGTH + 1];
        inMemoryFileItem.getOutputStream().write(content);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doesNotSupportWritingInFile() throws Exception {
        inMemoryFileItem.write(null);
    }
}