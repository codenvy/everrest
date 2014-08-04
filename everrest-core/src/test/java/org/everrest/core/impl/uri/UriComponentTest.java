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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import java.net.URI;
import java.util.List;

/**
 * @author andrew00x
 */
public class UriComponentTest {

    @Test
    public void testCheckHexCharacters() {
        String str = "%20%23%a0%ag";
        Assert.assertTrue(UriComponent.checkHexCharacters(str, 0));
        Assert.assertFalse(UriComponent.checkHexCharacters(str, 1));
        Assert.assertTrue(UriComponent.checkHexCharacters(str, 3));
        Assert.assertTrue(UriComponent.checkHexCharacters(str, 6));
        Assert.assertFalse(UriComponent.checkHexCharacters(str, 9));
        Assert.assertFalse(UriComponent.checkHexCharacters(str, 11));
    }

    @Test
    public void testEncodeDecode() {
        String str = "\u041f?\u0440#\u0438 \u0432\u0456\u0442";
        String estr = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
        Assert.assertEquals(estr, UriComponent.encode(str, UriComponent.HOST, false));
        Assert.assertEquals(str, UriComponent.decode(estr, UriComponent.HOST));

        // wrong encoded string, near %9g
        String estr1 = "%D0%9g%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
        try {
            UriComponent.decode(estr1, UriComponent.HOST);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
        // wrong encoded string, end %8
        estr1 = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%8";
        try {
            UriComponent.decode(estr1, UriComponent.HOST);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testParseQueryString() {
        String str = "q1=to%20be%20or%20not%20to%20be&q2=foo&q2=%D0%9F%D1%80%D0%B8%D0%B2%D1%96%D1%82";
        MultivaluedMap<String, String> m = UriComponent.parseQueryString(str, false);
        Assert.assertEquals(2, m.size());
        Assert.assertEquals(1, m.get("q1").size());
        Assert.assertEquals(2, m.get("q2").size());
        m = UriComponent.parseQueryString(str, true);
        Assert.assertEquals(2, m.size());
        Assert.assertEquals(1, m.get("q1").size());
        Assert.assertEquals(2, m.get("q2").size());
        Assert.assertEquals("to be or not to be", m.get("q1").get(0));
        Assert.assertEquals("foo", m.get("q2").get(0));
        Assert.assertEquals("\u041f\u0440\u0438\u0432\u0456\u0442", m.get("q2").get(1));
    }

    @Test
    public void test_EVERREST_58() {
        String str = "q1=bar&&q2=foo&q2=test";
        MultivaluedMap<String, String> m = UriComponent.parseQueryString(str, false);
        Assert.assertEquals(2, m.size());
        Assert.assertEquals(1, m.get("q1").size());
        Assert.assertEquals(2, m.get("q2").size());
        m = UriComponent.parseQueryString(str, true);
        Assert.assertEquals(2, m.size());
        Assert.assertEquals(1, m.get("q1").size());
        Assert.assertEquals(2, m.get("q2").size());
        Assert.assertEquals("bar", m.get("q1").get(0));
        Assert.assertEquals("foo", m.get("q2").get(0));
        Assert.assertEquals("test", m.get("q2").get(1));
    }

    @Test
    public void testParsePathSegment() {
        String path = "/to/be/or%20not/to/be;a=foo;b=b%20a%23r";
        List<PathSegment> segms = UriComponent.parsePathSegments(path, true);
        Assert.assertEquals(5, segms.size());
        Assert.assertEquals("to", segms.get(0).getPath());
        Assert.assertEquals("be", segms.get(1).getPath());
        Assert.assertEquals("or not", segms.get(2).getPath());
        Assert.assertEquals("to", segms.get(3).getPath());
        Assert.assertEquals("be", segms.get(4).getPath());
        Assert.assertEquals("foo", segms.get(4).getMatrixParameters().get("a").get(0));
        Assert.assertEquals("b a#r", segms.get(4).getMatrixParameters().get("b").get(0));
    }

    @Test
    public void testRecognizeEncoding() {
        String str = "to be%23or not to%20be";
        // double encoding here, %23 -> %2523 and %20 -> %2520
        Assert.assertEquals("to%20be%2523or%20not%20to%2520be", UriComponent.encode(str, UriComponent.PATH_SEGMENT, false));
        // no double encoding here
        Assert.assertEquals("to%20be%23or%20not%20to%20be", UriComponent.recognizeEncode(str, UriComponent.PATH_SEGMENT, false));
    }

    @Test
    public void testURINormalization() throws Exception {
        String[] testUris = {"http://localhost:8080/servlet/../1//2/3/./../../4", //
                             "http://localhost:8080/servlet/./1//2/3/./../../4", //
                             "http://localhost:8080/servlet/1//2/3/./../../4", //
                             "http://localhost:8080/servlet/1//2./3/./../4", //
                             "http://localhost:8080/servlet/1//.2/3/./../4", //
                             "http://localhost:8080/servlet/1..//.2/3/./../4", //
                             "http://localhost:8080/servlet/./1//2/3/./../../4", //
                             "http://localhost:8080/servlet/.", //
                             "http://localhost:8080/servlet/..", //
                             "http://localhost:8080/servlet/1"};

        String[] normalizedUris = {"http://localhost:8080/1/4", //
                                   "http://localhost:8080/servlet/1/4", //
                                   "http://localhost:8080/servlet/1/4", //
                                   "http://localhost:8080/servlet/1/2./4", //
                                   "http://localhost:8080/servlet/1/.2/4", //
                                   "http://localhost:8080/servlet/1../.2/4", //
                                   "http://localhost:8080/servlet/1/4", //
                                   "http://localhost:8080/servlet/", //
                                   "http://localhost:8080/", //
                                   "http://localhost:8080/servlet/1"};

        for (int i = 0; i < testUris.length; i++) {
            URI requestUri = new URI(testUris[i]);
            Assert.assertEquals(normalizedUris[i], UriComponent.normalize(requestUri).toString());
        }
    }
}
