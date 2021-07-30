/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;
import java.io.Writer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonWriterTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private JsonWriter jsonWriter;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    writer = new StringWriter();
    jsonWriter = new JsonWriter(writer);
  }

  @Test
  public void writesEmptyObject() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    assertEquals("{}", writer.toString());
  }

  @Test
  public void writesKeyBeforeObject() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    jsonWriter.writeEndObject();
    assertEquals("{\"key\":{}}", writer.toString());
  }

  @Test
  public void writesObjectsSeparatedByComma() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key1");
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    jsonWriter.writeKey("key2");
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    jsonWriter.writeEndObject();
    assertEquals("{\"key1\":{},\"key2\":{}}", writer.toString());
  }

  @Test
  public void writesEmptyArray() throws Exception {
    jsonWriter.writeStartArray();
    jsonWriter.writeEndArray();
    assertEquals("[]", writer.toString());
  }

  @Test
  public void writesObjectsAsItemsOfArray() throws Exception {
    jsonWriter.writeStartArray();
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    jsonWriter.writeStartObject();
    jsonWriter.writeEndObject();
    jsonWriter.writeEndArray();
    assertEquals("[{},{}]", writer.toString());
  }

  @Test
  public void failsWriteStartObjectWhenItDoesNotStartRootObjectOrDoesNotFollowKey()
      throws Exception {
    jsonWriter.writeStartObject();

    thrown.expect(JsonException.class);
    jsonWriter.writeStartObject();
  }

  @Test
  public void failsWhenEndObjectIsBeforeStartObject() throws Exception {
    thrown.expect(JsonException.class);
    jsonWriter.writeEndObject();
  }

  @Test
  public void writesStartArrayAfterKey() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeStartArray();
    jsonWriter.writeEndArray();
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":[]}", writer.toString());
  }

  @Test
  public void writesMultiDimensionArray() throws Exception {
    jsonWriter.writeStartArray();
    jsonWriter.writeStartArray();
    jsonWriter.writeEndArray();
    jsonWriter.writeStartArray();
    jsonWriter.writeEndArray();
    jsonWriter.writeEndArray();

    assertEquals("[[],[]]", writer.toString());
  }

  @Test
  public void failsWriteStartArrayWhenItDoesNotStartRootArrayOrDoesNotFollowKey() throws Exception {
    jsonWriter.writeStartObject();

    thrown.expect(JsonException.class);
    jsonWriter.writeStartArray();
  }

  @Test
  public void failsWhenEndArrayIsBeforeStartArray() throws Exception {
    thrown.expect(JsonException.class);
    jsonWriter.writeEndArray();
  }

  @Test
  public void failsWhenOneFollowsOther() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key1");

    thrown.expect(JsonException.class);
    jsonWriter.writeKey("key2");
  }

  @Test
  public void failsWhenKeyInsideArray() throws Exception {
    jsonWriter.writeStartArray();

    thrown.expect(JsonException.class);
    jsonWriter.writeKey("key");
  }

  @Test
  public void failsWhenKeyIsNull() throws Exception {
    jsonWriter.writeStartArray();

    thrown.expect(JsonException.class);
    jsonWriter.writeKey(null);
  }

  @Test
  public void failsWhenKeyIsEmpty() throws Exception {
    jsonWriter.writeStartArray();

    thrown.expect(JsonException.class);
    jsonWriter.writeKey("");
  }

  @Test
  public void failsWhenKeyContainsOnlySpaces() throws Exception {
    jsonWriter.writeStartArray();

    thrown.expect(JsonException.class);
    jsonWriter.writeKey("  ");
  }

  @Test
  public void writesStringValue() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeValue("value");
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":\"value\"}", writer.toString());
  }

  @Test
  public void writesLongValue() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeValue(777);
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":777}", writer.toString());
  }

  @Test
  public void writesDoubleValue() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeValue(7.77);
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":7.77}", writer.toString());
  }

  @Test
  public void writesBooleanValue() throws Exception {
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeValue(true);
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":true}", writer.toString());
  }

  @Test
  public void skipsNullValueByDefault() throws Exception {
    assertFalse(jsonWriter.isWriteNulls());
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key1");
    jsonWriter.writeNull();
    jsonWriter.writeKey("key2");
    jsonWriter.writeValue("value");
    jsonWriter.writeEndObject();

    assertEquals("{\"key2\":\"value\"}", writer.toString());
  }

  @Test
  public void writesNullValueWhenWritingNullsIsAllowed() throws Exception {
    jsonWriter.setWriteNulls(true);
    jsonWriter.writeStartObject();
    jsonWriter.writeKey("key");
    jsonWriter.writeNull();
    jsonWriter.writeKey("key2");
    jsonWriter.writeValue("value");
    jsonWriter.writeEndObject();

    assertEquals("{\"key\":null,\"key2\":\"value\"}", writer.toString());
  }

  @Test
  public void writesArrayOfValues() throws Exception {
    jsonWriter.writeStartArray();
    jsonWriter.writeValue("value1");
    jsonWriter.writeValue("value2");
    jsonWriter.writeEndArray();

    assertEquals("[\"value1\",\"value2\"]", writer.toString());
  }

  @Test
  public void failsWriteValueWhenItDoesNotFollowKeyOrIsNotItemOfArray() throws Exception {
    jsonWriter.writeStartObject();

    thrown.expect(JsonException.class);
    jsonWriter.writeValue("value");
  }

  @Test
  public void flushesUnderlyingWriter() throws Exception {
    Writer writer = mock(Writer.class);
    jsonWriter = new JsonWriter(writer);

    jsonWriter.flush();
    verify(writer).flush();
  }

  @Test
  public void closesUnderlyingWriter() throws Exception {
    Writer writer = mock(Writer.class);
    jsonWriter = new JsonWriter(writer);

    jsonWriter.close();
    verify(writer).close();
  }
}
