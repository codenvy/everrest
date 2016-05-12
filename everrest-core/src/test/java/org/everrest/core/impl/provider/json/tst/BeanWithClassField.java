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
package org.everrest.core.impl.provider.json.tst;

public class BeanWithClassField {
    public static BeanWithClassField createBeanWithClassField() {
        BeanWithClassField beanWithClassField = new BeanWithClassField();
        beanWithClassField.setKlass(BeanWithClassField.class);
        return beanWithClassField;
    }

    private Class klass;

    public Class getKlass() {
        return klass;
    }

    public void setKlass(Class klass) {
        this.klass = klass;
    }
}