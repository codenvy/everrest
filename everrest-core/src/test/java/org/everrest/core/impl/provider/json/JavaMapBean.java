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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class JavaMapBean {
    private Map<String, Book> map_items;

    private HashMap<String, Book> hashMap_items;

    private Hashtable<String, Book> hashtable_items;

    private LinkedHashMap<String, Book> linkedHashMap_items;

    // --------------------------
    private Map<String, List<Book>> map_list;

    private Map<String, Book[]> map_array;

    private Map<String, Map<String, Book>> map_map;

    public void setMapList(Map<String, List<Book>> hu) {
        this.map_list = hu;
    }

    public Map<String, List<Book>> getMapList() {
        return map_list;
    }

    // --------------------------

    public void setMapMap(Map<String, Map<String, Book>> hu) {
        this.map_map = hu;
    }

    public Map<String, Map<String, Book>> getMapMap() {
        return map_map;
    }

    // --------------------------

    public void setMapArray(Map<String, Book[]> hu) {
        this.map_array = hu;
    }

    public Map<String, Book[]> getMapArray() {
        return map_array;
    }

    // --------------------------
    private Map<String, String> map_strings;

    private Map<String, Integer> map_integers;

    private Map<String, Boolean> map_booleans;

    // set methods
    public void setStrings(Map<String, String> m) {
        map_strings = m;
    }

    public void setIntegers(Map<String, Integer> m) {
        map_integers = m;
    }

    public void setBooleans(Map<String, Boolean> m) {
        map_booleans = m;
    }

    ///////////////////////
    public void setMap(Map<String, Book> m) {
        map_items = m;
    }

    public void setHashMap(HashMap<String, Book> m) {
        hashMap_items = m;
    }

    public void setHashtable(Hashtable<String, Book> m) {
        hashtable_items = m;
    }

    public void setLinkedHashMap(LinkedHashMap<String, Book> m) {
        linkedHashMap_items = m;
    }

    // get methods
    public Map<String, String> getStrings() {
        return map_strings;
    }

    public Map<String, Integer> getIntegers() {
        return map_integers;
    }

    public Map<String, Boolean> getBooleans() {
        return map_booleans;
    }

    ///////////////////////
    public Map<String, Book> getMap() {
        return map_items;
    }

    public HashMap<String, Book> getHashMap() {
        return hashMap_items;
    }

    public Hashtable<String, Book> getHashtable() {
        return hashtable_items;
    }

    public LinkedHashMap<String, Book> getLinkedHashMap() {
        return linkedHashMap_items;
    }

}