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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

/** @author andrew00x */
public class GroovyBeanTest extends JsonTest {

    public void testRestoreGroovyBean() throws Exception {
        GroovyClassLoader cl = new GroovyClassLoader();
        Class<?> c = cl.parseClass(Thread.currentThread().getContextClassLoader().getResourceAsStream("SimpleBean.groovy"));
        JsonValue ov = new ObjectValue();
        StringValue sv = new StringValue("test restore groovy bean");
        ov.addElement("value", sv);
        assertEquals("test restore groovy bean", ObjectBuilder.createObject(c, ov).toString());
    }

    public void testSerializeGroovyBean() throws Exception {
        GroovyClassLoader cl = new GroovyClassLoader();
        Class<?> c = cl.parseClass(Thread.currentThread().getContextClassLoader().getResourceAsStream("SimpleBean.groovy"));
        GroovyObject groovyObject = (GroovyObject)c.newInstance();
        groovyObject.invokeMethod("setValue", new Object[]{"test serialize groovy bean"});
        assertEquals("{\"value\":\"test serialize groovy bean\"}", JsonGenerator.createJsonObject(groovyObject)
                                                                                .toString());
    }

    @SuppressWarnings("rawtypes")
    public void testSerializeGroovyBean1() throws Exception {
        GroovyClassLoader cl = new GroovyClassLoader();
        Class c = cl.parseClass(Thread.currentThread().getContextClassLoader().getResourceAsStream("BookStorage.groovy"));
        GroovyObject groovyObject = (GroovyObject)c.newInstance();
        groovyObject.invokeMethod("initStorage", new Object[]{});

        JsonValue jsonValue = JsonGenerator.createJsonObject(groovyObject);
        //System.out.println(jsonValue);
        assertTrue(jsonValue.isObject());
        Iterator<JsonValue> iterator = jsonValue.getElement("books").getElements();
        assertEquals("JUnit in Action", iterator.next().getElement("title").getStringValue());
        assertEquals("Beginning C# 2008 from novice to professional", iterator.next().getElement("title")
                                                                              .getStringValue());
        assertEquals("Advanced JavaScript, Third Edition", iterator.next().getElement("title").getStringValue());
        assertFalse(iterator.hasNext());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testRestoreGroovyBean1() throws Exception {
        GroovyClassLoader cl = new GroovyClassLoader();
        Class c = cl.parseClass(Thread.currentThread().getContextClassLoader().getResourceAsStream("BookStorage.groovy"));
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "BookStorage.json")));
        JsonValue jv = jsonParser.getJsonObject();
        GroovyObject o = (GroovyObject)ObjectBuilder.createObject(c, jv);
        //System.out.println(o);
        List<GroovyObject> books = (List<GroovyObject>)o.getProperty("books");
        assertEquals(3, books.size());
        assertEquals(books.get(0).getProperty("title"), "JUnit in Action");
        assertEquals(books.get(1).getProperty("title"), "Beginning C# 2008 from novice to professional");
        assertEquals(books.get(2).getProperty("title"), "Advanced JavaScript. Third Edition");
    }

}
