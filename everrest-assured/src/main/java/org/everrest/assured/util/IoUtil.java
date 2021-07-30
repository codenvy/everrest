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
package org.everrest.assured.util;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
