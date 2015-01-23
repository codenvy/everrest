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

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author andrew00x
 */
public class UriBuilderTest {
    @Test
    public void testReplaceScheme() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("https").build();
        Assert.assertEquals(URI.create("https://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testRemoveScheme() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).build();
        Assert.assertEquals(URI.create("//localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testReplaceSchemeFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("htt\tp").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testReplaceUserInfo() {
        URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo("andrew").build();
        Assert.assertEquals(URI.create("http://andrew@localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testRemoveUserInfo() {
        URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo(null).build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testReplaceHost() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("exoplatform.org").build();
        Assert.assertEquals(URI.create("http://exoplatform.org:8080/a/b/c"), uri);
    }

    @Test
    public void testReplaceHostEncoded() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("te st.org").build();
        Assert.assertEquals(URI.create("http://te%20st.org:8080/a/b/c"), uri);
    }

    @Test
    public void testRemoveHost() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host(null).build();
        Assert.assertEquals(URI.create("http://:8080/a/b/c"), uri);
    }

    @Test
    public void testReplacePort() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(9000).build();
        Assert.assertEquals(URI.create("http://localhost:9000/a/b/c"), uri);
    }

    @Test
    public void testRemovePort() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-1).build();
        Assert.assertEquals(URI.create("http://localhost/a/b/c"), uri);
    }

    @Test
    public void testReplacePath() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath("/x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/x/y/z"), uri);
    }

    @Test
    public void testReplaceMatrixParam() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;b=y;a=b;a=c"), uri);
    }

    @Test
    public void testReplaceMatrixParam2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("a", "b", "c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;b=y;a=b;a=c"), uri);
    }

    @Test
    public void testReplaceMatrixParam3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("b", (String[])null).build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;a=x;a=z"), uri);
    }

    @Test
    public void testReplaceMatrixParam4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam("b", new String[0])
                            .build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;y=b;a=x;a=z"), uri);
    }

    @Test
    public void testReplaceMatrixParamNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z").replaceMatrixParam(null, "a").build();
            Assert.fail("IllegalArgumentException should be thrown. ");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testReplaceMatrixParamsByString1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").replaceMatrix("x=a;y=b").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), uri);
    }

    @Test
    public void testReplaceMatrixParamsByString2() {
        // There are no matrix parameters so nothing to replace. New ones must be added to the path.
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replaceMatrix("x=a;y=b").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), uri);
    }

    @Test
    public void testReplaceQueryParam1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y&a=z").replaceQueryParam("a", "b", "c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), uri);
    }

    @Test
    public void testReplaceQueryParam2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&a=z&b=y").replaceQueryParam("a", "b", "c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=b&a=c"), uri);
    }

    @Test
    public void testReplaceQueryParam3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam("a", "b%20", "c%").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&y=b&a=b%20&a=c%25"), uri);
    }

    @Test
    public void testReplaceQueryParam4() {
        // Try replace not existed parameter.
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam("x", "b").build();
        // Parameter appended to URI.
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z&x=b"), uri);
    }

    @Test
    public void testRemoveQueryParam() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&b=&y=b&a=z").replaceQueryParam("b", (String[])null).build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&y=b&a=z"), uri);
    }

    @Test
    public void testReplaceQueryParamNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z").replaceQueryParam(null, "b", "c").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testReplaceQueryByString1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&y=b&zzz=").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b&zzz"), uri);
    }

    @Test
    public void testReplaceQueryByString2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&zzz=&y=b").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&zzz&y=b"), uri);
    }

    @Test
    public void testRemoveQuery1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testRemoveQuery2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery(null).build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testReplaceQueryFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&=zzz&y=b").build();
            Assert.fail("UriBuilderException should be here");
        } catch (UriBuilderException e) {
        }
    }

    @Test
    public void testReplaceFragment() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#hi").fragment("hel lo").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#hel%20lo"), u);
    }

    @Test
    public void testReplaceByUriScheme() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("https://exo@localhost:8080")).build();
        Assert.assertEquals(URI.create("https://exo@localhost:8080/a/b/c?a=x&b=y#fragment"), u);
    }

    @Test
    public void testReplaceByUriUriInfo() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("http://andrew@localhost:9000")).build();
        Assert.assertEquals(URI.create("http://andrew@localhost:9000/a/b/c?a=x&b=y#fragment"), u);
    }

    @Test
    public void testReplaceByUriPath() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("/x/y/z")).build();
        Assert.assertEquals(URI.create("http://exo@localhost:8080/x/y/z?a=x&b=y#fragment"), u);
    }

    @Test
    public void testReplaceByUriQuery() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("?x=a&b=y")).build();
        Assert.assertEquals(URI.create("http://exo@localhost:8080/a/b/c?x=a&b=y#fragment"), u);
    }

    @Test
    public void testReplaceByUriFragment() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI u = UriBuilder.fromUri(origin).uri(URI.create("#fragment2")).build();
        Assert.assertEquals(URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment2"), u);
    }

    @Test
    public void testReplaceByUriNull() {
        URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        try {
            UriBuilder.fromUri(origin).uri((String)null).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSchemeSpecificPart1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart("//localhost:8080/a/b/c/d").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/d"), uri);
    }

    @Test
    public void testSchemeSpecificPart2() {
        URI uri1 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI uri2 = UriBuilder.fromUri(uri1).schemeSpecificPart("//andrew@exoplatform.org:9000/x/y/z?x=a&y=b").build();
        Assert.assertEquals(URI.create("http://andrew@exoplatform.org:9000/x/y/z?x=a&y=b#fragment"), uri2);
    }

    @Test
    public void testSchemeSpecificPart3() {
        URI uri1 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
        URI uri2 = UriBuilder.fromUri(uri1).schemeSpecificPart("//andrew@exoplatform.org:9000/x /y/z?x= a&y=b").build();
        Assert.assertEquals(URI.create("http://andrew@exoplatform.org:9000/x%20/y/z?x=%20a&y=b#fragment"), uri2);
    }

    @Test
    public void testSchemeSpecificPartNull() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart(null).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendPath1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").path("a/b/c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testAppendPath2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").path("a/b/c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testAppendPath3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").path("/a/b/c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testAppendPath4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").path("/a/b/c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testAppendPath5() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);
    }

    @Test
    public void testAppendPath6() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);
    }

    @Test
    public void testAppendPath7() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    @Test
    public void testAppendPath8() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c%20").path("/x/y /z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c%20/x/y%20/z"), uri);
    }

    @Test
    public void testAppendSegment1() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
    }

    @Test
    public void testAppendSegment2() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c", "/x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    @Test
    public void testAppendSegment3() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").segment("a/b/c/", "x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    @Test
    public void testAppendSegment4() {
        URI uri = UriBuilder.fromUri("http://localhost:8080").segment("/a/b/c/", "/x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    @Test
    public void testAppendSegment5() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/").segment("/a/b/c", "x/y/z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);
    }

    @Test
    public void testAppendSegmentNull() {
        try {
            UriBuilder.fromUri("http://localhost:8080/").segment((String[])null).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendQueryParams1() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "x").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x"), u);
    }

    @Test
    public void testAppendQueryParams2() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c ", "%25z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c%20=%25z"), u);
    }

    @Test
    public void testAppendQueryParamsNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam(null, "z").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendQueryParamsNullValues() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c", (String[])null).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendQueryParamsNullValue() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c", new String[]{"z", null}).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendMatrixParams() {
        URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").matrixParam(" c", "%z").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;%20c=%25z"), u);
    }

    @Test
    public void testAppendMatrixParamsNullName() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam(null, "x").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendMatrixParamsNullValues() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam("a", (String[])null).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendMatrixParamsNullValue() {
        try {
            UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam("a", new String[]{"x", null}).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAppendPathAndMatrixParams() {
        URI u = UriBuilder.fromUri("http://localhost:8080/").path("a").matrixParam("x", " foo").matrixParam("y", "%20bar")
                          .path("b").matrixParam("x", "f o%20o").build();
        Assert.assertEquals(URI.create("http://localhost:8080/a;x=%20foo;y=%20bar/b;x=f%20o%20o"), u);
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

    @Test
    public void testResourceAppendPath() {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).build();
        Assert.assertEquals(URI.create("http://localhost:8080/base/resource"), uri);
    }

    @Test
    public void testResourceAppendPathFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(Object.class).build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testMethodAppendPath1() throws NoSuchMethodException {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("get")).build();
        Assert.assertEquals(URI.create("http://localhost:8080/base/method1"), uri);
    }

    @Test
    public void testMethodAppendPath2() throws NoSuchMethodException {
        URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("post")).build();
        Assert.assertEquals(URI.create("http://localhost:8080/base"), uri);
    }

    @Test
    public void testResourceAndMethodAppendPath1() {
        URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "get").build();
        Assert.assertEquals(URI.create("http://localhost:8080/base/resource/method1"), u);
    }

    @Test
    public void testResourceAndMethodAppendPath2() {
        URI u = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "post").build();
        Assert.assertEquals(URI.create("http://localhost:8080/base/resource"), u);
    }

    @Test
    public void testResourceAndMethodAppendPathFail() {
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "wrong").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testResourceAndMethodAppendPathConflict() {
        // There are two methods with name 'get' in class R2.
        try {
            UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R2.class, "get").build();
            Assert.fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testBuildUriFromMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        m.put("baz", "z");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{baz}/{foo}").buildFromMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildUriFromMap2() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/baz/foo").buildFromMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildUriFromMap3() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{foo}/baz").buildFromMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/%2525x/baz"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildUriFromEncodedMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        m.put("baz", "z");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{baz}/{foo}").buildFromEncodedMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildUriFromEncodedMap2() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/baz/foo").buildFromEncodedMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildUriFromEncodedMap3() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "%25x");
        m.put("bar", "%y");
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("{foo}/{bar}/{foo}/baz").buildFromEncodedMap(m);
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTemplates() {
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").build("%25x", "%y", "z", "wrong");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTemplates2() {
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/baz/foo").build("z", "y");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/z/y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTemplates3() {
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{foo}/baz").build("z", "y");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/z/y/z/baz"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEncodedTemplates() {
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}").buildFromEncoded("%25x",
                                                                                                                          "%y", "z",
                                                                                                                          "wrong");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/z/%25x"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEncodedTemplates2() {
        try {
            URI uri =
                    UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/baz/foo").buildFromEncoded("%25x", "%y", "wrong");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/baz/foo"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEncodedTemplates3() {
        try {
            URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{foo}/baz")
                                .buildFromEncoded("%25x", "%y", "wrong");
            Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"), uri);
        } catch (UriBuilderException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testResolveTemplateOneParameter() {
        String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        builder.resolveTemplate("scheme", "https");
        Assert.assertEquals("https://{host}/a/{path}?{q}={v}#{fragment}", builder.toTemplate());
        builder.resolveTemplate("host", "localhost");
        Assert.assertEquals("https://localhost/a/{path}?{q}={v}#{fragment}", builder.toTemplate());
        builder.resolveTemplate("path", "b/c");
        Assert.assertEquals("https://localhost/a/b/c?{q}={v}#{fragment}", builder.toTemplate());
        builder.resolveTemplate("q", "test");
        builder.resolveTemplate("v", "hello");
        Assert.assertEquals("https://localhost/a/b/c?test=hello#{fragment}", builder.toTemplate());
        builder.resolveTemplate("fragment", "hello_fragment");
        Assert.assertEquals("https://localhost/a/b/c?test=hello#hello_fragment", builder.toTemplate());
    }

    @Test
    public void testResolveTemplateOneParameterEncodeSlashInPath() {
        String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        builder.resolveTemplate("path", "b/c", true);
        Assert.assertEquals("{scheme}://{host}/a/b%2Fc?{q}={v}#{fragment}", builder.toTemplate());
    }

    @Test
    public void testResolveTemplateOneParameterFromEncoded() {
        String template = "http://localhost/a/{path}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        builder.resolveTemplateFromEncoded("path", "b%2fc");
        Assert.assertEquals("http://localhost/a/b%2fc", builder.toTemplate());
    }

    @Test
    public void testResolveTemplateWithMap() {
        String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        Map<String,Object> templateValues = new HashMap<>(8);
        templateValues.put("scheme", "https");
        templateValues.put("host", "localhost");
        templateValues.put("path", "b/c");
        templateValues.put("q", "test");
        templateValues.put("v", "hello");
        templateValues.put("fragment", "hello_fragment");
        builder.resolveTemplates(templateValues);
        Assert.assertEquals("https://localhost/a/b/c?test=hello#hello_fragment", builder.toTemplate());
    }

    @Test
    public void testResolveTemplateWithMapEncodeSlashInPath() {
        String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        builder.resolveTemplate("path", "b/c", true);
        Assert.assertEquals("{scheme}://{host}/a/b%2Fc?{q}={v}#{fragment}", builder.toTemplate());
    }

    @Test
    public void testResolveTemplateWithMapFromEncoded() {
        String template = "http://localhost/a/{path}";
        UriBuilder builder = UriComponent.parseTemplate(template);
        builder.resolveTemplateFromEncoded("path", "b%2fc");
        Assert.assertEquals("http://localhost/a/b%2fc", builder.toTemplate());
    }

    @Test
    public void testClone() {
        UriBuilder u = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
        URI full = u.clone().path("b").build();
        URI base = u.build();

        Assert.assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
        Assert.assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
    }
}
