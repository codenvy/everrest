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
package org.everrest.core.impl.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.everrest.core.impl.FileCollector;

public final class IOHelper {

  /** Default character set name. */
  @Deprecated private static final String DEFAULT_CHARSET_NAME = "UTF-8";

  /** If character set was not specified then this will be used. */
  @Deprecated private static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

  private IOHelper() {}

  /**
   * Read String from given {@link InputStream}.
   *
   * @param in source stream for reading
   * @param cs character set, if null then {@link #DEFAULT_CHARSET} will be used
   * @return resulting String
   * @throws IOException if i/o errors occurs
   */
  @Deprecated // Guava used instead in everrest-core, in other modules will be replaced with Guava
  // also
  public static String readString(InputStream in, String cs) throws IOException {
    Charset charset;
    // Must respect application specified character set.
    // For output if specified character set is not supported then UTF-8 should
    // be used instead.
    try {
      charset = cs != null ? Charset.forName(cs) : DEFAULT_CHARSET;
    } catch (Exception e) {
      charset = DEFAULT_CHARSET;
    }
    Reader r = new InputStreamReader(in, charset);
    char[] buf = new char[1024];
    StringBuilder sb = new StringBuilder();
    int rd;
    while ((rd = r.read(buf)) != -1) {
      sb.append(buf, 0, rd);
    }

    return sb.toString();
  }

  /**
   * Write String to {@link OutputStream}.
   *
   * @param s String
   * @param out See {@link OutputStream}
   * @param cs character set, if null then {@link #DEFAULT_CHARSET} will be used
   * @throws IOException if i/o errors occurs
   */
  @Deprecated // Guava used instead in everrest-core, in other modules will be replaced with Guava
  // also
  public static void writeString(String s, OutputStream out, String cs) throws IOException {
    Charset charset;
    // Must respect application specified character set.
    // For output if specified character set is not supported then UTF-8 should
    // be used instead.
    try {
      charset = cs != null ? Charset.forName(cs) : DEFAULT_CHARSET;
    } catch (Exception e) {
      charset = DEFAULT_CHARSET;
    }
    Writer w = new OutputStreamWriter(out, charset);
    try {
      w.write(s);
    } finally {
      w.flush();
    }
  }

  /**
   * Buffer input stream in memory of in file. If size of stream is less then <code>maxMemSize
   * </code> all data stored in memory otherwise stored in file.
   *
   * @param in source stream
   * @param maxMemSize max size of data to keep in memory
   * @return stream buffered in memory or in file
   * @throws IOException if any i/o error occurs
   */
  public static InputStream bufferStream(InputStream in, int maxMemSize) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int bytesNum;
    boolean overflow = false;
    while ((!overflow) && (bytesNum = in.read(buffer)) != -1) {
      bos.write(buffer, 0, bytesNum);
      overflow = bos.size() > maxMemSize;
    }

    if (overflow) {
      File file = FileCollector.getInstance().createFile();
      try (FileOutputStream fos = new FileOutputStream(file)) {
        bos.writeTo(fos);
        while ((bytesNum = in.read(buffer)) != -1) {
          fos.write(buffer, 0, bytesNum);
        }
      }
      return new DeleteOnCloseFIS(file);
    }
    return new ByteArrayInputStream(bos.toByteArray());
  }

  private static final class DeleteOnCloseFIS extends FileInputStream {
    private final File file;

    public DeleteOnCloseFIS(File file) throws FileNotFoundException {
      super(file);
      this.file = file;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        if (file.exists()) {
          file.delete();
        }
      }
    }
  }
}
