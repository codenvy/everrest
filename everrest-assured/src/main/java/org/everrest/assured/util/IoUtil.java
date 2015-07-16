/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
