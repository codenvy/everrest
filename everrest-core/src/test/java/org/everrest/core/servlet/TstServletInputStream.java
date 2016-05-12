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
package org.everrest.core.servlet;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class TstServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream data;

    TstServletInputStream(byte[] data) {
        this.data = new ByteArrayInputStream(data);
    }

    @Override
    public int read() throws IOException {
        return data.read();
    }

    @Override
    public boolean isFinished() {
        return data.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
}
