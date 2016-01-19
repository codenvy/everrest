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
package org.everrest.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Case insensitive read-only MultivaluedMap.
 *
 * @author andrew00x
 */
public class CaselessUnmodifiableMultivaluedMap<T> extends CaselessMultivaluedMap<T> {
    private static final long serialVersionUID = -7195370974690531404L;

    /** Read only implementation of java.util.Map.Entry. */
    static class ReadOnlyEntryAdapter<T> extends EntryAdapter<T> {
        public ReadOnlyEntryAdapter(Entry<CaselessStringWrapper, List<T>> entry) {
            super(entry);
        }

        /** Value may not be updated via this method. UnsupportedOperationException will be throwing. */
        @Override
        public List<T> setValue(List<T> value) {
            throw new UnsupportedOperationException("setValue");
        }
    }

    /** Read only set of map's entries. */
    class ReadOnlyEntrySet extends EntrySet {
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }

        @Override
        public Iterator<java.util.Map.Entry<String, List<T>>> iterator() {
            return new Iterator<Entry<String, List<T>>>() {
                private Iterator<Entry<CaselessStringWrapper, List<T>>> i = m.entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public Entry<String, List<T>> next() {
                    return new ReadOnlyEntryAdapter<>(i.next());
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll");
        }
    }

    /** Read only set of map's keys. */
    class ReadOnlyKeySet extends KeySet {
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }

        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private Iterator<CaselessStringWrapper> i = m.keySet().iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public String next() {
                    return i.next().getString();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("removeAll");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll");
        }
    }

    // ------------------------------------------------

    public CaselessUnmodifiableMultivaluedMap() {
        super();
    }

    public CaselessUnmodifiableMultivaluedMap(int capacity) {
        super(capacity);
    }

    public CaselessUnmodifiableMultivaluedMap(Map<String, List<T>> m) {
        this(m.size());
        for (Entry<String, List<T>> e : m.entrySet()) {
            this.m.put(new CaselessStringWrapper(e.getKey()), Collections.unmodifiableList(e.getValue()));
        }
    }

    /** Adding new value is not supported. */
    @Override
    public void add(String key, T value) {
        throw new UnsupportedOperationException("add");
    }

    /** Clear map operation is not supported. */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    /** Unmodifiable set of map's entries. */
    @Override
    public Set<Entry<String, List<T>>> entrySet() {
        if (entries == null) {
            entries = new ReadOnlyEntrySet();
        }
        return entries;
    }


    @Override
    public List<T> get(Object key) {
        List<T> list = super.get(key);
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return null;
    }

    /** Unmodifiable list corresponded to specified key. */
    @Override
    public List<T> getList(String key) {
        return Collections.unmodifiableList(super.getList(key));
    }

    /** Unmodifiable set of map's keys. */
    @Override
    public Set<String> keySet() {
        if (keys == null) {
            keys = new ReadOnlyKeySet();
        }
        return keys;
    }

    /** Adding of new mapping is not supported. */
    @Override
    public List<T> put(String key, List<T> value) {
        throw new UnsupportedOperationException("put");
    }

    /** Adding of new mapping is not supported. */
    @Override
    public void putAll(Map<? extends String, ? extends List<T>> m) {
        throw new UnsupportedOperationException("putAll");
    }

    /** Adding of new mapping is not supported. */
    @Override
    public void putSingle(String key, T value) {
        throw new UnsupportedOperationException("putSingle");
    }

    /** Removing of mapping is not supported. */
    @Override
    public List<T> remove(Object key) {
        throw new UnsupportedOperationException("remove");
    }

    /** Adding new values is not supported. */
    @Override
    public void addAll(String key, T... newValues) {
        throw new UnsupportedOperationException("addAll");
    }

    /** Adding new values is not supported. */
    @Override
    public void addAll(String key, List<T> valueList) {
        throw new UnsupportedOperationException("addAll");
    }

    /** Adding new values is not supported. */
    @Override
    public void addFirst(String key, T value) {
        throw new UnsupportedOperationException("addFirst");
    }

    /** Unmodifiable collection of map's values. */
    @Override
    public Collection<List<T>> values() {
        return Collections.unmodifiableCollection(super.values());
    }
}
