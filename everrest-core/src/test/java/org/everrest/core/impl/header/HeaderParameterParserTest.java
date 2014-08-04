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
package org.everrest.core.impl.header;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Map;

/**
 * @author andrew00x
 */
public class HeaderParameterParserTest {

    @Test
    public void testSimple() throws ParseException {
        HeaderParameterParser hp = new HeaderParameterParser();
        String src = "text/plain;foo=bar";
        Map<String, String> m = hp.parse(src);
        Assert.assertEquals("bar", m.get("foo"));
        src = "text/plain;foo=\"bar\"";
        m = hp.parse(src);
        Assert.assertEquals("bar", m.get("foo"));
    }

    @Test
    public void testQuoted() throws ParseException {
        HeaderParameterParser hp = new HeaderParameterParser();
        String src = "text/plain;foo=\"\\\"he\\\";llo\\\"\"   ;  ba r  =  f o o       ; foo2";
        Map<String, String> m = hp.parse(src);
        Assert.assertEquals(3, m.size());
        Assert.assertEquals("\"he\";llo\"", m.get("foo"));
        Assert.assertEquals("f o o", m.get("ba r"));
        Assert.assertNull(m.get("foo2"));
    }

    @Test
    public void testQuoted2() throws ParseException {
        HeaderParameterParser hp = new HeaderParameterParser();
        String src = "text/plain;bar=\"foo\" \t; bar2; test=\"\\a\\b\\c\\\"\"   ;  foo=bar";
        Map<String, String> m = hp.parse(src);
        Assert.assertEquals(4, m.size());
        Assert.assertEquals("foo", m.get("bar"));
        Assert.assertEquals("\\a\\b\\c\"", m.get("test"));
        Assert.assertEquals("bar", m.get("foo"));
        Assert.assertNull(m.get("bar2"));
    }
}
