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
package org.everrest.guice;

import com.google.inject.Key;
import java.lang.annotation.Annotation;

/**
 * Guice key that allows remap URI template of service. Example of usage in guice module:
 *
 * <pre>
 *     bind(ServiceBindingKey.of(MyService.class, "/my_path")).to(MyService.class);
 * </pre>
 *
 * @author andrew00x
 */
public class ServiceBindingHelper {
  private ServiceBindingHelper() {}

  public static <T> Key<T> bindingKey(Class<T> clazz, String path) {
    return Key.get(clazz, new BindingPathImpl(path));
  }

  static class BindingPathImpl implements BindingPath {
    private final String value;

    BindingPathImpl(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return BindingPath.class;
    }

    @Override
    public int hashCode() {
      return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof BindingPath && value.equals(((BindingPath) o).value());
    }

    @Override
    public String toString() {
      return "@BindingPath('" + value + "')";
    }
  }
}
