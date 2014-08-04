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
package org.everrest.core.impl;

import org.everrest.core.ResourcePublicationException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ResourceBinderTest extends BaseTest {

    @Path("/a/b/{c}")
    public static class Resource {
        @GET
        @Produces("text/html")
        public void m1() {
        }

        @GET
        @Path("d")
        @Produces("text/html")
        public void m2() {
        }

        @Path("d")
        public void m3() {
        }
    }


    @Path("/a/b/c/{d}/e")
    public static class URIConflictResource1 {
        @GET
        public void m0() {
        }
    }

    @Path("/a/b/c/{d}/e")
    public static class URIConflictResource2 {
        @GET
        public void m0() {
        }
    }

    @Test
    public void testBind() {
        int prevSize = processor.getResources().getSize();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource.class);
            }
        });
        Assert.assertEquals((prevSize + 1), processor.getResources().getSize());
    }

    @Test
    public void testUnbind() {
        int prevSize = processor.getResources().getSize();
        processor.getResources().addResource(Resource.class, null);
        processor.getResources().removeResource(Resource.class);
        Assert.assertEquals(prevSize, processor.getResources().getSize());
    }

    @Test
    public void testURIConflict() {
        // two per-request resources with URI conflict
        int initSize = processor.getResources().getSize();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(URIConflictResource1.class);
            }
        });
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
        try {
            processor.addApplication(new Application() {
                @Override
                public Set<Class<?>> getClasses() {
                    return Collections.<Class<?>>singleton(URIConflictResource2.class);
                }
            });
        } catch (ResourcePublicationException e) {
        }
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
    }

    @Test
    public void testURIConflict2() {
        // per-request and singleton resources with URI conflict
        int initSize = processor.getResources().getSize();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(URIConflictResource1.class);
            }
        });
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
        try {
            processor.addApplication(new Application() {
                @Override
                public Set<Class<?>> getClasses() {
                    return Collections.emptySet();
                }

                @Override
                public Set<Object> getSingletons() {
                    return Collections.<Object>singleton(new URIConflictResource2());
                }
            });
        } catch (ResourcePublicationException e) {
        }
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
    }

    @Test
    public void testURIConflict3() {
        // per-request and singleton resources with URI conflict
        int initSize = processor.getResources().getSize();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new URIConflictResource2());
            }
        });
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
        try {
            processor.addApplication(new Application() {
                @Override
                public Set<Class<?>> getClasses() {
                    return Collections.<Class<?>>singleton(URIConflictResource1.class);
                }
            });
        } catch (ResourcePublicationException e) {
        }
        Assert.assertEquals(initSize + 1, processor.getResources().getSize());
    }
}
