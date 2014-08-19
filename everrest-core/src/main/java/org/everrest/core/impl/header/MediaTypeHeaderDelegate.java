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
package org.everrest.core.impl.header;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author andrew00x
 */
public class MediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate<MediaType> {

    @Override
    public MediaType fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        try {
            int p = header.indexOf('/');
            int col = header.indexOf(';');

            String type;
            String subType = null;

            if (p < 0 && col < 0) // no '/' and ';'
            {
                return new MediaType(header, null);
            } else if (p > 0 && col < 0) // there is no ';' so no parameters
            {
                return new MediaType(HeaderHelper.removeWhitespaces(header.substring(0, p)),
                                     HeaderHelper.removeWhitespaces(header.substring(p + 1)));
            } else if (p < 0 && col == 0) { // string just start from ';'
                type = null;
                // sub-type is null
            } else if (p < 0 && col > 0) { // there is no '/' but present ';'
                type = HeaderHelper.removeWhitespaces(header.substring(0, col));
                // sub-type is null
            } else { // presents '/' and ';'
                type = HeaderHelper.removeWhitespaces(header.substring(0, p));
                subType = header.substring(p + 1, col);
            }

            Map<String, String> m = new HeaderParameterParser().parse(header);
            return new MediaType(type, subType, m);

        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public String toString(MediaType mime) {
        if (mime == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mime.getType()).append('/').append(mime.getSubtype());

        for (Entry<String, String> entry : mime.getParameters().entrySet()) {
            sb.append(';').append(entry.getKey()).append('=');
            HeaderHelper.appendWithQuote(sb, entry.getValue());
        }

        return sb.toString();
    }
}
