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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@RunWith(DataProviderRunner.class)
public class JsonParserTest {

    private JsonParser  parser;
    private JsonHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = mock(JsonHandler.class);
        parser = new JsonParser(handler);
    }

    @Test
    public void parsesArrayOfStringValues() throws Exception {
        String jsonString = "[\"JUnit in Action\",\"Advanced JavaScript\",\"Beginning C# 2008\"]";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("\"JUnit in Action\"".toCharArray());
        inOrder.verify(handler).characters("\"Advanced JavaScript\"".toCharArray());
        inOrder.verify(handler).characters("\"Beginning C# 2008\"".toCharArray());
        inOrder.verify(handler).endArray();
    }

    @Test
    public void parsesArrayOfNumericValues() throws Exception {
        String jsonString = "[1, 123, 123456789, 0xAA, 0x123456, 077, 0123456, -123456789]";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("1".toCharArray());
        inOrder.verify(handler).characters("123".toCharArray());
        inOrder.verify(handler).characters("123456789".toCharArray());
        inOrder.verify(handler).characters("0xAA".toCharArray());
        inOrder.verify(handler).characters("0x123456".toCharArray());
        inOrder.verify(handler).characters("077".toCharArray());
        inOrder.verify(handler).characters("0123456".toCharArray());
        inOrder.verify(handler).characters("-123456789".toCharArray());
        inOrder.verify(handler).endArray();
    }

    @Test
    public void parsesArrayOfMixedValues() throws Exception {
        String jsonString = "[1.0, \"to be or not to be\", 111, true, {\"object\":{\"foo\":\"bar\"}}]";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("1.0".toCharArray());
        inOrder.verify(handler).characters("\"to be or not to be\"".toCharArray());
        inOrder.verify(handler).characters("111".toCharArray());
        inOrder.verify(handler).characters("true".toCharArray());
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("object");
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"bar\"".toCharArray());
        inOrder.verify(handler, times(2)).endObject();
        inOrder.verify(handler).endArray();
    }

    @Test
    public void parsesObject() throws Exception {
        String jsonString = "{\"foo\":\"bar\", \"book\":{\"author\":\"Christian Gross\",\"title\":\"Beginning C# 2008\"}," +
                            "\"foo1\":[\"bar1\"]}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"bar\"".toCharArray());
        inOrder.verify(handler).key("book");
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("author");
        inOrder.verify(handler).characters("\"Christian Gross\"".toCharArray());
        inOrder.verify(handler).key("title");
        inOrder.verify(handler).characters("\"Beginning C# 2008\"".toCharArray());
        inOrder.verify(handler).endObject();
        inOrder.verify(handler).key("foo1");
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("\"bar1\"".toCharArray());
        inOrder.verify(handler).endArray();
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesMultiDimensionArray() throws Exception {
        String jsonString = "[\"foo0\", [\"foo1\", \"bar1\", [\"foo2\", \"bar2\"]], \"bar0\"]";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("\"foo0\"".toCharArray());
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("\"foo1\"".toCharArray());
        inOrder.verify(handler).characters("\"bar1\"".toCharArray());
        inOrder.verify(handler).startArray();
        inOrder.verify(handler).characters("\"foo2\"".toCharArray());
        inOrder.verify(handler).characters("\"bar2\"".toCharArray());
        inOrder.verify(handler, times(2)).endArray();
        inOrder.verify(handler).characters("\"bar0\"".toCharArray());
        inOrder.verify(handler).endArray();
    }

    @Test
    public void skipsSingleLineComment() throws Exception {
        String jsonString = "{\"foo\":" +
                            "// comment\n" +
                            "\"bar\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"bar\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void skipsMultiLineComment() throws Exception {
        String jsonString = "{\"foo\":" +
                            "/* comment\n" +
                            " continue comment\n" +
                            " */\n" +
                            "\"bar\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"bar\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainTabs() throws Exception {
        String jsonString = "{\"foo\": \"ba\\tr\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"ba\tr\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainLineSeparator() throws Exception {
        String jsonString = "{\"foo\": \"ba\\nr\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"ba\nr\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainCarriageReturn() throws Exception {
        String jsonString = "{\"foo\": \"ba\\rr\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"ba\rr\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainBackspace() throws Exception {
        String jsonString = "{\"foo\": \"ba\\br\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"ba\br\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainUnicodeString() throws Exception {
        String jsonString = "{\"foo\": \"\\u041f\\u0440\\u0438\\u0432\\u0456\\u0442\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"\u041f\u0440\u0438\u0432\u0456\u0442\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test
    public void parsesJsonWhenStringValuesContainFormFeed() throws Exception {
        String jsonString = "{\"foo\": \"ba\\fr\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));

        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startObject();
        inOrder.verify(handler).key("foo");
        inOrder.verify(handler).characters("\"ba\fr\"".toCharArray());
        inOrder.verify(handler).endObject();
    }

    @Test(expected = JsonException.class)
    public void failsWhenMultiLineCommentIsNotEndedProperly() throws Exception {
        String jsonString = "{\"foo\":" +
                            "/* comment\n" +
                            " continue comment\n" +
                            "\"bar\"}";
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
    }

    @DataProvider
    public static Object[][] invalidJson() {
        return new Object[][] {
                {"\"foo\":\"bar\"}"},
                {"{\"foo\":\"bar\""},
                {"{\"\":\"bar\"}"},
                {"{foo:\"bar\"}"},
                {"{\"foo\":\"bar}"},
                {"{\"foo\":\"bar"},
                {"{\"foo\":\"bar\n\"}"},
                {"{\"foo\":\"bar\\}"},
                {"{\"foo\":\"bar\\"},
                {"{foo\":\"bar\"}"},
                {"{\"foo:\"bar\"}"},
                {"{\"foo\"\"bar\"}"},
                {"{\"foo\":\"bar\"]"},
                {"{\"foo\":\"bar\"]"},
                {"{\"foo\":\"bar\"},"},
                {"{\"foo\":\"bar\"}{"},
                {"[\"foo\",\"bar\""},
                {"[\"foo\",\"bar\"}"},
                {"[\"foo\",\"bar\"]["},
                {"[\"foo\",\"]"},
                {"[\"foo\""},
                {"{\"foo\": \"\\u041f\\u0440\\u043\\u0432\\u0456\\u0442\"}"},
                {"{\"foo\": \"\\u041f\\u0440\\u0438\\u0432\\u0456\\u044"}
        };
    }

    @UseDataProvider("invalidJson")
    @Test(expected = JsonException.class)
    public void failsParseInvalidJson(String jsonString) throws Exception {
        parser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
    }
}
