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
package org.everrest.core.impl.method;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PrimitiveTypeProducerTest extends TestCase {

    public void testByte() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Byte.TYPE);
        assertEquals((byte)127, primitiveTypeProducer.createValue("127"));
    }

    public void testShort() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Short.TYPE);
        assertEquals((short)32767, primitiveTypeProducer.createValue("32767"));
    }

    public void testInt() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Integer.TYPE);
        assertEquals(2147483647, primitiveTypeProducer.createValue("2147483647"));
    }

    public void testLong() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Long.TYPE);
        assertEquals(9223372036854775807L, primitiveTypeProducer.createValue("9223372036854775807"));
    }

    public void testFloat() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Float.TYPE);
        assertEquals(1.23456789F, primitiveTypeProducer.createValue("1.23456789"));
    }

    public void testDouble() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Double.TYPE);
        assertEquals(1.234567898765432D, primitiveTypeProducer.createValue("1.234567898765432"));
    }

    public void testBoolean() throws Exception {
        PrimitiveTypeProducer primitiveTypeProducer = new PrimitiveTypeProducer(Boolean.TYPE);
        assertEquals(true, primitiveTypeProducer.createValue("true"));
    }

}
