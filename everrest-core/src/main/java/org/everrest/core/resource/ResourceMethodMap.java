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
package org.everrest.core.resource;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
public class ResourceMethodMap<T extends ResourceMethodDescriptor> extends MultivaluedHashMap<String, T>
        implements ExtMultivaluedMap<String, T> {
    private static final long serialVersionUID = 8930689464134153848L;

    /**
     * Compare list of media types. Each list should be already sorted by {@link MediaTypeHelper#MEDIA_TYPE_COMPARATOR}. So it is enough to
     * compare only last media types in the list. Last media types is the least precise.
     */
    private static final Comparator<ResourceMethodDescriptor> RESOURCE_METHOD_COMPARATOR =
            new Comparator<ResourceMethodDescriptor>() {
                @Override
                public int compare(ResourceMethodDescriptor o1, ResourceMethodDescriptor o2) {
                    int r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(getLast(o1.consumes()), getLast(o2.consumes()));
                    if (r == 0) {
                        r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(getLast(o1.produces()), getLast(o2.produces()));
                    }
                    if (r == 0) {
                        r = o1.consumes().size() - o2.consumes().size();
                    }
                    if (r == 0) {
                        r = o1.produces().size() - o2.produces().size();
                    }
                    return r;
                }

                private MediaType getLast(List<MediaType> l) {
                    return l.get(l.size() - 1);
                }
            };


    @Override
    public List<T> getList(String httpMethod) {
        List<T> l = get(httpMethod);
        if (l == null) {
            l = new LinkedList<>();
            put(httpMethod, l);
        }
        return l;
    }

    /** Sort each collections in map. */
    public void sort() {
        for (List<T> l : values()) {
            Collections.sort(l, RESOURCE_METHOD_COMPARATOR);
        }
    }

    /**
     * Get HTTP method names to use it in 'Allow' header.
     *
     * @return collection of method names
     */
    public Collection<String> getAllow() {
        List<String> allowed = new LinkedList<>();
        for (Map.Entry<String, List<T>> e : entrySet()) {
            if (!e.getValue().isEmpty()) {
                allowed.add(e.getKey());
            }
        }
        return allowed;
    }
}