/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.assured.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/** Utility class for io operations. */
public class IoUtil {

    private static final Logger LOG = LoggerFactory.getLogger(IoUtil.class);

    public static String getResource(String resourceName) {

        InputStream stream = null;
        try {
            File file = new File(resourceName);
            if (file.isFile() && file.exists()) {
                stream = new FileInputStream(file);
            } else {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            }
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);

        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return "";
    }
}
