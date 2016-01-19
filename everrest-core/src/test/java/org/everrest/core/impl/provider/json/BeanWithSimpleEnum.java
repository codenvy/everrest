/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider.json;

import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BeanWithSimpleEnum {
    private String name;

    private StringEnum count;

    private StringEnum[] counts;

    private List<StringEnum> countList;

    public StringEnum getCount() {
        return count;
    }

    public List<StringEnum> getCountList() {
        return countList;
    }

    public StringEnum[] getCounts() {
        return counts;
    }

    public String getName() {
        return name;
    }

    public void setCount(StringEnum count) {
        this.count = count;
    }

    public void setCountList(List<StringEnum> countList) {
        this.countList = countList;
    }

    public void setCounts(StringEnum[] counts) {
        this.counts = counts;
    }

    public void setName(String name) {
        this.name = name;
    }
}
