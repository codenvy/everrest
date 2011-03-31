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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class HeaderHelper
{
   /** Constructor.*/
   private HeaderHelper()
   {
   }

   /** Pattern for search whitespace and quote in string. */
   private static final Pattern WHITESPACE_QOUTE_PATTERN = Pattern.compile("[\\s\"]");

   /** Pattern for whitespace in string. */
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

   /** Header separators. Header token MUST NOT contains any of it. */
   private static final char[] SEPARATORS = new char[128];
   static
   {
      for (char c : "()<>@,;:\"\\/[]?={}".toCharArray())
         SEPARATORS[c] = c;
   }

   /** Accept all media type list. */
   private static final List<AcceptMediaType> ACCEPT_ALL_MEDIA_TYPE =
      Collections.singletonList(AcceptMediaType.DEFAULT);

   /** Accept all languages list. */
   private static final List<AcceptLanguage> ACCEPT_ALL_LANGUAGE = Collections.singletonList(AcceptLanguage.DEFAULT);

   /** Accept all tokens list. */
   private static final List<AcceptToken> ACCEPT_ALL_TOKENS = Collections.singletonList(new AcceptToken("*"));

   //

   /**
    * Comparator for tokens which have quality value.
    *
    * @see QualityValue
    */
   public static final Comparator<QualityValue> QUALITY_VALUE_COMPARATOR = new Comparator<QualityValue>()
   {

      /**
       * Compare two QualityValue for order.
       *
       * @param o1 first QualityValue to be compared
       * @param o2 second QualityValue to be compared
       * @return result of comparison
       * @see Comparator#compare(Object, Object)
       * @see QualityValue
       */
      public int compare(QualityValue o1, QualityValue o2)
      {
         float q1 = o1.getQvalue();
         float q2 = o2.getQvalue();
         if (q1 < q2)
            return 1;
         if (q1 > q2)
            return -1;
         return 0;
      }

   };

   // accept headers

   /**
    * Accept media type producer.
    *
    * @see ListHeaderProducer
    */
   private static final ListHeaderProducer<AcceptMediaType> LIST_MEDIA_TYPE_PRODUCER =
      new ListHeaderProducer<AcceptMediaType>()
      {

         /**
          * {@inheritDoc}
          */
         @Override
         protected AcceptMediaType create(String part)
         {
            return AcceptMediaType.valueOf(part);
         }

      };

   /**
    * Create sorted by quality value accepted media type list.
    *
    * @param header source header string
    * @return List of AcceptMediaType
    */
   public static List<AcceptMediaType> createAcceptedMediaTypeList(String header)
   {
      if (header == null || header.length() == 0 || MediaType.WILDCARD.equals(header.trim()))
         return ACCEPT_ALL_MEDIA_TYPE;
      return LIST_MEDIA_TYPE_PRODUCER.createQualitySortedList(header);
   }

   /**
    * Accept language producer.
    *
    * @see ListHeaderProducer
    */
   private static final ListHeaderProducer<AcceptLanguage> LIST_LANGUAGE_PRODUCER =
      new ListHeaderProducer<AcceptLanguage>()
      {

         /**
          * {@inheritDoc}
          */
         @Override
         protected AcceptLanguage create(String part)
         {
            return AcceptLanguage.valueOf(part);
         }

      };

   /**
    * Create sorted by quality value accepted language list.
    *
    * @param header source header string
    * @return List of AcceptLanguage
    */
   public static List<AcceptLanguage> createAcceptedLanguageList(String header)
   {
      if (header == null || header.length() == 0 || "*".equals(header))
         return ACCEPT_ALL_LANGUAGE;
      return LIST_LANGUAGE_PRODUCER.createQualitySortedList(header);
   }

   /**
    * Accept token producer. Useful for processing 'accept-charset' and
    * 'accept-encoding' request headers.
    *
    * @see ListHeaderProducer
    */
   private static final ListHeaderProducer<AcceptToken> LIST_TOKEN_PRODUCER = new ListHeaderProducer<AcceptToken>()
   {

      @Override
      protected AcceptToken create(String part)
      {

         try
         {
            // check does contains parameter
            int col = part.indexOf(';');
            String token = col > 0 ? part.substring(0, col).trim() : part.trim();

            int i = -1;
            if ((i = isToken(token)) != -1) // check is valid token
               throw new IllegalArgumentException("Not valid character at index " + i + " in " + token);

            if (col < 0)
               return new AcceptToken(token);

            Map<String, String> param = new HeaderParameterParser().parse(part);
            if (param.containsKey(QualityValue.QVALUE))
               return new AcceptToken(token, parseQualityValue(param.get(QualityValue.QVALUE)));

            return new AcceptToken(token);
         }
         catch (ParseException e)
         {
            throw new IllegalArgumentException(e);
         }
      }

   };

   /**
    * Create sorted by quality value 'accept-character' list.
    *
    * @param header source header string
    * @return List of accept charset tokens
    */
   public static List<AcceptToken> createAcceptedCharsetList(String header)
   {
      if (header == null || header.length() == 0 || "*".equals(header))
         return ACCEPT_ALL_TOKENS;
      return LIST_TOKEN_PRODUCER.createQualitySortedList(header);
   }

   /**
    * Create sorted by quality value 'accept-encoding' list.
    *
    * @param header source header string
    * @return List of accept encoding tokens
    */
   public static List<AcceptToken> createAcceptedEncodingList(String header)
   {
      if (header == null || header.length() == 0 || "*".equals(header))
         return ACCEPT_ALL_TOKENS;
      return LIST_TOKEN_PRODUCER.createQualitySortedList(header);
   }

   // cookie

   /**
    * Temporary cookie image.
    */
   private static class TempCookie
   {

      /** Cookie name. */
      String name;

      /** Cookie value. */
      String value;

      /** Cookie version. */
      int version;

      /** Cookie path. */
      String path;

      /** Cookie domain. */
      String domain;

      // For NewCokie.

      /** Comments about cookie. */
      String comment;

      /** Cookie max age. */
      int maxAge;

      /** True if cookie secure false otherwise. */
      boolean security;

      /**
       * @param name cookie name
       * @param value cookie value
       */
      public TempCookie(String name, String value)
      {
         this.name = name;
         this.value = value;
         this.version = Cookie.DEFAULT_VERSION;
         this.domain = null;
         this.path = null;
         this.comment = null;
         this.maxAge = NewCookie.DEFAULT_MAX_AGE;
         this.security = false;
      }

   }

   /**
    * Parse cookie header string and create collection of cookie from it.
    *
    * @param cookie the cookie string.
    * @return collection of Cookie.
    */
   public static List<Cookie> parseCookies(String cookie)
   {
      int n = 0;
      int p = 0;
      TempCookie temp = null;
      int version = 0;
      List<Cookie> l = new ArrayList<Cookie>();

      while (p < cookie.length())
      {
         n = findCookieParameterSeparator(cookie, p);

         // cut pair of key/value
         String pair = cookie.substring(p, n);

         String name = "";
         String value = "";

         // '=' separator
         int eq = pair.indexOf('=');
         if (eq != -1)
         {
            name = pair.substring(0, eq).trim();
            value = pair.substring(eq + 1).trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1)
               value = value.substring(1, value.length() - 1);
         }
         else
         {
            // there is no value
            name = pair.trim();
         }

         // Name of parameter not start from '$', then it is cookie name and value.
         // In header string name/value pair (name without '$') SHOULD be before
         // '$Path' and '$Domain' parameters, but '$Version' goes before name/value
         // pair.
         if (name.indexOf('$') == -1)
         {

            // first save previous cookie
            if (temp != null)
               l.add(new Cookie(temp.name, temp.value, temp.path, temp.domain, temp.version));

            temp = new TempCookie(name, value);
            // version was kept before
            // @see http://www.ietf.org/rfc/rfc2109.txt section 4.4
            temp.version = version;
         }
         else if (name.equalsIgnoreCase("$Version"))
         {
            // keep version number
            version = Integer.valueOf(value);
         }
         else if (name.equalsIgnoreCase("$Path") && temp != null)
         {
            // Temporary cookie must exists, otherwise this parameter will be lost
            temp.path = value;
         }
         else if (name.equalsIgnoreCase("$Domain") && temp != null)
         {
            // Temporary cookie must exists, otherwise this parameter will be lost.
            temp.domain = value;
         }

         p = n + 1;
      }

      if (temp != null)
         l.add(new Cookie(temp.name, temp.value, temp.path, temp.domain, temp.version));

      return l;
   }

   // Date

   // HTTP applications have historically allowed three different formats
   // for the representation of date/time stamps
   // For example :
   // Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
   // Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
   // Sun Nov 6 08:49:37 1994        ; ANSI C's asctime() format

   /**
    * RFC 822, updated by RFC 1123.
    */
   private static final String RFC_1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

   /**
    * RFC 850, obsoleted by RFC 1036.
    */
   private static final String RFC_1036_DATE_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss zzz";

   /**
    * ANSI C's asctime() format.
    */
   private static final String ANSI_C_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";

   /**
    * SimpleDateFormat java docs says:
    * <p>
    * Date formats are not synchronized. It is recommended to create separate
    * format instances for each thread. If multiple threads access a format
    * concurrently, it must be synchronized externally.
    */
   private static ThreadLocal<List<SimpleDateFormat>> dateFormats = new ThreadLocal<List<SimpleDateFormat>>()
   {
      /**
       * {@inheritDoc}
       */
      @Override
      protected List<SimpleDateFormat> initialValue()
      {
         List<SimpleDateFormat> l = new ArrayList<SimpleDateFormat>(3);
         l.add(new SimpleDateFormat(RFC_1123_DATE_FORMAT, Locale.US));
         l.add(new SimpleDateFormat(RFC_1036_DATE_FORMAT, Locale.US));
         l.add(new SimpleDateFormat(ANSI_C_DATE_FORMAT, Locale.US));
         TimeZone tz = TimeZone.getTimeZone("GMT");
         l.get(0).setTimeZone(tz);
         l.get(1).setTimeZone(tz);
         l.get(2).setTimeZone(tz);

         return Collections.unmodifiableList(l);
      }
   };

   /**
    * @return list of allowed date formats
    */
   public static List<SimpleDateFormat> getDateFormats()
   {
      return dateFormats.get();
   }

   /**
    * Parse date header. Will try to found appropriated format for given date
    * header. Format can be one of see
    * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1" >HTTP/1.1 documentation</a>}
    * .
    *
    * @param header source date header
    * @return parsed Date
    */
   public static Date parseDateHeader(String header)
   {
      try
      {
         for (SimpleDateFormat format : getDateFormats())
            return format.parse(header);
      }
      catch (ParseException e)
      {
         // ignore all ParseException now
      }
      // no one format was found
      throw new IllegalArgumentException("Not found appropriated date format for " + header);
   }

   //

   /**
    * @param httpHeaders HTTP headers
    * @return parsed content-length or null if content-length header is not
    *         specified
    */
   public static long getContentLengthLong(MultivaluedMap<String, String> httpHeaders)
   {
      String t = httpHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
      // to be sure Content-length header is not null, usually it must be not null
      return t != null ? Long.parseLong(t) : 0;
   }

   /**
    * Create string representation of Java Object for adding to response. Method
    * use {@link HeaderDelegate#toString()}.
    *
    * @param o HTTP header as Java type.
    * @return string representation of supplied type
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static String getHeaderAsString(Object o)
   {
      HeaderDelegate hd = RuntimeDelegate.getInstance().createHeaderDelegate(o.getClass());
      if (hd != null)
         return hd.toString(o);
      return o.toString();
   }

   /**
    * Convert Collection&lt;String&gt; to single String, where values separated
    * by ','. Useful for getting source string of HTTP header for next
    * processing quality value of header tokens.
    *
    * @param collection the source list
    * @return String result
    */
   public static String convertToString(Collection<String> collection)
   {
      if (collection == null)
         return null;
      if (collection.size() == 0)
         return "";

      StringBuilder sb = new StringBuilder();
      for (String t : collection)
      {
         if (sb.length() > 0)
            sb.append(',');
         sb.append(t);
      }

      return sb.toString();
   }

   /**
    * Append string in given string buffer, if string contains quotes or
    * whitespace, then it be escaped.
    *
    * @param sb string buffer
    * @param s string
    */
   static void appendWithQuote(StringBuilder sb, String s)
   {
      if (s == null)
         return;
      Matcher m = WHITESPACE_QOUTE_PATTERN.matcher(s);

      if (m.find())
      {
         sb.append('"');
         appendEscapeQuote(sb, s);
         sb.append('"');
         return;
      }

      sb.append(s);

   }

   /**
    * Append string in given string buffer, quotes will be escaped.
    *
    * @param sb string buffer
    * @param s string
    */
   static void appendEscapeQuote(StringBuilder sb, String s)
   {
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (c == '"')
            sb.append('\\');
         sb.append(c);
      }
   }

   /**
    * Remove all whitespace from given string.
    *
    * @param s the source string
    * @return the result string
    */
   static String removeWhitespaces(String s)
   {
      Matcher m = WHITESPACE_PATTERN.matcher(s);
      if (m.find())
         return m.replaceAll("");

      return s;
   }

   /**
    * Add quotes to <code>String</code> if it consists whitespaces, otherwise
    * <code>String</code> will be returned without changes.
    *
    * @param s the source string.
    * @return new string.
    */
   static String addQuotesIfHasWhitespace(String s)
   {
      Matcher macther = WHITESPACE_PATTERN.matcher(s);

      if (macther.find())
         return '"' + s + '"';

      return s;
   }

   /**
    * Check syntax of quality value and parse it. Quality value must have not
    * more then 5 characters and be not more then 1 .
    *
    * @param qstring string representation of quality value
    * @return quality value
    */
   static float parseQualityValue(String qstring)
   {
      if (qstring.length() > 5)
         throw new IllegalArgumentException("Quality value string has more then 5 characters");

      float q = Float.valueOf(qstring);
      if (q > 1.0F)
         throw new IllegalArgumentException("Quality value can't be greater then 1.0");

      return q;
   }

   /**
    * Check is given string token. Token may contains only US-ASCII characters
    * except separators, {@link #SEPARTORS} and controls.
    *
    * @param token the token
    * @return -1 if string has only valid character otherwise index of first
    *         wrong character
    */
   static int isToken(String token)
   {
      for (int i = 0; i < token.length(); i++)
      {
         char c = token.charAt(i);
         if (c > 127 || SEPARATORS[c] != 0)
            return i;
      }
      return -1;
   }

   /**
    * The cookies parameters can be separated by ';' or ',', try to find first
    * available separator in cookie string. If both not found the string length
    * will be returned.
    *
    * @param cookie the cookie string.
    * @param start index for start searching.
    * @return the index of ',' or ';'.
    */
   private static int findCookieParameterSeparator(String cookie, int start)
   {
      int p;

      int comma = cookie.indexOf(',', start);
      int semicolon = cookie.indexOf(';', start);
      if (comma > 0 && semicolon > 0)
         p = comma < semicolon ? comma : semicolon;
      else if (comma < 0 && semicolon > 0)
         p = semicolon;
      else if (comma > 0 && semicolon < 0)
         p = comma;
      else
         p = cookie.length(); // end of string? not comma nor semicolon found

      return p;

   }

   /**
    * Unescape '"' characters in string, e. g.
    * <p>
    * String \"hello \\\"someone\\\"\" will be changed to hello \"someone\"
    * </p>
    *
    * @param token token for processing
    * @return result
    */
   static String filterEscape(String token)
   {
      StringBuilder sb = new StringBuilder();
      //    boolean escape = false;
      int strlen = token.length();

      for (int i = 0; i < strlen; i++)
      {
         char c = token.charAt(i);
         //      escape = !escape && c == '\\';

         if (c == '\\' && i < strlen - 1 && token.charAt(i + 1) == '"')
            continue;
         sb.append(c);
      }

      return sb.toString();
   }

}
