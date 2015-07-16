To test your rest service you need 
1. Add TestNG listener annotation EverrestJetty.class to your test class.
2. Define you rest service as parameter.
everrest-assured will start Jetty and deploy your service with help of everrest.
So you can test it with rest-assured.
```java
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class BookServiceTest {
    @Mock
    private BookStorage bookStorage;

    @InjectMocks
    private BookService bookService;

    @Test
    public void testName(ITestContext context) throws Exception {
        Collection<Book> bookCollection = new ArrayList<Book>();
        Book book = new Book();
        book.setId("123-1235-555");
        bookCollection.add(book);
        when(bookStorage.getAll()).thenReturn(bookCollection);

        //unsecure call to rest service
        expect()
                .body("id", Matchers.hasItem("123-1235-555"))
                .when().get("/books");

        verify(bookStorage).getAll();
    }
}
```
