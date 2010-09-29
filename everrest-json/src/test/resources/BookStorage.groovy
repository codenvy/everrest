
public class BookStorage
{
   
   List<BookBean> books = new ArrayList<BookBean>()
   
   public BookStorage()
   {
   }
   
   void initStorage()
   {
      
      BookBean b1 = new BookBean(
      author:'Vincent Masson',
      title:'JUnit in Action',
      pages:386,
      price:19.37,
      isdn:93011099534534L,
      availability:true,
      delivery:true)
      
      BookBean b2 = new BookBean(
                        author:'Christian Gross',
                        title:'Beginning C# 2008 from novice to professional',
                        pages:511,
                        price:23.56,
                        isdn:9781590598696L,
                        availability:true,
                        delivery:true)
      
      BookBean b3 = new BookBean(
                        author:'Chuck Easttom',
                        title:'Advanced JavaScript, Third Edition',
                        pages:617,
                        price:25.99,
                        isdn:9781598220339L,
                        availability:false,
                        delivery:false)
      
      books.add(b1)
      books.add(b2)
      books.add(b3)
   }
   
   String toString()
   {
      books.toString()
   }
   
}