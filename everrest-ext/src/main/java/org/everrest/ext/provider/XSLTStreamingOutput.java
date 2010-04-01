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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.xml.transform.NotSupportedIOTypeException;
import org.exoplatform.services.xml.transform.impl.trax.TRAXTemplatesServiceImpl;
import org.exoplatform.services.xml.transform.trax.TRAXTemplates;
import org.exoplatform.services.xml.transform.trax.TRAXTemplatesService;
import org.exoplatform.services.xml.transform.trax.TRAXTransformer;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
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

   private String schemeName;

   private Source source;

   /**
    * XSLTStreamingOutput constructor.
    * 
    * @param schemeName XLST scheme name. Must be registered in
    *          {@link org.exoplatform.services.xml.transform.impl.trax.TRAXTemplatesLoaderPlugin
    *          TRAXTemplatesLoaderPlugin }
    * @param source entity to write into output stream.
    */
   public XSLTStreamingOutput(String schemeName, Source source)
   {

      this.schemeName = schemeName;
      this.source = source;

   }

   /**
    * {@inheritDoc} .
    */
   public void write(OutputStream outStream) throws IOException, WebApplicationException
   {

      ExoContainer container = ExoContainerContext.getCurrentContainer();

      TRAXTemplatesService templatesService =
         (TRAXTemplatesService)container.getComponentInstanceOfType(TRAXTemplatesServiceImpl.class);

      try
      {
         TRAXTransformer transformer = null;
         if (schemeName != null)
         {
            TRAXTemplates t = templatesService.getTemplates(schemeName);
            if (t == null)
            {
               String msg = "Template " + schemeName + " not found.";
               throw new IllegalArgumentException(msg);
            }
            transformer = t.newTransformer();
         }
         else
         {
            String msg = "XSLT scheme name is null.";
            throw new NullPointerException(msg);
         }
         transformer.initResult(new StreamResult(outStream));
         transformer.transform(source);
      }
      catch (TransformerConfigurationException tce)
      {
         throw new IOException("Can't write to output stream " + tce);
      }
      catch (NotSupportedIOTypeException nse)
      {
         throw new IOException("Can't write to output stream " + nse);
      }
      catch (TransformerException tre)
      {
         throw new IOException("Can't write to output stream " + tre);
      }
   }

}
