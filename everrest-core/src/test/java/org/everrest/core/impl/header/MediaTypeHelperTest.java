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
package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MediaTypeHelperTest extends BaseTest {

    public void testMatchWildcard() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "*");
        assertFalse(MediaTypeHelper.isMatched(one, two));
        assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    public void testMatchWildcardType() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "bla-bla");
        assertFalse(MediaTypeHelper.isMatched(one, two));
        assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    public void testMatchWildcardSubtype() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("application", "*");
        assertFalse(MediaTypeHelper.isMatched(one, two));
        assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    public void testMatchPrefixWildcardSubtype() {
        MediaType one = new MediaType("application", "*+xml");
        MediaType two = new MediaType("application", "xml");
        assertTrue(MediaTypeHelper.isMatched(one, two));
        assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    public void testMatchSuffixWildcardType() {
        MediaType one = new MediaType("application", "atom+*");
        MediaType two = new MediaType("application", "atom+xml");
        assertTrue(MediaTypeHelper.isMatched(one, two));
        assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    public void testMatchBothExtSubtypes() {
        MediaType one = new MediaType("application", "atom+xml");
        MediaType two = new MediaType("application", "xhtml+xml");
        assertFalse(MediaTypeHelper.isMatched(one, two));
        assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    public void testCompatibleWildcard() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "*");
        assertTrue(MediaTypeHelper.isCompatible(one, two));
        assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    public void testCompatibleWildcardType() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "bla-bla");
        assertTrue(MediaTypeHelper.isCompatible(one, two));
        assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    public void testCompatibleWildcardSubtype() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("application", "*");
        assertTrue(MediaTypeHelper.isCompatible(one, two));
        assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    public void testCompatiblePrefixWildcardSubtype() {
        MediaType one = new MediaType("application", "*+xml");
        MediaType two = new MediaType("application", "xml");
        assertTrue(MediaTypeHelper.isCompatible(one, two));
        assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    public void testCompatibleSuffixWildcardType() {
        MediaType one = new MediaType("application", "atom+*");
        MediaType two = new MediaType("application", "atom+xml");
        assertTrue(MediaTypeHelper.isCompatible(one, two));
        assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    public void testCompatibleBothExtSubtypes() {
        MediaType one = new MediaType("application", "atom+xml");
        MediaType two = new MediaType("application", "xhtml+xml");
        assertFalse(MediaTypeHelper.isCompatible(one, two));
        assertFalse(MediaTypeHelper.isCompatible(two, one));
    }
}
