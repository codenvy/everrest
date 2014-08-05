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
package org.everrest.core.impl.provider;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This type should be used by resource methods when need to apply XSLT
 * transformation for returned {@link Source}.
 *
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: XLSTStreamingOutPut.java
 * @see StreamingOutput
 */
public class XSLTStreamingOutput implements StreamingOutput {
    private Source source;

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

    /** {@inheritDoc} . */
    @Override
    public void write(OutputStream output) throws IOException {
        try {
            Transformer transformer = templates.newTransformer();
            transformer.transform(source, new StreamResult(output));
        } catch (TransformerConfigurationException tce) {
            throw new IOException(tce.getMessage(), tce);
        } catch (TransformerException tre) {
            throw new IOException(tre.getMessage(), tre);
        }
    }
}
