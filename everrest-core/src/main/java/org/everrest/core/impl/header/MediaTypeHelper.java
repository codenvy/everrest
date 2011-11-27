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
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
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
    * Builder or range acceptable media types for look up appropriate {@link MessageBodyReader},
    * {@link MessageBodyWriter} or {@link ContextResolver}. It provider set of media types in descending ordering.
    */
   public static final class MediaTypeRange implements java.util.Iterator<MediaType>
   {
      private MediaType next;

      public MediaTypeRange(MediaType type)
      {
         next = (type == null) ? MediaTypeHelper.DEFAULT_TYPE : type;
      }

      public boolean hasNext()
      {
         return next != null;
      }

      public MediaType next()
      {
         if (next == null)
            throw new NoSuchElementException();
         MediaType type = next;
         fetchNext();
         return type;
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }

      void fetchNext()
      {
         MediaType mediaType = next;
         next = null;
         if (!mediaType.isWildcardType() && !mediaType.isWildcardSubtype())
         {
            // Media type such as application/xml, application/atom+xml, application/*+xml
            // or application/xml+* .
            String type = mediaType.getType();
            String subType = mediaType.getSubtype();
            Matcher extMatcher = MediaTypeHelper.EXT_SUBTYPE_PATTERN.matcher(subType);
            if (extMatcher.matches())
            {
               // Media type such as application/atom+xml or application/*+xml (sub-type extended!!!)
               String extSubtypePrefix = extMatcher.group(1);
               String extSubtype = extMatcher.group(2);
               if (MediaType.MEDIA_TYPE_WILDCARD.equals(extSubtypePrefix))
               {
                  // Media type such as 'application/*+xml' (first part is wildcard).
                  // Next to be checked will be 'application/*'. NOTE do not use 'application/xml'
                  // since there is no guaranty sure xml reader/writer/resolver supports xml extentions.
                  next = new MediaType(type, MediaType.MEDIA_TYPE_WILDCARD);
               }
               else
               {
                  // Media type such as 'application/atom+xml' next to be checked will be 'application/*+xml'
                  // Reader/writer/resolver which declared support of 'application/*+xml' should
                  // supports 'application/atom+xml' also.
                  next = new MediaType(type, MediaTypeHelper.EXT_PREFIX_SUBTYPE + extSubtype);
               }
            }
            else
            {
               // Media type without extension such as 'application/xml'.
               // Next will be 'application/*+xml' since extensions should support pure xml as well.
               next = new MediaType(type, MediaTypeHelper.EXT_PREFIX_SUBTYPE + subType);
            }
         }
         else if (!mediaType.isWildcardType() && mediaType.isWildcardSubtype())
         {
            // Type such as 'application/*' . Next one to be checked is '*/*'.
            // This type is always last for checking in our range.
            next = MediaTypeHelper.DEFAULT_TYPE;
         }
      }
   }

   /**
    * Compare two mimetypes. The main rule for sorting media types is :
    * <p>
    * <li>n / m</li>
    * <li>n / *</li>
    * <li>* / *</li>
    * <p>
    * Method that explicitly list of media types is sorted before a method that list * / *.
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
    * Create a list of media type for given Consumes annotation. If parameter mime is null then list with single element
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
    * Create a list of media type for given Produces annotation. If parameter mime is null then list with single element
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
         //if (contentType.isCompatible(c))
         if (isMatched(c, contentType))
         {
            return true;
         }
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
    * Looking for accept media type with the best quality. Accept list of media type must be sorted by quality value.
    * 
    * @param accept See {@link AcceptMediaType}, {@link QualityValue}
    * @param produces list of produces media type, See {@link Produces}
    * @return quality value of best found compatible accept media type or 0.0 if media types are not compatible
    */
   public static float processQuality(List<MediaType> accept, List<MediaType> produces)
   {
      // NOTE accept contains list of AcceptMediaType instead
      // MediaType, see ContainerRequest#getAcceptableMediaTypes
      @SuppressWarnings("rawtypes")
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

   /**
    * Check types <code>one</code> and type <code>two</code> are compatible. The operation is commutative.
    * <p>
    * Examples:
    * <ul>
    * <li><i>text/plain</i> and <i>text/*</i> are compatible</li>
    * <li><i>application/atom+xml</i> and <i>application/atom+*</i> are compatible</li>
    * </ul>
    * </p>
    * 
    * @param one media type
    * @param two media type
    * @return <code>true</code> if types compatible and <code>false</code> otherwise
    */
   public static boolean isCompatible(MediaType one, MediaType two)
   {
      if (one == null || two == null)
      {
         throw new IllegalArgumentException("null");
      }

      String oneType = one.getType();
      String twoType = two.getType();
      if (oneType.equals(MediaType.MEDIA_TYPE_WILDCARD) || twoType.equals(MediaType.MEDIA_TYPE_WILDCARD))
      {
         return true;
      }

      if (one.getType().equalsIgnoreCase(two.getType()))
      {
         String oneSubtype = one.getSubtype();
         String twoSubtype = two.getSubtype();
         if (oneSubtype.equals(MediaType.MEDIA_TYPE_WILDCARD) || twoSubtype.equals(MediaType.MEDIA_TYPE_WILDCARD)
            || oneSubtype.equalsIgnoreCase(twoSubtype))
         {
            return true;
         }
         Matcher oneMatcher = EXT_SUBTYPE_PATTERN.matcher(oneSubtype);
         Matcher twoMatcher = EXT_SUBTYPE_PATTERN.matcher(twoSubtype);
         if (!oneMatcher.matches() && twoMatcher.matches())
         {
            // one is type such as application/xml
            // two is type such as application/atom+xml, application/*+xml, application/xml+*
            return oneSubtype.equalsIgnoreCase(twoMatcher.group(1)) || oneSubtype.equalsIgnoreCase(twoMatcher.group(2));
         }
         else if (oneMatcher.matches() && !twoMatcher.matches())
         {
            // one is type such as application/atom+xml, application/*+xml, application/xml+*
            // two is type such as application/xml
            return twoSubtype.equalsIgnoreCase(oneMatcher.group(1)) || twoSubtype.equalsIgnoreCase(oneMatcher.group(2));
         }
         else if (oneMatcher.matches() && twoMatcher.matches())
         {
            // both types are extended types
            String onePrefix = oneMatcher.group(1);
            String oneSuffix = oneMatcher.group(2);
            String twoPrefix = twoMatcher.group(1);
            String twoSuffix = twoMatcher.group(2);

            if (onePrefix.equalsIgnoreCase(twoPrefix)
               && (oneSuffix.equals(MediaType.MEDIA_TYPE_WILDCARD) || twoSuffix.equals(MediaType.MEDIA_TYPE_WILDCARD)))
            {
               // parts before '+' are the same and one of after '+' is wildcard '*'
               // For example two sub-types: atom+* and atom+xml
               return true;
            }
            if (oneSuffix.equalsIgnoreCase(twoSuffix)
               && (onePrefix.equals(MediaType.MEDIA_TYPE_WILDCARD) || twoPrefix.equals(MediaType.MEDIA_TYPE_WILDCARD)))
            {
               // parts after '+' are the same and one of before '+' is wildcard '*'
               // For example two sub-types: *+xml and atom+xml
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Check is type <code>checkMe</code> matched to type <code>pattern</code>. NOTE The operation is NOT commutative,
    * e.g. matching of type <code>checkMe</code> matched to <code>pattern</code> does not guaranty that
    * <code>pattern</code> matched to <code>checkMe</code>.
    * <p>
    * Examples:
    * <ul>
    * <li><i>text/plain</i> is matched to <i>text/*</i> but type <i>text/*</i> is not matched to <i>text/plain</i></li>
    * <li><i>application/atom+xml</i> is matched to <i>application/atom+*</i> but type <i>application/atom+*</i> is not
    * matched to <i>application/atom+xml</i></li>
    * </ul>
    * </p>
    * 
    * @param pattern pattern type
    * @param checkMe type to be checked
    * @return <code>true</code> if type <code>checkMe</code> is matched to <code>pattern</code> and <code>false</code>
    *         otherwise
    */
   public static boolean isMatched(MediaType pattern, MediaType checkMe)
   {
      if (pattern == null || checkMe == null)
      {
         throw new IllegalArgumentException("null");
      }

      if (pattern.getType().equals(MediaType.MEDIA_TYPE_WILDCARD))
      {
         return true;
      }

      if (pattern.getType().equalsIgnoreCase(checkMe.getType()))
      {
         String patternSubtype = pattern.getSubtype();
         String checkMeSubtype = checkMe.getSubtype();
         if (patternSubtype.equals(MediaType.MEDIA_TYPE_WILDCARD) || patternSubtype.equalsIgnoreCase(checkMeSubtype))
         {
            return true;
         }
         Matcher patternMatcher = EXT_SUBTYPE_PATTERN.matcher(patternSubtype);
         Matcher checkMeMatcher = EXT_SUBTYPE_PATTERN.matcher(checkMeSubtype);
         if (patternMatcher.matches())
         {
            String patternPrefix = patternMatcher.group(1);
            String patternSuffix = patternMatcher.group(2);

            if (!checkMeMatcher.matches())
            {
               // pattern is type such as application/atom+xml, application/*+xml, application/xml+*
               // checkMe is type such as application/xml
               return checkMeSubtype.equalsIgnoreCase(patternPrefix) || checkMeSubtype.equalsIgnoreCase(patternSuffix);
            }

            // both types are extended types
            String checkMePrefix = checkMeMatcher.group(1);
            String checkMeSuffix = checkMeMatcher.group(2);

            if (patternPrefix.equalsIgnoreCase(checkMePrefix) && patternSuffix.equals(MediaType.MEDIA_TYPE_WILDCARD))
            {
               // parts before '+' are the same and pattern after '+' is wildcard '*'
               // For example two sub-types: atom+* and atom+xml
               return true;
            }
            if (patternSuffix.equalsIgnoreCase(checkMeSuffix) && patternPrefix.equals(MediaType.MEDIA_TYPE_WILDCARD))
            {
               // parts after '+' are the same and pattern before '+' is wildcard '*'
               // For example two sub-types: *+xml and atom+xml
               return true;
            }
         }
      }

      return false;
   }
}
