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

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

/**
 * @author andrew00x
 */
public class MediaTypeHelperTest {

    @Test
    public void testMatchWildcard() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "*");
        Assert.assertFalse(MediaTypeHelper.isMatched(one, two));
        Assert.assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testMatchWildcardType() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "bla-bla");
        Assert.assertFalse(MediaTypeHelper.isMatched(one, two));
        Assert.assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testMatchWildcardSubtype() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("application", "*");
        Assert.assertFalse(MediaTypeHelper.isMatched(one, two));
        Assert.assertTrue(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testMatchPrefixWildcardSubtype() {
        MediaType one = new MediaType("application", "*+xml");
        MediaType two = new MediaType("application", "xml");
        Assert.assertTrue(MediaTypeHelper.isMatched(one, two));
        Assert.assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testMatchSuffixWildcardType() {
        MediaType one = new MediaType("application", "atom+*");
        MediaType two = new MediaType("application", "atom+xml");
        Assert.assertTrue(MediaTypeHelper.isMatched(one, two));
        Assert.assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testMatchBothExtSubtypes() {
        MediaType one = new MediaType("application", "atom+xml");
        MediaType two = new MediaType("application", "xhtml+xml");
        Assert.assertFalse(MediaTypeHelper.isMatched(one, two));
        Assert.assertFalse(MediaTypeHelper.isMatched(two, one));
    }

    @Test
    public void testCompatibleWildcard() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "*");
        Assert.assertTrue(MediaTypeHelper.isCompatible(one, two));
        Assert.assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    @Test
    public void testCompatibleWildcardType() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("*", "bla-bla");
        Assert.assertTrue(MediaTypeHelper.isCompatible(one, two));
        Assert.assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    @Test
    public void testCompatibleWildcardSubtype() {
        MediaType one = new MediaType("application", "xml");
        MediaType two = new MediaType("application", "*");
        Assert.assertTrue(MediaTypeHelper.isCompatible(one, two));
        Assert.assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    @Test
    public void testCompatiblePrefixWildcardSubtype() {
        MediaType one = new MediaType("application", "*+xml");
        MediaType two = new MediaType("application", "xml");
        Assert.assertTrue(MediaTypeHelper.isCompatible(one, two));
        Assert.assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    @Test
    public void testCompatibleSuffixWildcardType() {
        MediaType one = new MediaType("application", "atom+*");
        MediaType two = new MediaType("application", "atom+xml");
        Assert.assertTrue(MediaTypeHelper.isCompatible(one, two));
        Assert.assertTrue(MediaTypeHelper.isCompatible(two, one));
    }

    @Test
    public void testCompatibleBothExtSubtypes() {
        MediaType one = new MediaType("application", "atom+xml");
        MediaType two = new MediaType("application", "xhtml+xml");
        Assert.assertFalse(MediaTypeHelper.isCompatible(one, two));
        Assert.assertFalse(MediaTypeHelper.isCompatible(two, one));
    }
}
