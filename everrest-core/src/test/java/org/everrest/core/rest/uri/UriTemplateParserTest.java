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
package org.everrest.core.rest.uri;

import junit.framework.TestCase;

import org.everrest.core.uri.UriPattern;
import org.everrest.core.uri.UriTemplateParser;

import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriTemplateParserTest extends TestCase {

    public void testSimpleRegex() throws Exception {
        try {
            // two variables with the same name
            new UriPattern("/a/{x}/{y}/{x}");
            fail();
        } catch (IllegalArgumentException e) {
        }

        UriTemplateParser p = new UriTemplateParser("/a");
        assertEquals("/a", p.getRegex());
        assertEquals(2, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames());

        p = new UriTemplateParser("a");
        assertEquals("a", p.getRegex());
        assertEquals(1, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames());

        p = new UriTemplateParser("/a/");
        assertEquals("/a/", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames());

        p = new UriTemplateParser("/a/{x}");
        assertEquals("/a/([^/]+?)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{x}/b");
        assertEquals("/a/([^/]+?)/b", p.getRegex());
        assertEquals(5, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/{x}");
        assertEquals("/([^/]+?)", p.getRegex());
        assertEquals(1, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{x}/b/{y}/c");
        assertEquals("/a/([^/]+?)/b/([^/]+?)/c", p.getRegex());
        assertEquals(8, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y");

        p = new UriTemplateParser("/a/{x}/{y}/b/");
        assertEquals("/a/([^/]+?)/([^/]+?)/b/", p.getRegex());
        assertEquals(7, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y");

        p = new UriTemplateParser("/a/{x}/{y}/");
        assertEquals("/a/([^/]+?)/([^/]+?)/", p.getRegex());
        assertEquals(5, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y");

        p = new UriTemplateParser("/{x}/a/{y}/");
        assertEquals("/([^/]+?)/a/([^/]+?)/", p.getRegex());
        assertEquals(5, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y");
    }

    public void testExtRegex() {
        try {
            // two variables with the same name
            new UriTemplateParser("/a/{x:.*}/{y}/{x:.*}");
            fail();
        } catch (IllegalArgumentException e) {
        }
        UriTemplateParser p = new UriTemplateParser("/a/{x:}");
        assertEquals("/a/([^/]+?)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{x : }");
        assertEquals("/a/([^/]+?)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{  x  :  }");
        assertEquals("/a/([^/]+?)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{x:.+}");
        assertEquals("/a/(.+)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/a/{x:(/)?}");
        assertEquals("/a/((/)?)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x");

        p = new UriTemplateParser("/{x}/{y:.+}/{z:.*}");
        assertEquals("/([^/]+?)/(.+)/(.*)", p.getRegex());
        assertEquals(3, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y", "z");

        p = new UriTemplateParser("/a /{x}/{y:.+}/{z:.*}");
        assertEquals("/a%20/([^/]+?)/(.+)/(.*)", p.getRegex());
        assertEquals(6, p.getNumberOfLiteralCharacters());
        testNames(p.getParameterNames(), "x", "y", "z");
    }

    private static void testNames(List<String> templateVariables, String... names) {
        assertEquals(names.length, templateVariables.size());
        int i = 0;
        for (String t : templateVariables)
            assertEquals(t, names[i++]);
    }

}
