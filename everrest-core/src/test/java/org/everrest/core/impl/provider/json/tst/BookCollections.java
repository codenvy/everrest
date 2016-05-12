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

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.everrest.core.impl.provider.json.tst.Book.createCSharpBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJavaScriptBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJunitBook;

public class BookCollections {

    public static BookCollections createBookCollections() {
        BookCollections bookCollections = new BookCollections();
        bookCollections.setList(newArrayList(createJunitBook(), createCSharpBook(), createJavaScriptBook()));
        bookCollections.setListList(newArrayList(newArrayList(createJunitBook(), createJavaScriptBook()),
                                                 newArrayList(createCSharpBook(), createJavaScriptBook()),
                                                 newArrayList(createJunitBook(), createCSharpBook())));
        bookCollections.setListMap(newArrayList(ImmutableMap.of("1", createJunitBook(), "3", createJavaScriptBook()),
                                                ImmutableMap.of("2", createCSharpBook(), "3", createJavaScriptBook()),
                                                ImmutableMap.of("1", createJunitBook(), "2", createCSharpBook())));
        bookCollections.setListArray(newArrayList(new Book[]{createJunitBook(), createJavaScriptBook()},
                                                  new Book[]{createJunitBook(), createCSharpBook()},
                                                  new Book[]{createCSharpBook(), createJavaScriptBook()}));
        bookCollections.setSet(newHashSet(createJunitBook(), createCSharpBook(), createJavaScriptBook()));
        bookCollections.setSetSet(newHashSet(newHashSet(createJunitBook(), createJavaScriptBook()),
                                             newHashSet(createCSharpBook(), createJavaScriptBook()),
                                             newHashSet(createJunitBook(), createCSharpBook())));
        bookCollections.setSetMap(newHashSet(ImmutableMap.of("1", createJunitBook(), "3", createJavaScriptBook()),
                                             ImmutableMap.of("2", createCSharpBook(), "3", createJavaScriptBook()),
                                             ImmutableMap.of("1", createJunitBook(), "2", createCSharpBook())));
        bookCollections.setSetArray(newHashSet(new Book[]{createJunitBook(), createJavaScriptBook()},
                                               new Book[]{createJunitBook(), createCSharpBook()},
                                               new Book[]{createCSharpBook(), createJavaScriptBook()}));
        bookCollections.setMap(ImmutableMap.of("1", createJunitBook(), "2", createCSharpBook(), "3", createJavaScriptBook()));
        bookCollections.setMapArray(ImmutableMap.of("1", new Book[]{createJunitBook(), createJavaScriptBook()},
                                                    "2", new Book[]{createJunitBook(), createCSharpBook()},
                                                    "3", new Book[]{createCSharpBook(), createJavaScriptBook()}));
        bookCollections.setMapList(ImmutableMap.of("1", newArrayList(createJunitBook(), createJavaScriptBook()),
                                                   "2", newArrayList(createJunitBook(), createCSharpBook()),
                                                   "3", newArrayList(createCSharpBook(), createJavaScriptBook())));
        bookCollections.setMapMap(ImmutableMap.of("1", ImmutableMap.of("1", createJunitBook(), "2", createCSharpBook()),
                                                  "2", ImmutableMap.of("1", createJunitBook(), "3", createJavaScriptBook()),
                                                  "3", ImmutableMap.of("2", createCSharpBook(), "3", createJavaScriptBook())));
        bookCollections.setListEnum(newArrayList(BookEnum.JUNIT_IN_ACTION, BookEnum.BEGINNING_C, BookEnum.ADVANCED_JAVA_SCRIPT));
        return bookCollections;
    }

    private List<Book>                     list;
    private List<List<Book>>               listList;
    private List<Map<String, Book>>        listMap;
    private List<Book[]>                   listArray;
    private Set<Book>                      set;
    private Set<Set<Book>>                 setSet;
    private Set<Map<String, Book>>         setMap;
    private Set<Book[]>                    setArray;
    private Map<String, Book>              map;
    private Map<String, Book[]>            mapArray;
    private Map<String, List<Book>>        mapList;
    private Map<String, Map<String, Book>> mapMap;
    private List<BookEnum>                 listEnum;

    public void setList(List<Book> b) {
        list = b;
    }

    public List<Book> getList() {
        return list;
    }

    public List<Book[]> getListArray() {
        return listArray;
    }

    public void setListArray(List<Book[]> listArray) {
        this.listArray = listArray;
    }

    public List<List<Book>> getListList() {
        return listList;
    }

    public void setListList(List<List<Book>> listList) {
        this.listList = listList;
    }

    public List<Map<String, Book>> getListMap() {
        return listMap;
    }

    public void setListMap(List<Map<String, Book>> listMap) {
        this.listMap = listMap;
    }

    public Map<String, Book> getMap() {
        return map;
    }

    public void setMap(Map<String, Book> map) {
        this.map = map;
    }

    public Map<String, Book[]> getMapArray() {
        return mapArray;
    }

    public void setMapArray(Map<String, Book[]> mapArray) {
        this.mapArray = mapArray;
    }

    public Map<String, List<Book>> getMapList() {
        return mapList;
    }

    public void setMapList(Map<String, List<Book>> mapList) {
        this.mapList = mapList;
    }

    public Map<String, Map<String, Book>> getMapMap() {
        return mapMap;
    }

    public void setMapMap(
            Map<String, Map<String, Book>> mapMap) {
        this.mapMap = mapMap;
    }

    public Set<Book> getSet() {
        return set;
    }

    public void setSet(Set<Book> set) {
        this.set = set;
    }

    public Set<Book[]> getSetArray() {
        return setArray;
    }

    public void setSetArray(Set<Book[]> setArray) {
        this.setArray = setArray;
    }

    public Set<Map<String, Book>> getSetMap() {
        return setMap;
    }

    public void setSetMap(Set<Map<String, Book>> setMap) {
        this.setMap = setMap;
    }

    public Set<Set<Book>> getSetSet() {
        return setSet;
    }

    public void setSetSet(Set<Set<Book>> setSet) {
        this.setSet = setSet;
    }

    public List<BookEnum> getListEnum() {
        return listEnum;
    }

    public void setListEnum(List<BookEnum> listEnum) {
        this.listEnum = listEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookCollections that = (BookCollections)o;

        if (list != null ? !list.equals(that.list) : that.list != null) return false;
        if (listList != null ? !listList.equals(that.listList) : that.listList != null) return false;
        if (listMap != null ? !listMap.equals(that.listMap) : that.listMap != null) return false;
        if (!equals(listArray, that.listArray)) return false;
        if (set != null ? !set.equals(that.set) : that.set != null) return false;
        if (setSet != null ? !setSet.equals(that.setSet) : that.setSet != null) return false;
        if (setMap != null ? !setMap.equals(that.setMap) : that.setMap != null) return false;
        if (!equals(setArray, that.setArray)) return false;
        if (map != null ? !map.equals(that.map) : that.map != null) return false;
        if (!equals(mapArray, that.mapArray)) return false;
        if (mapList != null ? !mapList.equals(that.mapList) : that.mapList != null) return false;
        if (mapMap != null ? !mapMap.equals(that.mapMap) : that.mapMap != null) return false;
        return listEnum != null ? listEnum.equals(that.listEnum) : that.listEnum == null;
    }

    private boolean equals(List<Book[]> c1, List<Book[]> c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 != null && c2 != null) {
            Iterator<Book[]> iterator1 = c1.iterator();
            Iterator<Book[]> iterator2 = c2.iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
                Book[] o1 = iterator1.next();
                Book[] o2 = iterator2.next();
                if (!Arrays.equals(o1, o2))
                    return false;
            }
            return !(iterator1.hasNext() || iterator2.hasNext());
        }
        return false;
    }

    private boolean equals(Collection<Book[]> c1, Collection<Book[]> c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 != null && c2 != null) {
            if (c1.size() != c2.size()) return false;
            for (Book[] o1 : c1) {
                boolean contains = false;
                for (Book[] o2 : c2) {
                    if (Arrays.equals(o1, o2)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean equals(Map<String, Book[]> c1, Map<String, Book[]> c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 != null && c2 != null) {
            return c1.size() == c2.size()
                   && c1.keySet().equals(c2.keySet())
                   && equals(c1.values(), c2.values());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = list != null ? list.hashCode() : 0;
        result = 31 * result + (listList != null ? listList.hashCode() : 0);
        result = 31 * result + (listMap != null ? listMap.hashCode() : 0);
        result = 31 * result + (listArray != null ? listArray.hashCode() : 0);
        result = 31 * result + (set != null ? set.hashCode() : 0);
        result = 31 * result + (setSet != null ? setSet.hashCode() : 0);
        result = 31 * result + (setMap != null ? setMap.hashCode() : 0);
        result = 31 * result + (setArray != null ? setArray.hashCode() : 0);
        result = 31 * result + (map != null ? map.hashCode() : 0);
        result = 31 * result + (mapArray != null ? mapArray.hashCode() : 0);
        result = 31 * result + (mapList != null ? mapList.hashCode() : 0);
        result = 31 * result + (mapMap != null ? mapMap.hashCode() : 0);
        result = 31 * result + (listEnum != null ? listEnum.hashCode() : 0);
        return result;
    }
}
