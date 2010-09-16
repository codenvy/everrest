import javax.ws.rs.Path
import javax.ws.rs.GET
import dependencies.Dep1

@Path("a")
class GMain1
{
   @GET
   def m0()
   {
      return new Dep1().getName()
   }
   
}