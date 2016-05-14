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
package org.everrest.core.impl.uri;


import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Link;
import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LinkBuilderImplTest {

    private LinkBuilderImpl linkBuilder;

    @Before
    public void setUp() throws Exception {
        linkBuilder = new LinkBuilderImpl();
    }

    @Test
    public void buildsLinkBuilderFromExistedLink() {
        Link link = mock(Link.class);
        URI uri = URI.create("http://localhost:8080/x/y/z");
        when(link.getUri()).thenReturn(uri);
        Map<String, String> params = ImmutableMap.of("rel", "xxx", "title", "yyy");
        when(link.getParams()).thenReturn(params);

        Link newLink = linkBuilder.link(link).build();

        assertEquals(uri, newLink.getUri());
        assertEquals(params, newLink.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenLinkStringIsNull() {
        linkBuilder.link((String)null).build();
    }

    @Test
    public void buildsLinkFromStringWithEmptyUri() {
        String uri = "<>";
        Link link = linkBuilder.link(uri).build();

        assertEquals(URI.create(""), link.getUri());
        assertNull(link.getRel());
        assertNull(link.getTitle());
        assertNull(link.getType());
        assertTrue(link.getRels().isEmpty());
        assertTrue(link.getParams().isEmpty());
    }

    @Test
    public void buildsLinkFromStringWithoutLinkParameters() {
        String uri = "< http://localhost:8080/x/y/z >";
        Link link = linkBuilder.link(uri).build();

        assertEquals(URI.create("http://localhost:8080/x/y/z"), link.getUri());
        assertNull(link.getRel());
        assertNull(link.getTitle());
        assertNull(link.getType());
        assertTrue(link.getRels().isEmpty());
        assertTrue(link.getParams().isEmpty());
    }

    @Test
    public void buildsLinkFromStringWithEmptyLinkParameters() {
        String uri = "< http://localhost:8080/x/y/z>";
        Link link = linkBuilder.link(uri + ";").build();

        assertEquals(URI.create("http://localhost:8080/x/y/z"), link.getUri());
        assertNull(link.getRel());
        assertNull(link.getTitle());
        assertNull(link.getType());
        assertTrue(link.getRels().isEmpty());
        assertTrue(link.getParams().isEmpty());
    }

    @Test
    public void buildsLinkFromStringWithLinkParameters() {
        String uri = "< http://localhost:8080/x/y/z >";
        Link link = linkBuilder.link(uri + "; rel=\"xxx\"; title=\"yyy\"").build();

        assertEquals(URI.create("http://localhost:8080/x/y/z"), link.getUri());
        assertEquals("xxx", link.getRel());
        assertEquals("yyy", link.getTitle());
        assertEquals(ImmutableMap.of("rel", "xxx", "title", "yyy"), link.getParams());
    }

    @Test
    public void buildsLinkFromUri() {
        URI uri = URI.create("http://localhost:8080/x/y/z");
        Link link = linkBuilder.uri(uri).build();
        assertEquals(uri, link.getUri());
    }

    @Test
    public void buildsLinkWithRel() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").rel("xxx").build();
        assertEquals("xxx", link.getRel());
        assertEquals(newArrayList("xxx"), link.getRels());
        assertEquals(ImmutableMap.of("rel", "xxx"), link.getParams());
    }

    @Test
    public void buildsLinkWithMultipleRels() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").rel("xxx").rel("yyy").build();
        assertEquals("xxx yyy", link.getRel());
        assertEquals(newArrayList("xxx", "yyy"), link.getRels());
        assertEquals(ImmutableMap.of("rel", "xxx yyy"), link.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenRelIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").rel(null);
    }

    @Test
    public void buildsLinkWithTitle() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").title("yyy").build();
        assertEquals("yyy", link.getTitle());
        assertEquals(ImmutableMap.of("title", "yyy"), link.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenTileIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").title(null);
    }

    @Test
    public void buildsLinkWithType() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").type("text/css").build();
        assertEquals("text/css", link.getType());
        assertEquals(ImmutableMap.of("type", "text/css"), link.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenTypeIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").type(null);
    }

    @Test
    public void buildsLinkWithLinkParams() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").param("a", "b").param("c", "d").build();
        assertEquals(ImmutableMap.of("a", "b", "c", "d"), link.getParams());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNameOfParameterIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").param(null, "b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenValueOfParameterIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").param("a", null);
    }

    @Test
    public void buildsLinkWithParameters() {
        Link link = linkBuilder.uri("http://localhost:8080/{x}/{y}/{x}").build("a", "b");
        assertEquals(URI.create("http://localhost:8080/a/b/a"), link.getUri());
    }

    @Test
    public void buildsLinkWithBaseUriAndParameters() {
        Link link = linkBuilder.baseUri("http://localhost:8080").uri("/{x}/{y}/{x}").build("a", "b");
        assertEquals(URI.create("http://localhost:8080/a/b/a"), link.getUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenValuesForBuildIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").build(null);
    }

    @Test
    public void buildsLinkWithRelativizedUri() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").buildRelativized(URI.create("http://localhost:8080/x"));
        assertEquals(URI.create("y/z"), link.getUri());
    }

    @Test
    public void buildsLinkWhenSpecifiedUriDoesNotShareSamePrefixWithLinkUri() {
        Link link = linkBuilder.uri("http://localhost:8080/x/y/z").buildRelativized(URI.create("http://localhost2:8080/x"));
        assertEquals(URI.create("http://localhost:8080/x/y/z"), link.getUri());
    }

    @Test
    public void buildsLinkWithRelativeUri() {
        Link link = linkBuilder.uri("x/y/z").buildRelativized(URI.create("http://localhost:8080/x"));
        assertEquals(URI.create("x/y/z"), link.getUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenUriForBuildRelativizedLinkIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").buildRelativized(null, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenValuesForBuildRelativizedLinkIsNull() {
        linkBuilder.uri("http://localhost:8080/x/y/z").buildRelativized(URI.create("http://localhost:8080/x"), null);
    }
}
