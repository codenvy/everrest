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


    @Override
    public long getCreationTime() {
        return 0L;
    }


    @Override
    public String getId() {
        return "MockSessionId";
    }


    @Override
    public long getLastAccessedTime() {
        return 0L;
    }


    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }


    @Override
    public void setMaxInactiveInterval(int i) {
    }


    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }


    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }


    @Override
    public Object getAttribute(String s) {

        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        return attributes.get(s);
    }


    @Override
    public Object getValue(String s) {
        return getAttribute(s);
    }


    @Override
    public Enumeration getAttributeNames() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        return new Vector<String>(attributes.keySet()).elements();
    }


    @Override
    public String[] getValueNames() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call getAttribute() on invalidated session");
        }
        String results[] = new String[0];
        return ((String[])attributes.keySet().toArray(results));
    }


    @Override
    public void setAttribute(String s, Object o) {
        attributes.put(s, o);
    }


    @Override
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }


    @Override
    public void removeAttribute(String s) {
        attributes.remove(s);
    }


    @Override
    public void removeValue(String s) {
        removeAttribute(s);
    }


    @Override
    public void invalidate() {
        if (!isValid) {
            throw new IllegalStateException("Cannot call invalidate() on invalidated session");
        }
        this.isValid = false;
    }


    @Override
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
