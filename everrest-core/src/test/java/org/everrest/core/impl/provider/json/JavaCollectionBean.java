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
package org.everrest.core.impl.provider.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

/** @author andrew00x */
public class JavaCollectionBean {
    // interfaces
    private List<Book> list_items;

    private Set<Book> set_items;

    private Collection<Book> collection_items;

    private Queue<Book> queue_items;

    private ArrayList<Book> arrayList_items;

    private LinkedList<Book> linkedlist_items;

    private LinkedHashSet<Book> linkedHashSet_items;

    private HashSet<Book> hashSet_items;

    private Vector<Book> vector_items;

    private Book[] array_items;

    private List<String> list_string;

    private List<Integer> list_integers;

    private List<Character> list_chars;

    private List<List<Book>> list_list;

    private List<Map<String, Book>> list_map;

    // set methods
    public void setStrings(List<String> i) {
        list_string = i;
    }

    public void setIntegers(List<Integer> i) {
        list_integers = i;
    }

    public void setChars(List<Character> i) {
        list_chars = i;
    }

    //////////////////////
    public void setArrayList(ArrayList<Book> i) {
        arrayList_items = i;
    }

    public void setLinkedList(LinkedList<Book> i) {
        linkedlist_items = i;
    }

    public void setVector(Vector<Book> i) {
        vector_items = i;
    }

    public void setLinkedHashSet(LinkedHashSet<Book> i) {
        linkedHashSet_items = i;
    }

    public void setHashSet(HashSet<Book> i) {
        hashSet_items = i;
    }

    public void setArray(Book[] i) {
        array_items = i;
    }

    public void setList(List<Book> i) {
        list_items = i;
    }

    public void setSet(Set<Book> i) {
        set_items = i;
    }

    public void setCollection(Collection<Book> i) {
        collection_items = i;
    }

    public void setQueue(Queue<Book> i) {
        queue_items = i;
    }

    // get methods
    public List<String> getStrings() {
        return list_string;
    }

    public List<Integer> getIntegers() {
        return list_integers;
    }

    public List<Character> getChars() {
        return list_chars;
    }

    //////////////////////
    public ArrayList<Book> getArrayList() {
        return arrayList_items;
    }

    public Vector<Book> getVector() {
        return vector_items;
    }

    public LinkedList<Book> getLinkedList() {
        return linkedlist_items;
    }

    public Set<Book> getLinkedHashSet() {
        return linkedHashSet_items;
    }

    public Book[] getArray() {
        return array_items;
    }

    public HashSet<Book> getHashSet() {
        return hashSet_items;
    }

    public List<Book> getList() {
        return list_items;
    }

    public Set<Book> getSet() {
        return set_items;
    }

    public Collection<Book> getCollection() {
        return collection_items;
    }

    public Queue<Book> getQueue() {
        return queue_items;
    }

    public List<List<Book>> getListList() {
        return list_list;
    }

    public void setListList(List<List<Book>> list_list) {
        this.list_list = list_list;
    }

    public List<Map<String, Book>> getListMap() {
        return list_map;
    }

    public void setListMap(List<Map<String, Book>> list_map) {
        this.list_map = list_map;
    }
}
