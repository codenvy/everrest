/*
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
package org.everrest.core.impl.provider;

import com.google.common.io.Files;

import org.everrest.core.impl.FileCollector;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileEntityProviderTest {
    private byte[] testContent = "to be or not to be".getBytes();
    private FileEntityProvider fileEntityProvider;

    @Before
    public void setUp() throws Exception {
        fileEntityProvider = new FileEntityProvider();
    }

    @Test
    public void isReadableForFile() throws Exception {
        assertTrue(fileEntityProvider.isReadable(File.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanFile() throws Exception {
        assertFalse(fileEntityProvider.isReadable(String.class, null, null, null));
    }

    @Test
    public void isWritableForFile() throws Exception {
        assertTrue(fileEntityProvider.isWriteable(File.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanFile() throws Exception {
        assertFalse(fileEntityProvider.isWriteable(String.class, null, null, null));
    }

    @Test
    public void readsContentOfEntityStreamAsFile() throws Exception {
        File result = fileEntityProvider.readFrom(File.class, null, null, null, null, new ByteArrayInputStream(testContent));
        assertTrue(result.exists());
        byte[] bytes = Files.toByteArray(result);
        assertArrayEquals(testContent, bytes);
    }

    @Test
    public void writesFileToOutputStream() throws Exception {
        File source = FileCollector.getInstance().createFile();
        Files.write(testContent, source);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fileEntityProvider.writeTo(source, File.class, null, null, null, null, out);
        assertArrayEquals(testContent, out.toByteArray());
    }
}
