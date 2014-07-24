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
package org.everrest.core.impl.provider.json;


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonUtilsTest extends JsonTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetJSONString() {
        assertEquals(JsonUtils.getJsonString("string"), "\"string\"");
        assertEquals(JsonUtils.getJsonString("s\ntring\n"), "\"s\\ntring\\n\"");
        assertEquals(JsonUtils.getJsonString("s\tring"), "\"s\\tring\"");
        assertEquals(JsonUtils.getJsonString("st\ring"), "\"st\\ring\"");
        assertEquals(JsonUtils.getJsonString("str\\ing"), "\"str\\\\ing\"");
        assertEquals(JsonUtils.getJsonString("stri\"ng"), "\"stri\\\"ng\"");
        assertEquals(JsonUtils.getJsonString("stri/ng"), "\"stri/ng\"");
        int i = 0;
        for (char c = '\u0000'; c < '\u0020'; c++, i++) {
            System.out.print(JsonUtils.getJsonString(c + "") + " ");
            if (i > 10) {
                System.out.println();
                i = 0;
            }
        }
        for (char c = '\u0080'; c < '\u00a0'; c++, i++) {
            System.out.print(JsonUtils.getJsonString(c + " "));
            if (i > 10) {
                System.out.println();
                i = 0;
            }
        }
        for (char c = '\u2000'; c < '\u2100'; c++, i++) {
            System.out.print(JsonUtils.getJsonString(c + " "));
            if (i > 10) {
                System.out.println();
                i = 0;
            }
        }
    }

}
