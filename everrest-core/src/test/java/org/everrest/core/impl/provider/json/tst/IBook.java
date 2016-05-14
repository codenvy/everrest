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
package org.everrest.core.impl.provider.json.tst;

public interface IBook {
    void setAuthor(String s);

    void setTitle(String s);

    void setPrice(double d);

    void setIsdn(long i);

    void setPages(int i);

    void setAvailability(boolean availability);

    void setDelivery(boolean delivery);

    String getAuthor();

    String getTitle();

    double getPrice();

    long getIsdn();

    int getPages();

    boolean getAvailability();

    boolean getDelivery();
}