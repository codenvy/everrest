/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.provider.json;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class Book
{

   private String author;

   private String title;

   private double price;

   private long isdn;

   private int pages;

   private boolean availability;

   private boolean delivery;

   public Book()
   {
   }

   public Book(String author, String title, double price, long isdn, int pages, boolean availability, boolean delivery)
   {
      this.author = author;
      this.title = title;
      this.price = price;
      this.isdn = isdn;
      this.pages = pages;
      this.availability = availability;
      this.delivery = delivery;
   }

   public void setAuthor(String s)
   {
      author = s;
   }

   public void setTitle(String s)
   {
      title = s;
   }

   public void setPrice(double d)
   {
      price = d;
   }

   public void setIsdn(long i)
   {
      isdn = i;
   }

   public void setPages(int i)
   {
      pages = i;
   }

   public void setAvailability(boolean availability)
   {
      this.availability = availability;
   }

   public void setDelivery(boolean delivery)
   {
      this.delivery = delivery;
   }

   public String getAuthor()
   {
      return author;
   }

   public String getTitle()
   {
      return title;
   }

   public double getPrice()
   {
      return price;
   }

   public long getIsdn()
   {
      return isdn;
   }

   public int getPages()
   {
      return pages;
   }

   public boolean isAvailability()
   {
      return availability;
   }

   public boolean getDelivery()
   {
      return delivery;
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Book:{").append("Author: ").append(author).append(" ").append("Title: ").append(title).append(" ")
         .append("Pages: ").append(pages).append(" ").append("Price: ").append(price).append(" ").append("ISDN: ")
         .append(isdn).append("Availability: ").append(availability).append(" ").append("Delivery: ").append(delivery)
         .append(" ").append("} ");
      return sb.toString();
   }

   public boolean equals(Book book)
   {
      return book.getAuthor().equals(author) && book.getTitle().equals(title) && book.getIsdn() == isdn
         && book.getPages() == pages && book.getPrice() == price && book.isAvailability() == availability
         && book.getDelivery() == delivery;
   }
}
