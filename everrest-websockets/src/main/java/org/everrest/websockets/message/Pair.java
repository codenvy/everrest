/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.everrest.websockets.message;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.HeaderHelper;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class Pair {
    public static MultivaluedMap<String, String> toMap(Pair[] pairs) {
        MultivaluedMap<String, String> result = new MultivaluedMapImpl();
        if (!(pairs == null || pairs.length == 0)) {
            for (Pair p : pairs) {
                result.add(p.getName(), p.getValue());
            }
        }
        return result;
    }

    public static Pair[] fromMap(MultivaluedMap<String, Object> source) {
        if (!(source == null || source.isEmpty())) {
            List<Pair> list = new ArrayList<Pair>();
            for (String key : source.keySet()) {
                List<Object> values = source.get(key);
                if (!(values == null || values.isEmpty())) {
                    for (Object v : values) {
                        list.add(new Pair(key, HeaderHelper.getHeaderAsString(v)));
                    }
                } else {
                    list.add(new Pair(key, null));
                }
            }
            return list.toArray(new Pair[list.size()]);
        }
        return new Pair[0];
    }

    private String name;
    private String value;

    public Pair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Pair() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" +
               "name='" + name + '\'' +
               ", value='" + value + '\'' +
               '}';
    }
}
