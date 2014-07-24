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
package org.everrest.core.impl.uri;

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriBuilderTest extends BaseTest {
    public void testReplaceScheme() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("https").build();
        assertEquals(URI.create("https://localhost:8080/a/b/c"), uri);
    }

    public void testRemoveScheme() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).build();
        assertEquals(URI.create("//localhost:8080/a/b/c"), uri);
    }

    public void testReplaceSchemeFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("htt\tp").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testReplaceUserInfo() {
        URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo("andrew").build();
        assertEquals(URI.create("http://andrew@localhost:8080/a/b/c"), uri);
    }

    public void testRemoveUserInfo() {
        URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo(null).build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testReplaceHost() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("exoplatform.org").build();
        assertEquals(URI.create("http://exoplatform.org:8080/a/b/c"), uri);
    }

    public void testReplaceHostEncoded() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("te st.org").build();
        assertEquals(URI.create("http://te%20st.org:8080/a/b/c"), uri);
    }

    public void testRemoveHost() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host(null).build();
        assertEquals(URI.create("http://:8080/a/b/c"), uri);
    }

    public void testReplacePort() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(9000).build();
        assertEquals(URI.create("http://localhost:9000/a/b/c"), uri);
    }

    public void testRemovePort() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-1).build();
        assertEquals(URI.create("http://localhost/a/b/c"), uri);
    }

    public void testReplacePath() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath("/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/x/y/z"), uri);
    }

    public void testReplaceMatrixParam() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;b=y;a=b;a=c"), uri);
    }

    public void testReplaceMatrixParam2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;b=y;a=b;a=c"), uri);
    }

    public void testReplaceMatrixParam3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("b", (String[])null)
                            .build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;a=x;a=z"), uri);
    }

    public void testReplaceMatrixParam4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("b", new String[0])
                            .build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;a=x;a=z"), uri);
    }

    public void testReplaceMatrixParamNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam(null, "a").build();
            fail("IllegalArgumentException should be thrown. ");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testReplaceMatrixParamsByString1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").replaceMatrix("x=a;y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), uri);
    }

    public void testReplaceMatrixParamsByString2() {
        // There are no matrix parameters so nothing to replace. New ones must be added to the path.
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replaceMatrix("x=a;y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), uri);
    }

    public void testReplaceQueryParam1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y&a=z").replaceQueryParam("a", "b", "c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), uri);
    }

    public void testReplaceQueryParam2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&a=z&b=y").replaceQueryParam("a", "b", "c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), uri);
    }

    public void testReplaceQueryParam3() {
        URI uri =
                UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam("a", "b%20", "c%").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&y=b&a=b%20&a=c%25"), uri);
    }

    public void testReplaceQueryParam4() {
        // Try replace not existed parameter.
        URI uri =
                UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam("x", "b").build();
        // Parameter appended to URI.
        assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z&x=b"), uri);
    }

    public void testRemoveQueryParam() {
        URI uri =
                UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&b=&y=b&a=z").replaceQueryParam("b", (String[])null).build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&y=b&a=z"), uri);
    }

    public void testReplaceQueryParamNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam(null, "b", "c").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testReplaceQueryByString1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&y=b&zzz=").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b&zzz"), uri);
    }

    public void testReplaceQueryByString2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&zzz=&y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&zzz&y=b"), uri);
    }

    public void testRemoveQuery1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testRemoveQuery2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery(null).build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testReplaceQueryFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&=zzz&y=b").build();
            fail("UriBuilderException should be here");
        } catch (UriBuilderException e) {
        }
    }

    public void testReplaceFragment() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#hi").fragment("hel lo").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#hel%20lo"), u);
    }

    public void testReplaceByUriScheme() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("https://exo@localhost:8080")).build();
        assertEquals(URI.create("https://exo@localhost:8080/a/b/c?a=x&b=y#fragment"), u);
    }

    public void testReplaceByUriUriInfo() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("http://andrew@localhost:9000")).build();
        assertEquals(URI.create("http://andrew@localhost:9000/a/b/c?a=x&b=y#fragment"), u);
    }

    public void testReplaceByUriPath() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("/x/y/z")).build();
        assertEquals(URI.create("http://exo@localhost:8080/x/y/z?a=x&b=y#fragment"), u);
    }

    public void testReplaceByUriQuery() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("?x=a&b=y")).build();
        assertEquals(URI.create("http://exo@localhost:8080/a/b/c?x=a&b=y#fragment"), u);
    }

    public void testReplaceByUriFragment() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("#fragment2")).build();
        assertEquals(URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment2"), u);
    }

    public void testReplaceByUriNull() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        try {
            UriBuilder.fromUri(origin).uri(null).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSchemeSpecificPart1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart("//localhost:8080/a/b/c/d").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/d"), uri);
    }

    public void testSchemeSpecificPart2() {
        URI uri1 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI uri2 = UriBuilder.fromUri(uri1).schemeSpecificPart("//andrew@exoplatform.org:9000/x/y/z?x=a&y=b").build();
        assertEquals(URI.create("http://andrew@exoplatform.org:9000/x/y/z?x=a&y=b#fragment"), uri2);
    }

    public void testSchemeSpecificPart3() {
        URI uri1 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI uri2 = UriBuilder.fromUri(uri1).schemeSpecificPart("//andrew@exoplatform.org:9000/x /y/z?x= a&y=b").build();
        assertEquals(URI.create("http://andrew@exoplatform.org:9000/x%20/y/z?x=%20a&y=b#fragment"), uri2);
    }

    public void testSchemeSpecificPartNull() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart(null).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendPath1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").path("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testAppendPath2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").path("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testAppendPath3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").path("/a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testAppendPath4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").path("/a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testAppendPath5() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);
    }

    public void testAppendPath6() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);
    }

    public void testAppendPath7() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    public void testAppendPath8() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c%20").path("/x/y /z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c%20/x/y%20/z"), uri);
    }

    public void testAppendSegment1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    public void testAppendSegment2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c", "/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    public void testAppendSegment3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").segment("a/b/c/", "x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    public void testAppendSegment4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("/a/b/c/", "/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    public void testAppendSegment5() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").segment("/a/b/c", "x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    public void testAppendSegmentNull() {
        try {
            UriBuilder.fromUri("http://localhost:8080/").segment((String[])null).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendQueryParams1() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "x").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x"), u);
    }

    public void testAppendQueryParams2() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c ", "%25z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c%20=%25z"), u);
    }

    public void testAppendQueryParamsNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam(null, "z").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendQueryParamsNullValues() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c", (String[])null).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendQueryParamsNullValue() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c", new String[]{"z", null}).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendMatrixParams() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").matrixParam(" c", "%z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;%20c=%25z"), u);
    }

    public void testAppendMatrixParamsNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam(null, "x").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendMatrixParamsNullValues() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam("a", (String[])null).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendMatrixParamsNullValue() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam("a", new String[]{"x", null}).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAppendPathAndMatrixParams() {
        URI u =
                UriBuilder.fromUri("http://localhost:8080/").path("a").matrixParam("x", " foo").matrixParam("y", "%20bar")
                          .path("b").matrixParam("x", "f o%20o").build();
        assertEquals(URI.create("http://localhost:8080/a;x=%20foo;y=%20bar/b;x=f%20o%20o"), u);
    }

    @Path("resource")
    class R {
        @GET
        @Path("method1")
        public void get() {
        }

        @POST
        public void post() {
        }
    }

    @Path("resource2")
    class R2 {
        @GET
        @Path("method1")
        public void get() {
        }

        @GET
        @Path("method2")
        public void get(String s) {
        }
    }

    public void testResourceAppendPath() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).build();
        assertEquals(URI.create("http://localhost:8080/base/resource"), uri);
    }

    public void testResourceAppendPathFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(Object.class).build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testMethodAppendPath1() throws NoSuchMethodException {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("get")).build();
        assertEquals(URI.create("http://localhost:8080/base/method1"), uri);
    }

    public void testMethodAppendPath2() throws NoSuchMethodException {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("post")).build();
        assertEquals(URI.create("http://localhost:8080/base"), uri);
    }

    public void testResourceAndMethodAppendPath1() {
        URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "get").build();
        assertEquals(URI.create("http://localhost:8080/base/resource/method1"), u);
    }

    public void testResourceAndMethodAppendPath2() {
        URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "post").build();
        assertEquals(URI.create("http://localhost:8080/base/resource"), u);
    }

    public void testResourceAndMethodAppendPathFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "wrong").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testResourceAndMethodAppendPathConflict() {
        // There are two methods with name 'get' in class R2.
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R2.class, "get").build();
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testBuildUriFromMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        m.put("baz", "z");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{baz}/{foo}").buildFromMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testBuildUriFromMap2() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/baz/foo").buildFromMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testBuildUriFromMap3() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{foo}/baz").buildFromMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/%2525x/baz"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testBuildUriFromEncodedMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        m.put("baz", "z");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{baz}/{foo}").buildFromEncodedMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testBuildUriFromEncodedMap2() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/baz/foo").buildFromEncodedMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testBuildUriFromEncodedMap3() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{foo}/baz").buildFromEncodedMap(m);
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testTemplates() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").build("%25x", "%y", "z",
                                                                                                             "wrong");
            assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testTemplates2() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/baz/foo").build("z", "y");
            assertEquals(URI.create("http://localhost:8080/a/b/c/z/y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testTemplates3() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{foo}/baz").build("z", "y");
            assertEquals(URI.create("http://localhost:8080/a/b/c/z/y/z/baz"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testEncodedTemplates() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").buildFromEncoded("%25x",
                                                                                                                        "%y", "z", "wrong");
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testEncodedTemplates2() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/baz/foo").buildFromEncoded("%25x",
                                                                                                                    "%y", "wrong");
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testEncodedTemplates3() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{foo}/baz").buildFromEncoded("%25x",
                                                                                                                      "%y", "wrong");
            assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"), uri);
        } catch (UriBuilderException e) {
            fail(e.getMessage());
        }
    }

    public void testClone() {
        UriBuilder u = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
        URI full = u.clone().path("b").build();
        URI base = u.build();

        assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
        assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
    }
}
