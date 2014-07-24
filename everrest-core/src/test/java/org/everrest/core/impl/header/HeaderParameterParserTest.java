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

import org.everrest.core.impl.BaseTest;

import java.text.ParseException;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class HeaderParameterParserTest extends BaseTest {

    public void testSimple() throws ParseException {
        HeaderParameterParser hp = new HeaderParameterParser();
        String src = "text/plain;foo=bar";
        Map<String, String> m = hp.parse(src);
        assertEquals("bar", m.get("foo"));
        src = "text/plain;foo=\"bar\"";
        m = hp.parse(src);
        assertEquals("bar", m.get("foo"));
    }

    public void testQuoted() throws ParseException {
        HeaderParameterParser hp = new HeaderParameterParser();
        String src = "text/plain;foo=\"\\\"he\\\";llo\\\"\"   ;  ba r  =  f o o       ; foo2";
        Map<String, String> m = hp.parse(src);
        assertEquals(3, m.size());
        assertEquals("\"he\";llo\"", m.get("foo"));
        assertEquals("f o o", m.get("ba r"));
        assertNull(m.get("foo2"));

        src = "text/plain;bar=\"foo\" \t; bar2; test=\"\\a\\b\\c\\\"\"   ;  foo=bar";
        m = hp.parse(src);
        assertEquals(4, m.size());
        assertEquals("foo", m.get("bar"));
        assertEquals("\\a\\b\\c\"", m.get("test"));
        assertEquals("bar", m.get("foo"));
        assertNull(m.get("bar2"));
    }

}
