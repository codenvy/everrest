package a.b

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@javax.ws.rs.ext.Provider
class GExceptionMapper implements ExceptionMapper<GRuntimeException>
{
   Response toResponse(GRuntimeException e)
   {Response.status(200).entity('GExceptionMapper').type('text/plain').build()}
}