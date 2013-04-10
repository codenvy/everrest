package a.b

import dependencies.GDependency1

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("a")
class GMain1 {
    @GET
    def m0() { new GDependency1().getName() }
}