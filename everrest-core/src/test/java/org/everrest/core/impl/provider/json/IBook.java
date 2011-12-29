package org.everrest.core.impl.provider.json;

public interface IBook
{
   void setAuthor(String s);

   void setTitle(String s);

   void setPrice(double d);

   void setIsdn(long i);

   void setPages(int i);

   void setAvailability(boolean availability);

   void setDelivery(boolean delivery);

   String getAuthor();

   String getTitle();

   double getPrice();

   long getIsdn();

   int getPages();

   boolean isAvailability();

   boolean getDelivery();
}