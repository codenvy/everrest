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

import java.util.List;

/**
 * @author andrew00x
 */
public class AcceptMediaTypeTest {

    @Test
    public void testValueOf() {
        String mt = "text/xml;charset=utf8";
        AcceptMediaType acceptedMediaType = AcceptMediaType.valueOf(mt);
        Assert.assertEquals("text", acceptedMediaType.getType());
        Assert.assertEquals("xml", acceptedMediaType.getSubtype());
        Assert.assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
        Assert.assertEquals(1.0F, acceptedMediaType.getQvalue(), 0.0F);
    }

    @Test
    public void testValueOfWithQValue() {
        String mt = "text/xml;charset=utf8;q=0.825";
        AcceptMediaType acceptedMediaType = AcceptMediaType.valueOf(mt);
        Assert.assertEquals("text", acceptedMediaType.getType());
        Assert.assertEquals("xml", acceptedMediaType.getSubtype());
        Assert.assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
        Assert.assertEquals(0.825F, acceptedMediaType.getQvalue(), 0.0F);
    }

    @Test
    public void testFromString() {
        String mt = "text/xml;charset=utf8";
        AcceptMediaTypeHeaderDelegate hd = new AcceptMediaTypeHeaderDelegate();
        AcceptMediaType acceptedMediaType = hd.fromString(mt);
        Assert.assertEquals("text", acceptedMediaType.getType());
        Assert.assertEquals("xml", acceptedMediaType.getSubtype());
        Assert.assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
        Assert.assertEquals(1.0F, acceptedMediaType.getQvalue(), 0.0F);
    }

    @Test
    public void testFromStringWithQValue() {
        String mt = "text/xml;charset=utf8;q=0.825";
        AcceptMediaTypeHeaderDelegate hd = new AcceptMediaTypeHeaderDelegate();
        AcceptMediaType acceptedMediaType = hd.fromString(mt);
        Assert.assertEquals("text", acceptedMediaType.getType());
        Assert.assertEquals("xml", acceptedMediaType.getSubtype());
        Assert.assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
        Assert.assertEquals(0.825F, acceptedMediaType.getQvalue(), 0.0F);
    }

    @Test
    public void testListProducerNull() {
        List<AcceptMediaType> l = HeaderHelper.createAcceptedMediaTypeList(null);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getType(), "*");
        Assert.assertEquals(l.get(0).getSubtype(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListProducerEmptyString() {
        List<AcceptMediaType> l = HeaderHelper.createAcceptedMediaTypeList("");
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getType(), "*");
        Assert.assertEquals(l.get(0).getSubtype(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListProducer() {
        String mt = "text/xml;  charset=utf8;q=0.825,    text/html;charset=utf8,  text/plain;charset=utf8;q=0.8";
        List<AcceptMediaType> l = HeaderHelper.createAcceptedMediaTypeList(mt);
        Assert.assertEquals(3, l.size());

        Assert.assertEquals(l.get(0).getType(), "text");
        Assert.assertEquals(l.get(0).getSubtype(), "html");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);

        Assert.assertEquals(l.get(1).getType(), "text");
        Assert.assertEquals(l.get(1).getSubtype(), "xml");
        Assert.assertEquals(l.get(1).getQvalue(), 0.825F, 0.0F);

        Assert.assertEquals(l.get(2).getType(), "text");
        Assert.assertEquals(l.get(2).getSubtype(), "plain");
        Assert.assertEquals(l.get(2).getQvalue(), 0.8F, 0.0F);
    }
}
