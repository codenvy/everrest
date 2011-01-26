public class BookBean
{
   
   String author
   
   String title
   
   double price
   
   long isdn
   
   int pages
   
   boolean availability
   
   boolean delivery
   
   String toString()
   {
      StringBuffer sb = new StringBuffer()
      sb.append("Book:{").append("Author: ").append(author).append(" ").append("Title: ").append(title).append(" ")
      .append("Pages: ").append(pages).append(" ").append("Price: ").append(price).append(" ").append("ISDN: ")
      .append(isdn).append("Availability: ").append(availability).append(" ").append("Delivery: ").append(delivery)
      .append(" ").append("} ")
      sb.toString()
   }
   
   boolean equals(Object other)
   {
      return other != null && other instanceof BookBean && other.author == author && other.title == title && other.isdn == isdn && other.pages == pages && other.price == price && other.availability == availability && other.delivery == delivery
   }
}