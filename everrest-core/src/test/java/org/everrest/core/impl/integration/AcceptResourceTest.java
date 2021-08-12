/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.integration;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class AcceptResourceTest extends BaseTest {

  @Path("/a")
  public static class Resource2 {
    @GET
    @Produces({"text/plain", "text/html"})
    public String m0() {
      return "foo";
    }

    @GET
    @Produces({"text/xml", "text/*"})
    public String m1() {
      return "<foo></foo>";
    }

    @GET
    @Produces({"image/*"})
    public String m2() {
      return "gif";
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    public String m3() {
      return "png";
    }

    @GET
    public String m4() {
      return "bar";
    }
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {
      {"text/plain;q=0.9,text/html;q=0.7,text/*;q=0.5", "foo"},
      {"text/plain;q=0.7,text/html;q=0.9,text/*;q=0.5", "foo"},
      {"text/plain;q=0.5,text/html;q=0.7,text/*;q=0.9", "foo"},
      {"text/xml;q=0.9,text/bell;q=0.5", "<foo></foo>"},
      {"text/foo", "<foo></foo>"},
      {"image/gif", "gif"},
      {"image/jpeg;q=0.8,  image/png;q=0.9", "png"},
      {"image/foo;q=0.8,  image/png;q=0.9", "png"},
      {"image/foo;q=0.9,  image/gif;q=0.8", "gif"},
      {"application/x-www-form-urlencoded", "bar"},
      {"application/x-www-form-urlencoded;q=0.5,text/plain", "foo"}
    };
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new Resource2());
          }
        });
  }

  @Test
  @UseDataProvider("data")
  public void testAcceptedMediaType(String acceptMediaType, String expectedResponse)
      throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("accept", acceptMediaType);
    assertEquals(
        expectedResponse, launcher.service("GET", "/a", "", headers, null, null).getEntity());
  }
}
