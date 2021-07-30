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
package org.everrest.core.impl.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class XSLTStreamingOutputTest {
  private Templates templates;
  private Transformer transformer;
  private Source source;

  private XSLTStreamingOutput xsltStreamingOutput;

  @Before
  public void setUp() throws Exception {
    templates = mock(Templates.class);
    transformer = mock(Transformer.class);
    when(templates.newTransformer()).thenReturn(transformer);

    source = mock(Source.class);
    xsltStreamingOutput = new XSLTStreamingOutput(source, templates);
  }

  @Test
  public void writesTransformedContentToOutputStream() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    final String transformedContent = "<xml/>";
    doAnswer(doTransformation(out, transformedContent))
        .when(transformer)
        .transform(same(source), isA(StreamResult.class));

    xsltStreamingOutput.write(out);

    assertEquals(transformedContent, out.toString());
  }

  private Answer doTransformation(ByteArrayOutputStream out, String testContent) {
    return invocation -> {
      out.write(testContent.getBytes());
      return null;
    };
  }

  @Test(expected = IOException.class)
  public void throwsIoExceptionWhenCreationOfTransformerFromTemplateFails() throws Exception {
    when(templates.newTransformer()).thenThrow(new TransformerConfigurationException());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    xsltStreamingOutput.write(out);
  }

  @Test(expected = IOException.class)
  public void throwsIoExceptionWhenTransformationFails() throws Exception {
    doThrow(TransformerException.class)
        .when(transformer)
        .transform(same(source), isA(StreamResult.class));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    xsltStreamingOutput.write(out);
  }
}
