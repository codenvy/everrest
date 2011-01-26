package a.b

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("a")
class GroovyResource3
{
   GroovyResource3(){}
   
   @GET
   @Path("b")
   def m0(){System.getProperty("java.home")}
}