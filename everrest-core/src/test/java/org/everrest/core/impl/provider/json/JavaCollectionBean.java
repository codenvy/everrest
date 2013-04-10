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
package org.everrest.core.impl.provider.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
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

}
