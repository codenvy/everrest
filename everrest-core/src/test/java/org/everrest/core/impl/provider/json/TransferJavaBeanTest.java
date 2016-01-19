/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class TransferJavaBeanTest extends JsonTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testTransfer() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String title = "JUnit in Action";
        String author = "Vincent Masson";
        int pages = 386;
        double price = 19.37;
        long isdn = 930110995;

        Book book = new Book();
        book.setAuthor(author);
        book.setTitle(title);
        book.setPages(pages);
        book.setPrice(price);
        book.setIsdn(isdn);

        JsonValue jv = JsonGenerator.createJsonObject(book);
        JsonWriter jsonWriter = new JsonWriter(out);
        jv.writeTo(jsonWriter);
        jsonWriter.flush();
        jsonWriter.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        JsonParser jsonParser = new JsonParser();

        jsonParser.parse(in);
        JsonValue jsonValue = jsonParser.getJsonObject();
        Book newBook = ObjectBuilder.createObject(Book.class, jsonValue);
        assertEquals(author, newBook.getAuthor());
        assertEquals(title, newBook.getTitle());
        assertEquals(pages, newBook.getPages());
        assertEquals(price, newBook.getPrice());
        assertEquals(isdn, newBook.getIsdn());
    }

}
