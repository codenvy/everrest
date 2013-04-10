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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CaselessUnmodifiableMultivaluedMap<T> extends CaselessMultivaluedMap<T> {
    private static final long serialVersionUID = -7195370974690531404L;

    /** Read only implementation of java.util.Map.Entry. */
    class ReadOnlyEntryAdapter extends EntryAdapter {
        public ReadOnlyEntryAdapter(Entry<CaselessStringWrapper, List<T>> entry) {
            super(entry);
        }

        /**
         * Value may not be updated via this method. UnsupportedOperationException
         * will be throwing.
         */
        @Override
        public List<T> setValue(List<T> value) {
            throw new UnsupportedOperationException("setValue");
        }
    }

    /** Read only set of map's entries. */
    class ReadOnlyEntrySet extends EntrySet {
        /** {@inheritDoc} */
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<java.util.Map.Entry<String, List<T>>> iterator() {
            return new Iterator<Entry<String, List<T>>>() {
                private Iterator<Entry<CaselessStringWrapper, List<T>>> i = m.entrySet().iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Entry<String, List<T>> next() {
                    return new ReadOnlyEntryAdapter(i.next());
                }

                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove");
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("remove");
        }

        /** {@inheritDoc} */
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll");
        }
    }

    /** Read only set of map's keys. */
    class ReadOnlyKeySet extends KeySet {
        /** {@inheritDoc} */
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private Iterator<CaselessStringWrapper> i = m.keySet().iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public String next() {
                    return i.next().getString();
                }

                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove");
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("removeAll");
        }

        /** {@inheritDoc} */
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll");
        }
    }

    // ------------------------------------------------

    public CaselessUnmodifiableMultivaluedMap() {
        super();
    }

    public CaselessUnmodifiableMultivaluedMap(int capasity) {
        super(capasity);
    }

    public CaselessUnmodifiableMultivaluedMap(Map<String, List<T>> m) {
        this(m.size());
        for (Iterator<Entry<String, List<T>>> iterator = m.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, List<T>> e = iterator.next();
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

    /** {@inheritDoc} */
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

    /** Unmodifiable collection of map's values. */
    @Override
    public Collection<List<T>> values() {
        return Collections.unmodifiableCollection(super.values());
    }
}
