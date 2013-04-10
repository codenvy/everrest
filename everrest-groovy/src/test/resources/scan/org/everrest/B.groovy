package org.everrest

@javax.ws.rs.Path("scan/b")
class B {
    B() {}

    @javax.ws.rs.GET
    def m() { "B" }
}