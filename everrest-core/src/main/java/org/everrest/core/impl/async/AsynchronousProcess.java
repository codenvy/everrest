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
 * statuses of asynchronous jobs.
 *
 * @author andrew00x
 */
public final class AsynchronousProcess {
    private String owner;
    private Long   id;
    private String path;
    private String status;

    public AsynchronousProcess() {
    }

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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AsynchronousProcess{" +
               "owner='" + owner + '\'' +
               ", id=" + id +
               ", path='" + path + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}
