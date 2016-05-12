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
package org.everrest.core.util;

import org.everrest.core.resource.ResourceMethodDescriptor;

import java.util.Comparator;

import static com.google.common.collect.Iterables.getLast;

/**
 * Compare list of media types. Each list should be already sorted by {@link MediaTypeComparator}. So it is enough to
 * compare only last media types in the list. Last media types is the least precise.
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
    public int compare(ResourceMethodDescriptor resourceMethodOne, ResourceMethodDescriptor resourceMethodTwo) {
        int result = mediaTypeComparator.compare(getLast(resourceMethodOne.consumes()), getLast(resourceMethodTwo.consumes()));
        if (result == 0) {
            result = mediaTypeComparator.compare(getLast(resourceMethodOne.produces()), getLast(resourceMethodTwo.produces()));
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
