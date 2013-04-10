/**
 * Copyright (C) 2010 eXo Platform SAS.
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
