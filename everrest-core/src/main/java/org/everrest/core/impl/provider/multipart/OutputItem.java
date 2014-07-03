/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.provider.multipart;

import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
public class OutputItem {
    public static OutputItem create(String name, Object entity, Class<?> type, Type genericType, MediaType mediaType, String fileName) {
        return new OutputItem(name, entity, type, genericType, mediaType, fileName);
    }

    public static OutputItem create(String name, Object entity, Type genericType, MediaType mediaType, String fileName) {
        return new OutputItem(name, entity, entity.getClass(), genericType, mediaType, fileName);
    }

    public static OutputItem create(String name, Object entity, MediaType mediaType, String fileName) {
        return new OutputItem(name, entity, entity.getClass(), null, mediaType, fileName);
    }

    public static OutputItem create(String name, Object entity, MediaType mediaType) {
        return new OutputItem(name, entity, entity.getClass(), null, mediaType, null);
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
}
