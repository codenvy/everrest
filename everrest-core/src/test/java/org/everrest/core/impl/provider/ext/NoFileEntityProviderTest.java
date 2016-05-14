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
package org.everrest.core.impl.provider.ext;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.WebApplicationException;
import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoFileEntityProviderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private NoFileEntityProvider noFileEntityProvider;

    @Before
    public void setUp() throws Exception {
        noFileEntityProvider = new NoFileEntityProvider();
    }

    @Test
    public void isReadableForFile() throws Exception {
        assertTrue(noFileEntityProvider.isReadable(File.class, null, null, null));
    }

    @Test
    public void isNotReadableForTypeOtherThanFile() throws Exception {
        assertFalse(noFileEntityProvider.isReadable(String.class, null, null, null));
    }

    @Test
    public void isWritableForFile() throws Exception {
        assertTrue(noFileEntityProvider.isWriteable(File.class, null, null, null));
    }

    @Test
    public void isNotWritableForTypeOtherThanFile() throws Exception {
        assertFalse(noFileEntityProvider.isWriteable(String.class, null, null, null));
    }

    @Test
    public void throwsWebApplicationExceptionWhenTryToGetSizeOfFile() {
        thrown.expect(webApplicationExceptionBadRequestMatcher());
        noFileEntityProvider.getSize(null, null, null, null, null);
    }

    @Test
    public void readsContentOfEntityStreamAsFile() throws Exception {
        thrown.expect(webApplicationExceptionBadRequestMatcher());
        noFileEntityProvider.readFrom(File.class, null, null, null, null, null);
    }

    @Test
    public void writesFileToOutputStream() throws Exception {
        thrown.expect(webApplicationExceptionBadRequestMatcher());
        noFileEntityProvider.writeTo(null, null, null, null, null, null, null);
    }

    private BaseMatcher<Throwable> webApplicationExceptionBadRequestMatcher() {
        return new BaseMatcher<Throwable>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof WebApplicationException
                       && ((WebApplicationException)item).getResponse().getStatus() == 400;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("WebApplicationException with status 400, \"Bad Request\"");
            }
        };
    }
}