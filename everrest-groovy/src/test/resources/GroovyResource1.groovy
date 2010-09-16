import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("a")
class GroovyResource1
{
   
   GroovyResource1(@Context HttpServletRequest req1)
   {
      this.req1 = req1;
   }
   
   @Context
   private HttpServletRequest req
   
   private HttpServletRequest req1
   
   @GET
   @Path("b")
   def m0()
   {
      return req.getMethod() + "\n" +req.getRequestURI().toString() 
   }
   
}