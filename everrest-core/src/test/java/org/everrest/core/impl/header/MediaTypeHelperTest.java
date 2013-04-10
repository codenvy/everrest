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
