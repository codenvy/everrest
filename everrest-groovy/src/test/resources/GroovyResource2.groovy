import org.everrest.groovy.GroovyIoCInjectTest.Component1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("a")
class GroovyResource2
{
   private Component1 component
   
   GroovyResource2(Component1 component)
   {
      this.component = component
   }
   
   @GET
   @Path("b")
   def m0()
   {
      return component.getName()  
   }
   
   
}