EverRest Example
=================

This is sample of using EverRest to launch JAX-RS services.

We will create simple books service. It should be able give access to books by id, get list all available books and add new book in storage. Service supports JSON format for transfer data to/from client.

Add required _context-param_
---------------------------

```xml
<context-param>
  <param-name>javax.ws.rs.Application</param-name>
  <param-value>org.everrest.sample.book.BookApplication</param-value>   
</context-param>
```

- `javax.ws.rs.Application`: This is FQN of Java class that extends _javax.ws.rs.core.Application_ and provides set of classes and(or) instances of JAX-RS components.


Add bootstrap listeners
-----------------------

Need add two listeners. First one initializes BookStorage and adds it to servlet context. The second one initializes common components of EverRest frameworks.

```xml
<listener>
   <listener-class>org.everrest.sample.book.BookServiceBootstrap</listener-class>
</listener>
<listener>
   <listener-class>org.everrest.core.servlet.EverrestInitializedListener</listener-class>
</listener>
```

Add EverrestServlet
-------------------

```xml
<servlet>
   <servlet-name>EverrestServlet</servlet-name>
   <servlet-class>org.everrest.core.servlet.EverrestServlet</servlet-class>
</servlet>
<servlet-mapping>
   <servlet-name>EverrestServlet</servlet-name>
   <url-pattern>/*</url-pattern>
</servlet-mapping>
```

EverRest components
------------------

- `org.everrest.sample.book.BookApplication` - application deployer.

    ```java
    public class BookApplication extends Application {
       @Override
       public Set<Class<?>> getClasses() {
          Set<Class<?>> cls = new HashSet<Class<?>>(1);
          cls.add(BookService.class);
          return cls;
       }

       @Override
       public Set<Object> getSingletons() {
          Set<Object> objs = new HashSet<Object>(1);
          objs.add(new BookNotFoundExceptionMapper());
          return objs;
       }
    }
    ```

- `org.everrest.sample.book.Book` - simple Java Bean that will be used to transfer data via JSON.

- `org.everrest.sample.book.BookNotFoundException` - exception that will be thrown by `org.everrest.sample.book.BookService` if client requested book that does not exist in storage.

- `org.everrest.sample.book.BookNotFoundExceptionMapper` - JAX-RS component that intercepts `org.everrest.sample.book.BookNotFoundException` and send correct response to client.

    ```java
    @Provider
    public class BookNotFoundExceptionMapper implements ExceptionMapper<BookNotFoundException> {
       Response toResponse(BookNotFoundException exception) {
          return Response.status(404).entity(exception.getMessage()).type("text/plain").build();
       }
    }
    ```

- `org.everrest.sample.book.BookService` - JAX-RS service that process client's requests. Instance of BookStorage will be injected automatically thanks to `org.everrest.core.Inject` annotation

    ```java
    @Path("books")
    public class BookService {
       @Inject
       private BookStorage bookStorage;

       @Path("{id}")
       @GET
       @Produces("application/json")
       public Book get(@PathParam("id") String id) throws BookNotFoundException {
          Book book = bookStorage.getBook(id);
          if (book == null)
             throw new BookNotFoundException(id);
          return book;
       }

       @GET
       @Produces("application/json")
       public Collection<Book> getAll() {
          return bookStorage.getAll();
       }

       @PUT
       @Consumes("application/json")
       public Response put(Book book, @Context UriInfo uriInfo) {
          String id = bookStorage.putBook(book);
          URI location = uriInfo.getBaseUriBuilder().path(getClass()).path(id).build();
          return Response.created(location).entity(location.toString()).type("text/plain").build();
       }
    }
    ```

- `org.everrest.sample.book.BookStorage` - storage of Books.

    ```java
    public class BookStorage {

       private static int idCounter = 100;

       public synchronized String generateId() {
          idCounter++;
          return Integer.toString(idCounter);
       }

       private Map<String, Book> books = new ConcurrentHashMap<String, Book>();

       public BookStorage() {
          init();
       }

       private void init() {
          Book book = new Book();
          book.setTitle("JUnit in Action");
          book.setAuthor("Vincent Masson");
          book.setPages(386);
          book.setPrice(19.37);
          putBook(book);
       }

       public Book getBook(String id) {
          return books.get(id);
       }

       public String putBook(Book book) {
          String id = book.getId();
          if (id == null || id.trim().length() == 0)
          {
             id = generateId();
             book.setId(id);
          }
          books.put(id, book);
          return id;
       }

       public Collection<Book> getAll() {
          return books.values();
       }

       public int numberOfBooks() {
          return books.size();
       }
    }
    ```

Request mapping
---------------

- **GET** `book-service/books/{id}`: get books with specified id. Just after server start only one book in storage and it can be accessed via id `101`
- **GET** `book-service/books/`: get all books from storage.
- **PUT** `book-service/books/`: add new book in storage. The body of request must contains book's description in JSON format. The `Content-type` header must be set to `application/json`

How to try
-----------

Build project.

```
mvn clean install
```

Run it with Jetty server.

```
mvn jetty:run
```

Point you web browser to <http://localhost:8080/book-service/books/101>

If you are under linux or other unix like OS the you can use `curl` utility (often it is already installed). Binary build of this utility available for windows also at <http://curl.haxx.se/download.html>. With `curl` you able to add new book in storage via command:

```
curl -X PUT \
     -H "Content-type:application/json" \
     -d '{"author":"My Author","title":"My Title","price":1.00,"pages":100}' \
     http://localhost:8080/book-service/books/ 
         
```