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
package org.everrest.core.impl.provider.ext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

class InMemoryItemFactory implements FileItemFactory {
  private final int maxSize;

  public InMemoryItemFactory(int maxSize) {
    this.maxSize = maxSize;
  }

  @Override
  public FileItem createItem(
      String fieldName, String contentType, boolean isFormField, String fileName) {
    return new InMemoryFileItem(contentType, fieldName, isFormField, fileName, maxSize);
  }
}
