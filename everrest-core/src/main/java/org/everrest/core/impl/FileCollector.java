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
package org.everrest.core.impl;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Provides store for temporary files.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class FileCollector {
    private static final String PREF = "everrest";
    private static final String SUFF = ".tmp";

    private static class FileCollectorHolder {
        private static final String        name      = PREF + Long.toString(Math.abs(new SecureRandom().nextLong()));
        private static final FileCollector collector = new FileCollector( //
                                                                          new File(System.getProperty("java.io.tmpdir"), name));
    }

    public static FileCollector getInstance() {
        return FileCollectorHolder.collector;
    }

    private final File store;
    private final Thread cleaner = new Thread() {
        public void run() {
            clean();
        }
    };

    private FileCollector(File store) {
        this.store = store;
        try {
            Runtime.getRuntime().addShutdownHook(cleaner);
        } catch (IllegalStateException ignored) {
        }
    }

    /** Clean all files in storage. */
    public void clean() {
        if (store.exists()) {
            delete(store);
        }
    }

    public void stop() {
        clean();
        try {
            Runtime.getRuntime().removeShutdownHook(cleaner);
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * Create file with specified <code>fileName</code> in storage.
     *
     * @param fileName
     *         file name
     * @return newly created file
     * @throws IOException
     *         if any i/o error occurs
     */
    public File createFile(String fileName) throws IOException {
        checkStore();
        return new File(store, fileName);
    }

    /**
     * Create new file with generated name in storage.
     *
     * @return newly created file
     * @throws IOException
     *         if any i/o error occurs
     */
    public File createFile() throws IOException {
        checkStore();
        return File.createTempFile(PREF, SUFF, store);
    }

    public File getStore() {
        checkStore();
        return store;
    }

    private void checkStore() {
        if (!store.exists()) {
            store.mkdirs();
        }
    }

    private void delete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children.length > 0) {
                for (File ch : children) {
                    delete(ch);
                }
            }
        }
        fileOrDirectory.delete();
    }
}
