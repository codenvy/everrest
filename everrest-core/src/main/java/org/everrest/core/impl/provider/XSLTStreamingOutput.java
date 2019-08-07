/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This type should be used by resource methods when need to apply XSLT transformation for returned {@link Source}.
 *
 * @author Dmytro Katayev
 */
public class XSLTStreamingOutput implements StreamingOutput {
    private Source    source;
    private Templates templates;

    /**
     * XSLTStreamingOutput constructor.
     *
     * @param source
     *         entity to write into output stream.
     * @param templates
     *         transformation templates
     */
    public XSLTStreamingOutput(Source source, Templates templates) {
        this.source = source;
        this.templates = templates;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        try {
            Transformer transformer = templates.newTransformer();
            transformer.transform(source, new StreamResult(output));
        } catch (TransformerException tre) {
            throw new IOException(tre.getMessage(), tre);
        }
    }
}
