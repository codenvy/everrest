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

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Locale;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class LanguageTest extends BaseTest {

    public void testFromString() {
        String header = "en-GB";
        Locale locale = Language.getLocale(header);
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());

        header = "en-US,      en-GB";
        locale = Language.getLocale(header);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
    }

    public void testToString() {
        HeaderDelegate<Locale> delegate = RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class);
        Locale locale = new Locale("");
        assertNull(delegate.toString(locale));
        locale = new Locale("*");
        assertNull(delegate.toString(locale));
        locale = new Locale("en", "GB");
        assertEquals("en-gb", delegate.toString(locale));
    }

}
