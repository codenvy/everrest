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
package org.everrest.core.util;

import static com.google.common.collect.Iterables.getLast;

import java.util.Comparator;
import org.everrest.core.resource.ResourceMethodDescriptor;

/**
 * Compare list of media types. Each list should be already sorted by {@link MediaTypeComparator}.
 * So it is enough to compare only last media types in the list. Last media types is the least
 * precise.
 */
public class ResourceMethodComparator implements Comparator<ResourceMethodDescriptor> {
  private final MediaTypeComparator mediaTypeComparator;

  public ResourceMethodComparator() {
    this(new MediaTypeComparator());
  }

  ResourceMethodComparator(MediaTypeComparator mediaTypeComparator) {
    this.mediaTypeComparator = mediaTypeComparator;
  }

  @Override
  public int compare(
      ResourceMethodDescriptor resourceMethodOne, ResourceMethodDescriptor resourceMethodTwo) {
    int result =
        mediaTypeComparator.compare(
            getLast(resourceMethodOne.consumes()), getLast(resourceMethodTwo.consumes()));
    if (result == 0) {
      result =
          mediaTypeComparator.compare(
              getLast(resourceMethodOne.produces()), getLast(resourceMethodTwo.produces()));
    }
    if (result == 0) {
      result = resourceMethodOne.consumes().size() - resourceMethodTwo.consumes().size();
    }
    if (result == 0) {
      result = resourceMethodOne.produces().size() - resourceMethodTwo.produces().size();
    }
    return result;
  }
}
