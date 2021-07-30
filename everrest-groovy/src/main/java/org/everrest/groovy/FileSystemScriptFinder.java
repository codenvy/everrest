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
package org.everrest.groovy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Scanner of Groovy scripts on local file system.
 *
 * @author andrew00x
 */
public class FileSystemScriptFinder implements ScriptFinder {

  @Override
  public URL[] find(URLFilter filter, URL root) throws MalformedURLException {
    // Be sure protocol is supported.
    if ("file".equals(root.getProtocol())) {
      File file = new File(URI.create(root.toString()));
      if (file.isDirectory()) {
        return find(file, filter);
      }
    }
    return new URL[0];
  }

  private URL[] find(File directory, URLFilter filter) throws MalformedURLException {
    List<URL> files = new ArrayList<>();
    LinkedList<File> q = new LinkedList<>();
    q.add(directory);
    while (!q.isEmpty()) {
      File current = q.pop();
      File[] list = current.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          final File f = list[i];
          if (f.isDirectory()) {
            q.push(f);
          } else {
            URL url = f.toURI().toURL();
            if (filter.accept(url)) {
              files.add(url);
            }
          }
        }
      }
    }
    return files.toArray(new URL[files.size()]);
  }
}
