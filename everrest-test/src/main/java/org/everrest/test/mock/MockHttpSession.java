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
package org.everrest.test.mock;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The Class MockHttpSession.
 *
 * @author Max Shaposhnik
 */
public class MockHttpSession implements HttpSession {

    /** The attributes map. */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /** The servlet context. */
    private ServletContext servletContext;

    /** The is valid. */
    private boolean isValid = true;

    /** {@inheritDoc} */
    public long getCreationTime() {
        return 0L;
    }

    /** {@inheritDoc} */
    public String getId() {
        return "MockSessionId";
    }

    /** {@inheritDoc} */
    public long getLastAccessedTime() {
        return 0L;
    }

    /** {@inheritDoc} */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /** {@inheritDoc} */
    public void setMaxInactiveInterval(int i) {
    }

    /** {@inheritDoc} */
    public int getMaxInactiveInterval() {
        return 0;
    }

    /** {@inheritDoc} */
    public HttpSessionContext getSessionContext() {
        return null;
    }

    /** {@inheritDoc} */
    public Object getAttribute(String s) {

        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        return attributes.get(s);
    }

    /** {@inheritDoc} */
    public Object getValue(String s) {
        return getAttribute(s);
    }

    /** {@inheritDoc} */
    public Enumeration getAttributeNames() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        return new Vector<String>(attributes.keySet()).elements();
    }

    /** {@inheritDoc} */
    public String[] getValueNames() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        String results[] = new String[0];
        return ((String[])attributes.keySet().toArray(results));
    }

    /** {@inheritDoc} */
    public void setAttribute(String s, Object o) {
        attributes.put(s, o);
    }

    /** {@inheritDoc} */
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }

    /** {@inheritDoc} */
    public void removeAttribute(String s) {
        attributes.remove(s);
    }

    /** {@inheritDoc} */
    public void removeValue(String s) {
        removeAttribute(s);
    }

    /** {@inheritDoc} */
    public void invalidate() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call invalidate() on invalidated session");
        }
        this.isValid = false;
    }

    /** {@inheritDoc} */
    public boolean isNew() {
        return false;
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Sets the valid.
     *
     * @param isValid
     *         the new valid
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }
}
