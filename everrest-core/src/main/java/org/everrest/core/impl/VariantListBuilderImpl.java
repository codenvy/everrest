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
package org.everrest.core.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

/**
 * See {@link VariantListBuilder}.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: VariantListBuilderImpl.java -1 $
 */
public class VariantListBuilderImpl extends VariantListBuilder
{

   /**
    * Languages.
    */
   private List<Locale> languages = new ArrayList<Locale>();

   /**
    * Encodings.
    */
   private List<String> encodings = new ArrayList<String>();

   /**
    * Media Types.
    */
   private List<MediaType> mediatypes = new ArrayList<MediaType>();

   /**
    * List of {@link Variant}.
    */
   private List<Variant> variants;

   /**
    * {@inheritDoc}
    */
   @Override
   public VariantListBuilder add()
   {
      if (variants == null)
         variants = new ArrayList<Variant>();

      Iterator<MediaType> mediatypesIterator = mediatypes.iterator();

      // do iteration at least one time, even all list are empty
      do
      {
         MediaType mediaType = mediatypesIterator.hasNext() ? mediatypesIterator.next() : null;
         Iterator<Locale> languagesIterator = languages.iterator();

         do
         {
            Locale language = languagesIterator.hasNext() ? languagesIterator.next() : null;
            Iterator<String> encodingsIterator = encodings.iterator();

            do
            {
               String encoding = encodingsIterator.hasNext() ? encodingsIterator.next() : null;
               variants.add(new Variant(mediaType, language, encoding));
            }
            while (encodingsIterator.hasNext());

         }
         while (languagesIterator.hasNext());

      }
      while (mediatypesIterator.hasNext());

      clearAll();
      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<Variant> build()
   {
      return variants == null ? variants = new ArrayList<Variant>() : variants;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VariantListBuilder encodings(String... encs)
   {
      for (String enc : encs)
         encodings.add(enc);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VariantListBuilder languages(Locale... langs)
   {
      for (Locale lang : langs)
         languages.add(lang);

      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VariantListBuilder mediaTypes(MediaType... mediaTypes)
   {
      for (MediaType mediaType : mediaTypes)
         mediatypes.add(mediaType);

      return this;
   }

   /**
    * Reset builder to default state.
    */
   private void clearAll()
   {
      mediatypes.clear();
      languages.clear();
      encodings.clear();
   }

}
