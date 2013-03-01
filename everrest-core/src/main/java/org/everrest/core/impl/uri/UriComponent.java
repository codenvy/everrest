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
package org.everrest.core.impl.uri;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.util.NoSyncByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class UriComponent
{
   // Components of URI, see http://gbiv.com/protocols/uri/rfc/rfc3986.htm
   /** Scheme URI component. */
   public static final int SCHEME = 0;

   /** UserInfo URI component. */
   public static final int USER_INFO = 1;

   /** Host URI component. */
   public static final int HOST = 2;

   /** Port URI component. */
   public static final int PORT = 3;

   /** Path segment URI sub-component, it can't contains '/'. */
   public static final int PATH_SEGMENT = 4;

   /** Path URI components, consists of path-segments. */
   public static final int PATH = 5;

   /** Query string. */
   public static final int QUERY = 6;

   /** Fragment. */
   public static final int FRAGMENT = 7;

   /** Scheme-specific part. */
   public static final int SSP = 8;

   /** Encoded '%' character. */
   public static final String PERCENT = "%25";

   // --------------------

   /** Characters that used for percent encoding. */
   private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   private static final char[][][] ENCODED = new char[9][128][3];

   /** Array of legal characters for each component of URI. */
   private static final int[][] ENCODING = new int[9][128];

   // fill table
   static
   {
      for (int i = SCHEME; i <= SSP; i++)
      {
         ENCODING[i] = new int[128];
      }

      /* The letters of the basic Latin alphabet */
      int[] alphabet = new int[128];
      fillTable(alphabet, 'a', 'z');
      fillTable(alphabet, 'A', 'Z');
      /* Digits */
      int[] digit = new int[128];
      fillTable(digit, '0', '9');
      /* Characters that are allowed in a URI but do not have a reserved purpose are called unreserved. These include
       * uppercase and lowercase letters, decimal digits, hyphen, period, underscore, and tilde.
       * Unreserved = ALPHA | DIGIT | '-' | '.' | '_' | '~' */
      int[] unreserved = new int[128];
      set(alphabet, unreserved);
      set(digit, unreserved);
      unreserved['-'] = 1;
      unreserved['.'] = 1;
      unreserved['_'] = 1;
      unreserved['~'] = 1;
      /* The subset of the reserved characters (gen-delims) is used as delimiters of the generic URI components. */
      int[] gendelim = new int[128];
      gendelim[':'] = 1;
      gendelim['/'] = 1;
      gendelim['?'] = 1;
      gendelim['#'] = 1;
      gendelim['['] = 1;
      gendelim[']'] = 1;
      gendelim['@'] = 1;
      /* Sub-delims characters. */
      int[] subdelim = new int[128];
      subdelim['!'] = 1;
      subdelim['$'] = 1;
      subdelim['&'] = 1;
      subdelim['\''] = 1;
      subdelim['('] = 1;
      subdelim[')'] = 1;
      subdelim['*'] = 1;
      subdelim['+'] = 1;
      subdelim[','] = 1;
      subdelim[';'] = 1;
      subdelim['='] = 1;

      set(alphabet, ENCODING[SCHEME]);
      set(digit, ENCODING[SCHEME]);
      ENCODING[SCHEME]['-'] = 1;
      ENCODING[SCHEME]['+'] = 1;
      ENCODING[SCHEME]['.'] = 1;

      set(unreserved, ENCODING[USER_INFO]);
      set(subdelim, ENCODING[USER_INFO]);
      ENCODING[USER_INFO][':'] = 1;

      set(unreserved, ENCODING[HOST]);
      set(subdelim, ENCODING[HOST]);

      set(digit, ENCODING[PORT]);

      set(unreserved, ENCODING[PATH_SEGMENT]);
      set(subdelim, ENCODING[PATH_SEGMENT]);
      ENCODING[PATH_SEGMENT][':'] = 1;
      ENCODING[PATH_SEGMENT]['@'] = 1;

      set(unreserved, ENCODING[PATH]);
      set(subdelim, ENCODING[PATH]);
      ENCODING[PATH][':'] = 1;
      ENCODING[PATH]['@'] = 1;
      ENCODING[PATH]['/'] = 1;

      set(unreserved, ENCODING[QUERY]);
      ENCODING[QUERY]['-'] = 1;
      ENCODING[QUERY]['.'] = 1;
      ENCODING[QUERY]['_'] = 1;
      ENCODING[QUERY]['~'] = 1;
      ENCODING[QUERY]['!'] = 1;
      ENCODING[QUERY]['$'] = 1;
      ENCODING[QUERY]['\''] = 1;
      ENCODING[QUERY]['('] = 1;
      ENCODING[QUERY][')'] = 1;
      ENCODING[QUERY]['*'] = 1;
      ENCODING[QUERY][','] = 1;
      ENCODING[QUERY][';'] = 1;
      ENCODING[QUERY][':'] = 1;
      ENCODING[QUERY]['@'] = 1;
      ENCODING[QUERY]['?'] = 1;
      ENCODING[QUERY]['/'] = 1;

      System.arraycopy(ENCODING[QUERY], 0, ENCODING[FRAGMENT], 0, ENCODING[QUERY].length);

      set(unreserved, ENCODING[SSP]);
      set(subdelim, ENCODING[SSP]);
      set(gendelim, ENCODING[SSP]);

      for (int i = SCHEME; i <= SSP; i++)
      {
         for (int j = 0; j < 128; j++)
         {
            if (ENCODING[i][j] == 0)
            {
               char[] ca = new char[3];
               ca[0] = '%';
               ca[1] = HEX_DIGITS[j >> 4];
               ca[2] = HEX_DIGITS[j & 0x0F];
               ENCODED[i][j] = ca;
            }
         }
      }
   }

   /** UTF-8 Charset. */
   private static final Charset UTF8 = Charset.forName("UTF-8");

   private static void fillTable(int[] array, char begin, char end)
   {
      if (begin < 0 || end < 0 || begin > 127 || end > 127 || begin > end)
      {
         throw new IllegalArgumentException("Invalid range '" + begin + "' - '" + end + '\'');
      }
      for (char c = begin; c <= end; c++)
      {
         array[c] = 1;
      }
   }

   private static void set(int[] src, int[] dest)
   {
      for (int i = 0, srcLength = src.length; i < srcLength; i++)
      {
         int flag = src[i];
         if (flag == 1)
         {
            dest[i] = 1;
         }
      }
   }

   // -------------------------------------------

   /**
    * Normalization URI according to rfc3986. For details see
    * http://www.unix.com.ua/rfc/rfc3986.html#s6.2.2 .
    *
    * @param uri
    *    source URI
    * @return normalized URI
    */
   public static URI normalize(URI uri)
   {
      String oldPath = uri.getRawPath();
      String normalizedPath = normalize(oldPath);
      if (normalizedPath.equals(oldPath))
      {
         // nothing to do, URI was normalized
         return uri;
      }
      return UriBuilder.fromUri(uri).replacePath(normalizedPath).build();
   }

   private static String normalize(String path)
   {
      String inputBuffer = path;
      StringBuilder outputBuffer = new StringBuilder();
      if (inputBuffer.contains("//"))
      {
         inputBuffer = inputBuffer.replaceAll("//", "/");
      }

      while (inputBuffer.length() != 0)
      {
         // If the input buffer begins with a prefix of "../" or "./", then remove
         // that prefix from the input buffer.
         // http://www.unix.com.ua/rfc/rfc3986.html#sA.
         if (inputBuffer.startsWith("../") || inputBuffer.startsWith("./"))
         {
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf('/') + 1, inputBuffer.length());
            continue;
         }
         // if the input buffer begins with a prefix of "/./" or "/.", where "." is
         // a complete path segment, then replace that prefix with "/" in the input buffer.
         // http://www.unix.com.ua/rfc/rfc3986.html#sB.
         if (inputBuffer.startsWith("/./") || (inputBuffer.startsWith("/.") && isCompletePathSeg(".", inputBuffer)))
         {
            if (inputBuffer.equals("/."))
            {
               inputBuffer = "";
               outputBuffer.append('/');
               continue;
            }
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf('/', 1), inputBuffer.length());
            continue;
         }
         // if the input buffer begins with a prefix of "/../" or "/..", where ".."
         // is a complete path segment, then replace that prefix with "/" in the input buffer and
         // remove the last segment and its preceding "/" (if any) from the output buffer.
         // http://www.unix.com.ua/rfc/rfc3986.html#sC.
         if (inputBuffer.startsWith("/../") || (inputBuffer.startsWith("/..") && isCompletePathSeg("..", inputBuffer)))
         {
            if (inputBuffer.equals("/.."))
            {
               inputBuffer = "";
               outputBuffer.delete(outputBuffer.lastIndexOf("/") + 1, outputBuffer.length());
               continue;
            }
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf('/', 1), inputBuffer.length());
            outputBuffer.delete(outputBuffer.lastIndexOf("/"), outputBuffer.length());
            continue;
         }
         // if the input buffer consists only of "." or "..", then remove that from
         // the input buffer.
         // http://www.unix.com.ua/rfc/rfc3986.html#sD.
         if (inputBuffer.equals(".") || inputBuffer.equals(".."))
         {
            inputBuffer = "";
            continue;
         }
         // move the first path segment in the input buffer to the end of the
         // output buffer, including the initial "/" character (if any) and any subsequent
         // characters up to, but not including, the next "/" character or the end of the
         // input buffer.
         // http://www.unix.com.ua/rfc/rfc3986.html#sE.
         if (inputBuffer.indexOf('/') != inputBuffer.lastIndexOf('/'))
         {
            outputBuffer.append(inputBuffer.substring(0, inputBuffer.indexOf('/', 1)));
            inputBuffer = inputBuffer.substring(inputBuffer.indexOf('/', 1));
         }
         else
         {
            outputBuffer.append(inputBuffer);
            inputBuffer = "";
         }
      }
      return outputBuffer.toString();
   }

   /**
    * Checks if the segment is a complete path segment
    * http://www.unix.com.ua/rfc/rfc3986.html#sB.
    *
    * @param segment
    *    path segment
    * @param path
    *    whole path
    * @return true if segment is complete path segment false otherwise
    */
   private static boolean isCompletePathSeg(String segment, String path)
   {
      return path.equals("/" + segment) || (path.charAt(path.indexOf(segment) + segment.length()) == '/');
   }

   /**
    * Encode given URI string.
    *
    * @param str
    *    the URI string
    * @param containsUriParams
    *    true if the source string contains URI parameters
    * @param component
    *    component of URI, scheme, host, port, etc
    * @return encoded string
    */
   public static String encode(String str, int component, boolean containsUriParams)
   {
      if (str == null)
      {
         throw new IllegalArgumentException();
      }
      return encodingInt(str, component, containsUriParams, false);
   }

   /**
    * Validate content of percent-encoding string.
    *
    * @param str
    *    the string which must be validate
    * @param component
    *    component of URI, scheme, host, port, etc
    * @param containsUriParams
    *    true if the source string contains URI parameters
    * @return the source string
    */
   public static String validate(String str, int component, boolean containsUriParams)
   {
      for (int i = 0; i < str.length(); i++)
      {
         char ch = str.charAt(i);
         if ((!(ch >= 128 || needEncode(ch, component)))
            || (containsUriParams && (ch == '{' || ch == '}'))
            || (ch == '%'))
         {
            continue;
         }
         throw new IllegalArgumentException("Illegal character, index " + i + ": " + str);
      }
      return str;
   }

   /**
    * Check string and if it does not contains any '%' characters validate it
    * for contains only valid characters. If it contains '%' then check does
    * following two character is valid hex numbers, if not then encode '%' to
    * '%25' otherwise keep characters without change, there is no double
    * encoding.
    *
    * @param str
    *    source string
    * @param component
    *    part of URI, e. g. schema, host, path
    * @param containsUriParams
    *    does string may contains URI templates
    * @return valid string
    */
   public static String recognizeEncode(String str, int component, boolean containsUriParams)
   {
      if (str == null)
      {
         throw new IllegalArgumentException();
      }
      return encodingInt(str, component, containsUriParams, true);
   }

   /**
    * @param str
    *    source string
    * @param component
    *    part of URI, e. g. schema, host, path
    * @param containsUriParams
    *    does string may contains URI templates
    * @param recognizeEncoded
    *    must check string to avoid double encoding
    * @return valid string
    */
   private static String encodingInt(String str, int component, boolean containsUriParams, boolean recognizeEncoded)
   {
      int length = str.length();

      boolean encode = false;
      for (int i = 0; i < length && !encode; i++)
      {
         encode = needEncode(str.charAt(i), component);
      }

      if (encode)
      {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < length; i++)
         {
            char ch = str.charAt(i);

            if (ch == '%' && recognizeEncoded)
            {
               if (checkHexCharacters(str, i))
               {
                  sb.append(ch);
                  sb.append(str.charAt(++i));
                  sb.append(str.charAt(++i));
               }
               else
               {
                  sb.append(PERCENT);
               }
            }
            else if (containsUriParams && (ch == '{' || ch == '}'))
            {
               sb.append(ch);
            }
            else if (ch < 128)
            {
               if (needEncode(ch, component))
               {
                  sb.append(ENCODED[component][ch]);
               }
               else
               {
                  sb.append(ch);
               }
            }
            else
            {
               addUTF8Encoded(ch, sb);
            }
         }
         return sb.toString();
      }
      return str;
   }

   /**
    * Decode percent encoded URI string.
    *
    * @param str
    *    the source percent encoded string
    * @param component
    *    component of URI, scheme, host, port, etc. NOTE type of
    *    component is not used currently but will be used for decoding IPv6
    *    addresses
    * @return decoded string
    */
   public static String decode(String str, int component)
   {
      if (str == null)
      {
         throw new IllegalArgumentException("Decoded string is null");
      }

      int length = str.length();

      if (length < 3 && str.indexOf('%') > 0)
      {
         throw new IllegalArgumentException("Malformed string " + str);
      }

      int p = str.lastIndexOf('%');
      if (p > 0 && p > (length - 3))
      {
         throw new IllegalArgumentException("Malformed string at index " + p);
      }

      p = 0; // reset pointer
      StringBuilder sb = new StringBuilder();
      while (p < length)
      {
         char c = str.charAt(p);
         if (c == '%')
         {
            ByteArrayOutputStream out = new NoSyncByteArrayOutputStream(4);
            p = percentDecode(str, p, out);
            byte[] buff = out.toByteArray();

            if (buff.length == 1 && (buff[0] & 0xFF) < 128)
            {
               sb.append((char)buff[0]);
            }
            else
            {
               addUTF8Decoded(buff, sb);
            }
            out.reset();
         }
         else
         {
            sb.append(c == '+' ? ' ' : c);
            p++;
         }
      }
      return sb.toString();
   }

   /**
    * Check must charter be encoded.
    *
    * @param ch
    *    character
    * @param component
    *    the URI component
    * @return true if character must be encoded false otherwise
    */
   private static boolean needEncode(char ch, int component)
   {
      int[] allowed = ENCODING[component];
      return allowed.length <= ch || allowed[ch] == 0;
   }

   /**
    * Append percent encoded character in StringBuilder.
    *
    * @param c
    *    character which must be encoded
    * @param sb
    *    StringBuilder to add character
    */
   private static void addPercentEncoded(int c, StringBuilder sb)
   {
      sb.append('%');
      sb.append(HEX_DIGITS[c >> 4]);
      sb.append(HEX_DIGITS[c & 0x0F]);
   }

   /**
    * Append UTF-8 encoded character in StringBuilder.
    *
    * @param c
    *    character which must be encoded
    * @param sb
    *    StringBuilder to add character
    */
   private static void addUTF8Encoded(char c, StringBuilder sb)
   {
      ByteBuffer buf = UTF8.encode(CharBuffer.wrap(Character.toChars(c)));
      while (buf.hasRemaining())
      {
         addPercentEncoded(buf.get() & 0xFF, sb);
      }
   }

   /**
    * Decode percent encoded string.
    *
    * @param str
    *    the source string
    * @param p
    *    start position in string
    * @param out
    *    output buffer for decoded characters
    * @return current position in source string
    */
   private static int percentDecode(String str, int p, ByteArrayOutputStream out)
   {
      int length = str.length();
      for (; ; )
      {
         char hc = getHexCharacter(str, ++p); // higher char
         char lc = getHexCharacter(str, ++p); // lower char

         int r = ((Character.isDigit(hc) ? hc - '0' : hc - 'A' + 10) << 4)
            | (Character.isDigit(lc) ? lc - '0' : lc - 'A' + 10);

         out.write((byte)r);
         p++;

         if (p == length || str.charAt(p) != '%')
         {
            break;
         }
      }

      return p;
   }

   /**
    * Check does two next characters after '%' represent percent-encoded
    * character.
    *
    * @param s
    *    source string
    * @param p
    *    position of character in string
    * @return true is two characters after '%' represent percent-encoded
    *         character false otherwise
    */
   public static boolean checkHexCharacters(String s, int p)
   {
      if (p > (s.length() - 3))
      {
         return false;
      }
      try
      {
         getHexCharacter(s, ++p);
         getHexCharacter(s, ++p);
         return true;
      }
      catch (IllegalArgumentException e)
      {
         return false;
      }
   }

   /**
    * Extract character from given string and check is it one of valid for hex
    * sequence.
    *
    * @param s
    *    source string
    * @param p
    *    position of character in string
    * @return character
    */
   private static char getHexCharacter(String s, int p)
   {
      char c = s.charAt(p);
      if (c >= '0' && c <= '9')
      {
         return c;
      }
      if (c >= 'A' && c <= 'F')
      {
         return c;
      }
      if (c >= 'a' && c <= 'f')
      {
         return Character.toUpperCase(c);
      }
      throw new IllegalArgumentException("Malformed string at index " + p);
   }

   /**
    * Decodes bytes to characters using the UTF-8 decoding and add them to a
    * StringBuilder.
    *
    * @param buff
    *    source bytes
    * @param sb
    *    StringBuilder for append characters
    */
   private static void addUTF8Decoded(byte[] buff, StringBuilder sb)
   {
      sb.append(UTF8.decode(ByteBuffer.wrap(buff)));
   }

   /**
    * Parse path segments.
    *
    * @param path
    *    the relative path
    * @param decode
    *    true if character must be decoded false otherwise
    * @return List of {@link PathSegment}
    */
   public static List<PathSegment> parsePathSegments(String path, boolean decode)
   {
      List<PathSegment> result = new ArrayList<PathSegment>();
      if (!(path == null || path.isEmpty()))
      {
         // remove leading slash
         if (path.charAt(0) == '/')
         {
            path = path.substring(1);
         }

         int p = 0;
         int n = 0;
         while (n < path.length())
         {
            n = path.indexOf('/', p);
            if (n < 0)
            {
               n = path.length();
            }

            result.add(PathSegmentImpl.fromString(path.substring(p, n), decode));
            p = n + 1;
         }
      }
      return result;
   }

   /**
    * Parse encoded query string.
    *
    * @param rawQuery
    *    source query string
    * @param decode
    *    if true then query parameters will be decoded
    * @return {@link MultivaluedMap} with query parameters
    */
   public static MultivaluedMap<String, String> parseQueryString(String rawQuery, boolean decode)
   {
      MultivaluedMap<String, String> result = new MultivaluedMapImpl();
      if (!(rawQuery == null || rawQuery.isEmpty()))
      {
         int p = 0;
         int n = 0;
         while (n < rawQuery.length())
         {
            n = rawQuery.indexOf('&', p);
            if (n < 0)
            {
               n = rawQuery.length();
            }

            String pair = rawQuery.substring(p, n);
            if (!pair.isEmpty())
            {
               String name;
               String value;
               int eq = pair.indexOf('=');
               if (eq < 0)
               {
                  // no value
                  name = pair;
                  value = "";
               }
               else
               {
                  name = pair.substring(0, eq);
                  value = pair.substring(eq + 1);
               }

               result.add(decode ? decode(name, QUERY) : name, decode ? decode(value, QUERY) : value);

               p = n + 1;
            }
         }
      }
      return result;
   }

   private UriComponent()
   {
   }
}
