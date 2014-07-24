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

import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonWriterTest extends JsonTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testJSONWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonWriter jsw = new JsonWriter(out);
        String key = "key";
        String value = "value";

        jsw.writeStartObject();
        jsw.writeKey(key + "_top");
        jsw.writeStartObject();
        for (int i = 0; i <= 5; i++) {
            jsw.writeKey(key + i);
            jsw.writeString(value + i);
        }
        jsw.writeKey(key + "_inner_top");
        jsw.writeStartObject();
        jsw.writeKey(key + "_string");
        jsw.writeString("string");
        jsw.writeKey(key + "_null");
        jsw.writeNull();
        jsw.writeKey(key + "_boolean");
        jsw.writeValue(true);
        jsw.writeKey(key + "_long");
        jsw.writeValue(121);
        jsw.writeKey(key + "_double");
        jsw.writeValue(121.121);
        jsw.writeEndObject();
        jsw.writeEndObject();
        jsw.writeKey(key + "_array");
        jsw.writeStartArray();
        for (int i = 0; i <= 5; i++) {
            jsw.writeString(value + i);
        }
        jsw.writeEndArray();
        jsw.writeEndObject();
        jsw.flush();
        jsw.close();
        System.out.println(new String(out.toByteArray()));
    }

}
