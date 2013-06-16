package org.everrest.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a> */
@Path("test")
public class _s {
    @GET
    @Produces("text/html")
    @Path("a")
    public String _a() {
        return "<script type=\"text/javascript\">\n" +
               "<!--\n" +
               "window.location = \"http://localhost:8080/everrest/test/b\"\n" +
               "//-->\n" +
               "</script>";
    }

    @GET
    @Produces("text/html")
    @Path("b")
    public String _b(@Context HttpHeaders headers) {
        for (Map.Entry<String, List<String>> e : headers.getRequestHeaders().entrySet()) {
            System.out.printf("%s: %s%n", e.getKey(), e.getValue());
        }

        return "<body><h1>on b</h1></body>";
    }

}
