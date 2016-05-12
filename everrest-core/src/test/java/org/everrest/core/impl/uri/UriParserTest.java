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
package org.everrest.core.impl.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UriParserTest {
    @Parameters(name = "{index} URI string {0}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"x/y"},
                {"x"},
                {"../../x"},
                {"/x/y"},
                {"/x/../../y/../z"},
                {"x:/y"},
                {"x:/y/z"},
                {"x:/y?q=qq#f"},
                {"x:/y?q#f"},
                {"host:1"},
                {"http://localhost"},
                {"http://localhost:8080"},
                {"http://localhost:8080/"},
                {"http://localhost:8080/x/y"},
                {"http://localhost:8080/x/y?q=qq"},
                {"http://localhost:8080/x/y?q=qq#f"},
                {"http://localhost:8080/x/y#f"},
                {"x:/user@host:111"},
                {"x:/user@host:111/y/z"},
                {"x:/user:pass@host:111/y/z?q1&q2=qq2#f"},
                {"a:/user:pass@h.o.s.t:111/y/z?q1&q2=qq2#f"},
                {"//localhost:8080/x/y"},
                {"//x:8/y/z"},
                {"mailto:user@mail.com"},
                {"http://xx@yy:co@m:111/x/y/z?q#f"}
        });
    }


    @Parameter(0)
    public String uri;

    @Test
    public void parsesUri() throws Exception {
        URI original = new URI(this.uri);
        UriParser parser = new UriParser(this.uri);
        parser.parse();

        assertEquals(original.getScheme(), parser.getScheme());
        assertEquals(original.getHost(), parser.getHost());
        assertEquals(original.getPort(), parser.getPort() == null ? -1 : Integer.parseInt(parser.getPort()));
        assertEquals(original.getUserInfo(), parser.getUserInfo());
        assertEquals(original.getPath(), parser.getPath());
        assertEquals(original.getQuery(), parser.getQuery());
        assertEquals(original.getFragment(), parser.getFragment());
        assertEquals(original.getRawSchemeSpecificPart(), parser.getSchemeSpecificPart());
    }
}