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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FileSystemScriptFinder implements ScriptFinder {
    /** {@inheritDoc} */
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
        List<URL> files = new ArrayList<URL>();
        LinkedList<File> q = new LinkedList<File>();
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
