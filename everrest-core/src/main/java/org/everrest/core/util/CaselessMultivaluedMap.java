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
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Case insensitive MultivaluedMap.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CaselessMultivaluedMap<T> implements ExtMultivaluedMap<String, T>, Serializable {
    private static final long serialVersionUID = -4159372000926269780L;

    /** Case insensitive {@link Entry}. */
    class CaselessEntry implements Entry<CaselessStringWrapper, List<T>> {
        private CaselessStringWrapper key;

        private List<T> value;

        public CaselessEntry(CaselessStringWrapper key, List<T> value) {
            this.key = key;
            this.value = value;
        }

        /** Case insensitive key. {@inheritDoc}. */
        public CaselessStringWrapper getKey() {
            return key;
        }

        /** {@inheritDoc} */
        public List<T> getValue() {
            return value;
        }

        /** {@inheritDoc} */
        public List<T> setValue(List<T> value) {
            List<T> old = this.value;
            this.value = value;
            return old;
        }
    }

    /** Case insensitive {@link Entry} adapter. */
    class EntryAdapter implements Entry<String, List<T>> {
        private Entry<CaselessStringWrapper, List<T>> entry;

        public EntryAdapter(Entry<CaselessStringWrapper, List<T>> entry) {
            this.entry = entry;
        }

        /** Restore original String key of this entry. {@inheritDoc}. */
        public String getKey() {
            return entry.getKey().getString();
        }

        /** {@inheritDoc} */
        public List<T> getValue() {
            return entry.getValue();
        }

        /** {@inheritDoc} */
        public List<T> setValue(List<T> value) {
            return entry.setValue(value);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    class EntrySet extends AbstractSet<Entry<String, List<T>>> {
        /** {@inheritDoc} */
        @Override
        public boolean addAll(Collection<? extends Entry<String, List<T>>> c) {
            throw new UnsupportedOperationException("addAll");
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            m.clear();
        }

        /** {@inheritDoc} */
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;
            Object k = e.getKey();
            Object v = e.getValue();
            return (k == null || k instanceof String)
                   && (v == null || v instanceof List)
                   && m.entrySet().contains(new CaselessEntry(new CaselessStringWrapper((String)k), (List)v));
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<String, List<T>>> iterator() {
            return new Iterator<Entry<String, List<T>>>() {
                private Iterator<Entry<CaselessStringWrapper, List<T>>> i = m.entrySet().iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Entry<String, List<T>> next() {
                    return new EntryAdapter(i.next());
                }

                public void remove() {
                    i.remove();
                }
            };
        }

        /** {@inheritDoc} */
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;
            Object k = e.getKey();
            Object v = e.getValue();
            return (k == null || k instanceof String)
                   && (v == null || v instanceof List)
                   && m.entrySet().remove(new CaselessEntry(new CaselessStringWrapper((String)k), (List)v));
        }

        /** {@inheritDoc} */
        public int size() {
            return m.size();
        }

    }

    class KeySet extends AbstractSet<String> {

        /** {@inheritDoc} */
        @Override
        public boolean addAll(Collection<? extends String> c) {
            throw new UnsupportedOperationException("addAll");
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            m.clear();
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return m.keySet().contains(new CaselessStringWrapper(null));
            }
            return o instanceof String && m.keySet().contains(new CaselessStringWrapper((String)o));
        }

        /** {@inheritDoc} */
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
                    i.remove();
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            if (o == null) {
                return m.keySet().remove(new CaselessStringWrapper(null));
            }
            return o instanceof String && m.keySet().remove(new CaselessStringWrapper((String)o));
        }

        /** {@inheritDoc} */
        public int size() {
            return m.size();
        }

    }

    class ExtMultivaluedMapImpl extends LinkedHashMap<CaselessStringWrapper, List<T>> implements
                                                                                      ExtMultivaluedMap<CaselessStringWrapper, T> {
        private static final long serialVersionUID = -1357174424906146761L;

        ExtMultivaluedMapImpl() {
            super();
        }

        ExtMultivaluedMapImpl(int initialCapacity) {
            super(initialCapacity);
        }

        ExtMultivaluedMapImpl(Map<CaselessStringWrapper, List<T>> m) {
            super(m);
        }

        /** {@inheritDoc} */
        public void add(CaselessStringWrapper key, T value) {
            if (value == null) {
                return;
            }
            List<T> list = getList(key);
            list.add(value);
        }

        /** {@inheritDoc} */
        public T getFirst(CaselessStringWrapper key) {
            List<T> list = get(key);
            return list != null && list.size() > 0 ? list.get(0) : null;
        }

        /** {@inheritDoc} */
        public List<T> getList(CaselessStringWrapper key) {
            List<T> list = get(key);
            if (list == null) {
                list = new ArrayList<T>();
                put(key, list);
            }
            return list;
        }

        /** {@inheritDoc} */
        public void putSingle(CaselessStringWrapper key, T value) {
            if (value == null) {
                remove(key);
                return;
            }
            List<T> list = getList(key);
            list.clear();
            list.add(value);
        }

    }

    // ---------------------------------------------

    ExtMultivaluedMapImpl m;

    Set<String> keys;

    Set<Entry<String, List<T>>> entries;

    public CaselessMultivaluedMap() {
        this.m = new ExtMultivaluedMapImpl();
    }

    public CaselessMultivaluedMap(int capacity) {
        this.m = new ExtMultivaluedMapImpl(capacity);
    }

    public CaselessMultivaluedMap(Map<String, List<T>> m) {
        this.m = new ExtMultivaluedMapImpl(m.size());
        for (Iterator<Entry<String, List<T>>> iterator = m.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, List<T>> e = iterator.next();
            this.m.put(new CaselessStringWrapper(e.getKey()), e.getValue());
        }
    }

    /** {@inheritDoc} */
    public void add(String key, T value) {
        m.add(new CaselessStringWrapper(key), value);
    }

    /** {@inheritDoc} */
    public void clear() {
        m.clear();
    }

    /** {@inheritDoc} */
    public boolean containsKey(Object key) {
        if (key == null) {
            return m.containsKey(new CaselessStringWrapper(null));
        }
        return key instanceof String && m.containsKey(new CaselessStringWrapper((String)key));
    }

    /** {@inheritDoc} */
    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    /** {@inheritDoc} */
    public Set<Entry<String, List<T>>> entrySet() {
        if (entries == null) {
            entries = new EntrySet();
        }
        return entries;
    }

    /** {@inheritDoc} */
    public List<T> get(Object key) {
        if (key == null) {
            return m.get(new CaselessStringWrapper(null));
        }
        if (!(key instanceof String)) {
            return null;
        }
        return m.get(new CaselessStringWrapper((String)key));
    }

    /** {@inheritDoc} */
    public T getFirst(String key) {
        return m.getFirst(new CaselessStringWrapper(key));
    }

    /** {@inheritDoc} */
    public List<T> getList(String key) {
        CaselessStringWrapper caselessKey = new CaselessStringWrapper(key);
        List<T> list = m.get(caselessKey);
        if (list == null) {
            list = new ArrayList<T>();
            m.put(caselessKey, list);
        }
        return list;
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /** {@inheritDoc} */
    public Set<String> keySet() {
        if (keys == null) {
            keys = new KeySet();
        }
        return keys;
    }

    /** {@inheritDoc} */
    public List<T> put(String key, List<T> value) {
        return m.put(new CaselessStringWrapper(key), value);
    }

    /** {@inheritDoc} */
    public void putAll(Map<? extends String, ? extends List<T>> m) {
        for (String key : m.keySet()) {
            List<T> values = m.get(key);
            for (T v : values) {
                add(key, v);
            }
        }
    }

    /** {@inheritDoc} */
    public void putSingle(String key, T value) {
        m.putSingle(new CaselessStringWrapper(key), value);
    }

    /** {@inheritDoc} */
    public List<T> remove(Object key) {
        if (key == null) {
            return m.remove(new CaselessStringWrapper(null));
        }
        if (!(key instanceof String)) {
            return null;
        }
        return m.remove(new CaselessStringWrapper((String)key));
    }

    /** {@inheritDoc} */
    public int size() {
        return m.size();
    }

    /** {@inheritDoc} */
    public Collection<List<T>> values() {
        return m.values();
    }
}
