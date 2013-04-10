package org.everrest

@javax.ws.rs.Path("scan/a")
class A {
    A() {}

    @javax.ws.rs.GET
    def m() { "A" }
}