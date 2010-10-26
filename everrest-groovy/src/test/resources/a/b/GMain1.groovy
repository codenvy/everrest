package a.b
import javax.ws.rs.Path
import javax.ws.rs.GET
import dependencies.GDependency1

@Path("a")
class GMain1
{
   @GET
   def m0(){new GDependency1().getName()}
}