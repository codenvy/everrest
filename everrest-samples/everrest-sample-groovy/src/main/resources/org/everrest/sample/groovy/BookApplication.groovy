package org.everrest.sample.groovy

class BookApplication extends javax.ws.rs.core.Application
{
   Set<Class<?>> getClasses()
   {
      new HashSet<Class<?>>([])
   }

   Set<Object> getSingletons()
   {
      new HashSet<Object>([new BookService(bookStorage:new BookStorage()),
         new BookNotFoundExceptionMapper()])
   }
}