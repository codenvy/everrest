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
package org.everrest.websockets.message;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.HeaderHelper;

/** @author andrew00x */
public final class Pair {
  public static MultivaluedMap<String, String> toMap(Pair[] pairs) {
    final MultivaluedMap<String, String> result = new MultivaluedMapImpl();
    if (!(pairs == null || pairs.length == 0)) {
      for (Pair p : pairs) {
        result.add(p.getName(), p.getValue());
      }
    }
    return result;
  }

  public static Pair[] fromMap(MultivaluedMap<String, Object> source) {
    if (!(source == null || source.isEmpty())) {
      final List<Pair> list = new ArrayList<>();
      for (String key : source.keySet()) {
        List<Object> values = source.get(key);
        if (!(values == null || values.isEmpty())) {
          for (Object v : values) {
            list.add(Pair.of(key, HeaderHelper.getHeaderAsString(v)));
          }
        } else {
          list.add(Pair.of(key, null));
        }
      }
      return list.toArray(new Pair[list.size()]);
    }
    return new Pair[0];
  }

  public static Pair of(String name, String value) {
    return new Pair(name, value);
  }

  private String name;
  private String value;

  public Pair(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public Pair() {}

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
    return "Pair{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
  }
}
