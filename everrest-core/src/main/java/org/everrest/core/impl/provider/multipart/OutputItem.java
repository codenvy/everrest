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
package org.everrest.core.impl.provider.multipart;

import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
public class OutputItem {
    public static OutputItemBuilder anOutputItem() {
        return new OutputItemBuilder();
    }

    private final String                         name;
    private final Object                         entity;
    private final Class<?>                       type;
    private final Type                           genericType;
    private final MediaType                      mediaType;
    private final String                         filename;
    private final MultivaluedMap<String, String> headers;

    public OutputItem(String name, Object entity, Class<?> type, Type genericType, MediaType mediaType, String filename) {
        this.name = name;
        this.entity = entity;
        this.type = type;
        this.genericType = genericType;
        this.mediaType = mediaType;
        this.filename = filename;
        headers = new MultivaluedMapImpl();
    }

    public String getName() {
        return name;
    }

    public Object getEntity() {
        return entity;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getFilename() {
        return filename;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public static class OutputItemBuilder {
        private String name;
        private Object    entity;
        private Class<?>  type;
        private Type      genericType;
        private MediaType mediaType;
        private String    filename;

        public OutputItemBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public OutputItemBuilder withEntity(Object entity) {
            this.entity = entity;
            return this;
        }

        public OutputItemBuilder withMediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public OutputItemBuilder withFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public OutputItemBuilder withType(Class<?> type) {
            this.type = type;
            return this;
        }

        public OutputItemBuilder withGenericType(Type genericType) {
            this.genericType = genericType;
            return this;
        }

        public OutputItem build() {
            return new OutputItem(name, entity, type == null ? entity.getClass() : type, genericType, mediaType, filename);
        }
    }
}
