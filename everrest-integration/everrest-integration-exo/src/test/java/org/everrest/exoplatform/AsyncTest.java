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
package org.everrest.exoplatform;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AsyncTest extends StandaloneBaseTest {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Identity user = new Identity("john", new HashSet<MembershipEntry>(), new HashSet<String>(Arrays.asList("admin")));
        ConversationState.setCurrent(new ConversationState(user));
    }

    @Path("AsyncTest.Async1")
    public static class Async1 {
        @GET
        public void m() {
            assertNotNull(ConversationState.getCurrent());
        }
    }

    @Path("AsyncTest.Async2")
    public static class Async2 {
        @GET
        public void m() {
            //throw new WebApplicationException(Response.status(400).entity("test").build());
            throw new RuntimeException("test process exceptions in asynchronous mode");
        }
    }

    public void testCopyConversationState() throws Exception {
        resources.addResource(Async1.class, null);
        ContainerResponse response = launcher.service("GET", "/AsyncTest.Async1?async=true", "", null, null, null);
        assertEquals(202, response.getStatus());
        String jobUrl = (String)response.getEntity();
        response = getAsynchronousResponse(jobUrl, null);
        //System.out.println(response.getEntity());
        assertEquals(204, response.getStatus());
        resources.removeResource(Async1.class);
    }

    public void testProcessExceptions() throws Exception {
        resources.addResource(Async2.class, null);
        ContainerResponse response = launcher.service("GET", "/AsyncTest.Async2?async=true", "", null, null, null);
        assertEquals(202, response.getStatus());
        String jobUrl = (String)response.getEntity();
        ByteArrayContainerResponseWriter w = new ByteArrayContainerResponseWriter();
        response = getAsynchronousResponse(jobUrl, w);
        //System.out.println(response.getEntity());
        assertEquals(500, response.getStatus());
        assertEquals("test process exceptions in asynchronous mode", new String(w.getBody()));
        resources.removeResource(Async1.class);
    }

    private ContainerResponse getAsynchronousResponse(String jobUrl, ByteArrayContainerResponseWriter writer) throws Exception {
        ContainerResponse response;
        // Limit end time to avoid infinite loop if something going wrong.
        final long endTime = System.currentTimeMillis() + 5000;
        synchronized (this) {
            while ((response = launcher.service("GET", jobUrl, "", null, null, writer, null)).getStatus() == 202
                   && System.currentTimeMillis() < endTime) {
                wait(100);
                if (writer != null) {
                    writer.reset();
                }
            }
        }
        return response;
    }
}
