package org.everrest.core.impl.provider.json;

import java.util.List;

public interface IBookStorage
{
   void setBooks(List<IBook> b);

   List<IBook> getBooks();
}
