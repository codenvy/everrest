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
package org.everrest.core.impl.header;

import org.everrest.core.header.QualityValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MediaTypeHelper.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public final class MediaTypeHelper
{

   /** Constructor. */
   private MediaTypeHelper()
   {
   }

   /** Default media type. It minds any content type. */
   public static final String DEFAULT = "*/*";

   /** Default media type. It minds any content type. */
   public static final MediaType DEFAULT_TYPE = new MediaType("*", "*");

   /** List which contains default media type. */
   public static final List<MediaType> DEFAULT_TYPE_LIST = Collections.singletonList(DEFAULT_TYPE);

   /** WADL media type. */
   public static final String WADL = "application/vnd.sun.wadl+xml";

   /** WADL media type. */
   public static final MediaType WADL_TYPE = new MediaType("application", "vnd.sun.wadl+xml");

   /** Suffix of sub-type part of media types as application/atom+*. */
   public static final String EXT_SUFFIX_SUBTYPE = "+*";

   /** Prefix of sub-type part of media types as application/*+xml. */
   public static final String EXT_PREFIX_SUBTYPE = "*+";

   /** Media types as application/atom+* or application/*+xml pattern. */
   public static final Pattern EXT_SUBTYPE_PATTERN = Pattern.compile("([^\\+]+)\\+(.+)");

   /** Media types as application/atom+* pattern. */
   public static final Pattern EXT_SUFFIX_SUBTYPE_PATTERN = Pattern.compile("([^\\+]+)\\+\\*");

   /** Media types as application/*+xml pattern. */
   public static final Pattern EXT_PREFIX_SUBTYPE_PATTERN = Pattern.compile("\\*\\+(.+)");

   /**
    * Compare two mimetypes. The main rule for sorting media types is :
    * <p>
    * <li>n / m</li>
    * <li>n / *</li>
    * <li>* / *</li>
    * <p>
    * Method that explicitly list of media types is sorted before a method that
    * list * / *.
    */
   public static final Comparator<MediaType> MEDIA_TYPE_COMPARATOR = new Comparator<MediaType>()
   {
      public int compare(MediaType mediaType1, MediaType mediaType2)
      {
         String type1 = mediaType1.getType();
         String subType1 = mediaType1.getSubtype();
         String type2 = mediaType2.getType();
         String subType2 = mediaType2.getSubtype();

         if (type1.equals(MediaType.MEDIA_TYPE_WILDCARD) && !type2.equals(MediaType.MEDIA_TYPE_WILDCARD))
            return 1;
         if (!type1.equals(MediaType.MEDIA_TYPE_WILDCARD) && type2.equals(MediaType.MEDIA_TYPE_WILDCARD))
            return -1;
         if (subType1.equals(MediaType.MEDIA_TYPE_WILDCARD) && !subType2.equals(MediaType.MEDIA_TYPE_WILDCARD))
            return 1;
         if (!subType1.equals(MediaType.MEDIA_TYPE_WILDCARD) && subType2.equals(MediaType.MEDIA_TYPE_WILDCARD))
            return -1;

         Matcher extmatcher1 = EXT_SUBTYPE_PATTERN.matcher(subType1);
         Matcher extmatcher2 = EXT_SUBTYPE_PATTERN.matcher(subType2);
         if (extmatcher1.matches() && !extmatcher2.matches())
            return 1;
         if (!extmatcher1.matches() && extmatcher2.matches())
            return -1;

         extmatcher1 = EXT_PREFIX_SUBTYPE_PATTERN.matcher(subType1);
         extmatcher2 = EXT_PREFIX_SUBTYPE_PATTERN.matcher(subType2);
         if (extmatcher1.matches() && !extmatcher2.matches())
            return 1;
         if (!extmatcher1.matches() && extmatcher2.matches())
            return -1;

         extmatcher1 = EXT_SUFFIX_SUBTYPE_PATTERN.matcher(subType1);
         extmatcher2 = EXT_SUFFIX_SUBTYPE_PATTERN.matcher(subType2);
         if (extmatcher1.matches() && !extmatcher2.matches())
            return 1;
         if (!extmatcher1.matches() && extmatcher2.matches())
            return -1;

         return 0;
      }

   };

   /**
    * Create a list of media type for given Consumes annotation. If parameter
    * mime is null then list with single element
    * {@link MediaTypeHelper#DEFAULT_TYPE} will be returned.
    *
    * @param mime the Consumes annotation.
    * @return ordered list of media types.
    */
   public static List<MediaType> createConsumesList(Consumes mime)
   {
      if (mime == null)
      {
         return DEFAULT_TYPE_LIST;
      }

      return createMediaTypesList(mime.value());
   }

   /**
    * Create a list of media type for given Produces annotation. If parameter
    * mime is null then list with single element
    * {@link MediaTypeHelper#DEFAULT_TYPE} will be returned.
    *
    * @param mime the Produces annotation.
    * @return ordered list of media types.
    */
   public static List<MediaType> createProducesList(Produces mime)
   {
      if (mime == null)
      {
         return DEFAULT_TYPE_LIST;
      }

      return createMediaTypesList(mime.value());
   }

   /**
    * Useful for checking does method able to consume certain media type.
    *
    * @param consumes list of consumed media types
    * @param contentType should be checked
    * @return true contentType is compatible to one of consumes, false otherwise
    */
   public static boolean isConsume(List<MediaType> consumes, MediaType contentType)
   {
      for (MediaType c : consumes)
      {
         if (contentType.isCompatible(c))
            return true;
      }

      return false;
   }

   /**
    * Create a list of media type from string array.
    *
    * @param mimes source string array
    * @return ordered list of media types
    */
   private static List<MediaType> createMediaTypesList(String[] mimes)
   {
      List<MediaType> l = new ArrayList<MediaType>(mimes.length);
      for (String m : mimes)
         l.add(MediaType.valueOf(m));

      Collections.sort(l, MEDIA_TYPE_COMPARATOR);
      return l;
   }

   /**
    * Looking for accept media type with the best quality. Accept list of media
    * type must be sorted by quality value.
    *
    * @param accept See {@link AcceptMediaType}, {@link QualityValue}
    * @param produces list of produces media type, See {@link Produces}
    * @return quality value of best found compatible accept media type or 0.0 if
    *         media types are not compatible
    */
   @SuppressWarnings("unchecked")
   public static float processQuality(List<MediaType> accept, List<MediaType> produces)
   {
      // NOTE accept contains list of AcceptMediaType instead
      // MediaType, see ContainerRequest#getAcceptableMediaTypes
      Iterator i = accept.iterator();
      while (i.hasNext())
      {
         AcceptMediaType a = (AcceptMediaType)i.next();
         if ("*".equals(a.getType())) // accept everything, not need continue
            return a.getQvalue();
         for (MediaType p : produces)
         {
            if (p.isCompatible(a))
               return a.getQvalue();
         }
      }

      return 0.0F; // 0 quality not acceptable
   }

}
