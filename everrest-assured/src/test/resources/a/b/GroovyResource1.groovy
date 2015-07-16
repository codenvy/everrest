package a.b
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("a")
class GroovyResource1
 { 
   @Path("b")
   @GET
   String m0(){
   return "GroovyResource1"
   }
}