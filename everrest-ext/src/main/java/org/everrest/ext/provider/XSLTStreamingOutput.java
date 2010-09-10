/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.ext.provider;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

/**
 * This type should be used by resource methods when need to apply XSLT
 * transformation for returned {@link Source}.
 *
 * @see StreamingOutput
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: XLSTStreamingOutPut.java
 */
public class XSLTStreamingOutput implements StreamingOutput
{

   private Source source;

   private Templates templates;

   /**
    * XSLTStreamingOutput constructor.
    *
    * @param source entity to write into output stream.
    * @param templates transformation templates
    */
   public XSLTStreamingOutput(Source source, Templates templates)
   {
      this.source = source;
      this.templates = templates;
   }

   /**
    * {@inheritDoc} .
    */
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      try
      {
         Transformer transformer = templates.newTransformer();
         transformer.transform(source, new StreamResult(output));
      }
      catch (TransformerConfigurationException tce)
      {
         throw new IOException(tce.getMessage(), tce);
      }
      catch (TransformerException tre)
      {
         throw new IOException(tre.getMessage(), tre);
      }
   }

}
