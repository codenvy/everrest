package a.b

@javax.ws.rs.Path("a")
class GroovyResource2
{
   private org.everrest.groovy.GroovyIoCInjectTest.Component1 component
   GroovyResource2(org.everrest.groovy.GroovyIoCInjectTest.Component1 component)
   {
      this.component = component
   }
   
   @javax.ws.rs.GET
   @javax.ws.rs.Path("b")
   def m0(){component.getName()}
}