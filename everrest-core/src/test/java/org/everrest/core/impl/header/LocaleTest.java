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

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Locale;

/**
 * @author andrew00x
 */
public class LocaleTest {
    @Test
    public void testFromString() {
        String header = "en-GB";
        Locale locale = Language.getLocale(header);
        Assert.assertEquals("en", locale.getLanguage());
        Assert.assertEquals("GB", locale.getCountry());

        header = "en-US,      en-GB";
        locale = Language.getLocale(header);
        Assert.assertEquals("en", locale.getLanguage());
        Assert.assertEquals("US", locale.getCountry());
    }

    @Test
    public void testToString() {
        HeaderDelegate<Locale> delegate = RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class);
        Locale locale = new Locale("");
        Assert.assertNull(delegate.toString(locale));
        locale = new Locale("*");
        Assert.assertNull(delegate.toString(locale));
        locale = new Locale("en", "GB");
        Assert.assertEquals("en-gb", delegate.toString(locale));
    }
}
