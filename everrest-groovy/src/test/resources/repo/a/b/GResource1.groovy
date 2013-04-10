package a.b

@javax.ws.rs.Path("a")
class GResource1 {
    @javax.ws.rs.GET
    @javax.ws.rs.Path("1")
    def m0() { "GResource1" }

    @javax.ws.rs.GET
    @javax.ws.rs.Path("2")
    def m1() { throw new GRuntimeException() }
}