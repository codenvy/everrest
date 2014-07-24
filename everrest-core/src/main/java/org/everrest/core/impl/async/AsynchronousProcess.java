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
package org.everrest.core.impl.async;

/**
 * Description of AsynchronousJob. It may be serialized to JSON or plain text format to make possible for client to see
 * what asynchronous jobs in progress.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class AsynchronousProcess {
    private final String owner;
    private final Long   id;
    private final String path;
    private final String status;

    public AsynchronousProcess(String owner, Long id, String path, String status) {
        this.owner = owner;
        this.id = id;
        this.path = path;
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getStatus() {
        return status;
    }
}
