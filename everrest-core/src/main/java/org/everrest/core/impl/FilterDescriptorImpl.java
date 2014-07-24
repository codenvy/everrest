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

import org.everrest.core.BaseObjectModel;
import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;

/**
 * @author andrew00x
 */
public class FilterDescriptorImpl extends BaseObjectModel implements FilterDescriptor {
    public static final String DEFAULT_PATH = "/{path:.*}";
    /** @see PathValue */
    private final PathValue path;

    /** @see UriPattern */
    private final UriPattern uriPattern;

    /**
     * @param filterClass
     *         filter class
     * @param scope
     *         filter scope
     * @see ComponentLifecycleScope
     */
    public FilterDescriptorImpl(Class<?> filterClass, ComponentLifecycleScope scope) {
        super(filterClass, scope);
        final Path p = filterClass.getAnnotation(Path.class);
        if (p != null) {
            this.path = new PathValue(p.value());
            this.uriPattern = new UriPattern(p.value());
        } else {
            this.path = new PathValue(DEFAULT_PATH);
            this.uriPattern = new UriPattern(this.path.getPath());
        }
    }

    public void accept(ResourceDescriptorVisitor visitor) {
        visitor.visitFilterDescriptor(this);
    }

    public PathValue getPathValue() {
        return path;
    }

    public UriPattern getUriPattern() {
        return uriPattern;
    }

    @Override
    public String toString() {
        return "[ FilterDescriptorImpl: " + "path: " + getPathValue() + "; filter class: " + getObjectClass() + "; " +
               getConstructorDescriptors() + "; " + getFieldInjectors() + " ]";
    }
}
