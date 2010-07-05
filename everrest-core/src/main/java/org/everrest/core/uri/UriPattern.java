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
package org.everrest.core.uri;

import org.everrest.core.impl.uri.UriComponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: UriPattern.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class UriPattern
{

   /**
    * Sort the templates according to the string comparison of the template
    * regular expressions.
    * <p>
    * JSR-311 specification: "Sort the set of matching resource classes using
    * the number of characters in the regular expression not resulting from
    * template variables as the primary key and the number of matching groups as
    * a secondary key"
    * </p>
    */
   public static final Comparator<UriPattern> URIPATTERN_COMPARATOR = new UriPatternComparator();

   /**
    * URI pattern comparator.
    */
   private static final class UriPatternComparator implements Comparator<UriPattern>
   {
      /**
       * {@inheritDoc}
       */
      public int compare(UriPattern o1, UriPattern o2)
      {
         if (o1 == null & o2 == null)
            return 0;
         if (o1 == null)
            return 1;
         if (o2 == null)
            return -1;

         if ("".equals(o1.getTemplate()) && "".equals(o2.getTemplate()))
            return 0;
         if ("".equals(o1.getTemplate()))
            return 1;
         if ("".equals(o2.getTemplate()))
            return -1;

         if (o1.getNumberOfLiteralCharacters() < o2.getNumberOfLiteralCharacters())
            return 1;
         if (o1.getNumberOfLiteralCharacters() > o2.getNumberOfLiteralCharacters())
            return -1;

         // pattern with two variables less the pattern with four variables
         if (o1.getParameterNames().size() < o2.getParameterNames().size())
            return 1;
         if (o1.getParameterNames().size() > o2.getParameterNames().size())
            return -1;

         return o1.getRegex().compareTo(o2.getRegex());
      }

   }

   /**
    * Should be added in URI pattern regular expression.
    */
   private static final String URI_PATTERN_TAIL = "(/.*)?";

   //

   /**
    * List of names for URI template variables.
    */
   private final List<String> parameterNames;

   /**
    * URI template.
    */
   private final String template;

   /**
    * Number of characters in URI template NOT resulting from template variable
    * substitution.
    */
   private final int numberOfCharacters;

   /**
    * Compiled URI pattern.
    */
   private final Pattern pattern;

   /**
    * Regular expressions for URI pattern.
    */
   private final String regex;

   /**
    * Regex capturing group indexes.
    */
   private final int[] groupIndexes;

   //

   /**
    * Constructs UriPattern.
    *
    * @param template the source template
    * @see {@link javax.ws.rs.Path}
    */
   public UriPattern(String template)
   {
      if (template.length() > 0 && template.charAt(0) != '/')
         template = "/" + template;

      UriTemplateParser parser = new UriTemplateParser(template);
      this.template = parser.getTemplate();
      this.parameterNames = Collections.unmodifiableList(parser.getParameterNames());
      this.numberOfCharacters = parser.getNumberOfLiteralCharacters();

      int[] indxs = parser.getGroupIndexes();
      if (indxs != null)
      {
         this.groupIndexes = new int[indxs.length + 1];
         System.arraycopy(indxs, 0, this.groupIndexes, 0, indxs.length);
         // Add one more index for URI_PATTERN_TAIL
         this.groupIndexes[groupIndexes.length - 1] = indxs[indxs.length - 1] + 1;
      }
      else
      {
         this.groupIndexes = null;
      }

      String regex = parser.getRegex();
      if (regex.endsWith("/"))
         regex = regex.substring(0, regex.length() - 1);
      this.regex = regex + URI_PATTERN_TAIL;
      this.pattern = Pattern.compile(this.regex);
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (obj.getClass() != getClass())
         return false;
      return getRegex().equals(((UriPattern)obj).getRegex());
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return template.hashCode() + regex.hashCode();
   }

   /**
    * Get the regex pattern.
    *
    * @return the regex pattern
    */
   public Pattern getPattern()
   {
      return pattern;
   }

   /**
    * Get the URI template as a String.
    *
    * @return the URI template
    */
   public String getTemplate()
   {
      return template;
   }

   /**
    * Get the regular expression.
    *
    * @return the regular expression
    */
   public String getRegex()
   {
      return regex;
   }

   /**
    * Get the number of literal characters in the template.
    *
    * @return number of literal characters in the template
    */
   public int getNumberOfLiteralCharacters()
   {
      return numberOfCharacters;
   }

   /**
    * @return list of names
    */
   public List<String> getParameterNames()
   {
      return parameterNames;
   }

   /**
    * Check is URI string match to pattern. If it is then fill given list by
    * parameter value. Before coping value list is cleared. List will be 1
    * greater then number of keys. It can be used for check is resource is
    * matching to requested. If resource is match the last element in list must
    * be '/' or null.
    *
    * @param uri the URI string
    * @param parameters target list
    * @return true if URI string is match to pattern, false otherwise
    */
   public boolean match(String uri, List<String> parameters)
   {

      if (parameters == null)
         throw new IllegalArgumentException("list is null");

      if (uri == null || uri.length() == 0)
      {
         if (pattern == null)
            return true;

         return false;
      }
      else if (pattern == null)
      {
         return false;
      }

      Matcher m = pattern.matcher(uri);
      if (!m.matches())
         return false;

      parameters.clear();
      if (groupIndexes == null)
      {
         for (int i = 1; i <= m.groupCount(); i++)
            parameters.add(m.group(i));
      }
      else
      {
         for (int i = 0; i < groupIndexes.length - 1; i++)
            parameters.add(m.group(groupIndexes[i]));
      }
      return true;

   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return regex;
   }

   /**
    * Create URI from URI part. Each URI part can contains templates.
    *
    * @param schema the schema URI part
    * @param userInfo the user info URI part
    * @param host the host name URI part
    * @param port the port number URI part
    * @param path the path URI part
    * @param query the query string URI part
    * @param fragment the fragment URI part
    * @param values the values which must be used instead templates parameters
    * @param encode if true then encode value before add it in URI, otherwise
    *        value must be validate to legal characters
    * @return the URI string
    */
   public static String createUriWithValues(String schema, String userInfo, String host, int port, String path,
      String query, String fragment, Map<String, ? extends Object> values, boolean encode)
   {

      StringBuffer sb = new StringBuffer();
      if (schema != null)
      {
         appendUriPart(sb, schema, UriComponent.SCHEME, values, false);
         sb.append(':');
      }
      if (userInfo != null || host != null || port != -1)
      {
         sb.append('/').append('/');

         if (userInfo != null && userInfo.length() > 0)
         {
            appendUriPart(sb, userInfo, UriComponent.USER_INFO, values, encode);
            sb.append('@');
         }
         if (host != null)
            appendUriPart(sb, host, UriComponent.HOST, values, encode);

         if (port != -1)
         {
            sb.append(':');
            appendUriPart(sb, "" + port, UriComponent.PORT, values, encode);
         }

      }

      if (path != null)
      {
         if (sb.length() > 0 && path.charAt(0) != '/')
            sb.append('/');
         appendUriPart(sb, path, UriComponent.PATH, values, encode);
      }

      if (query != null && query.length() > 0)
      {
         sb.append('?');
         appendUriPart(sb, query, UriComponent.QUERY, values, encode);
      }

      if (fragment != null && fragment.length() > 0)
      {
         sb.append('#');
         appendUriPart(sb, fragment, UriComponent.FRAGMENT, values, encode);
      }

      return sb.toString();
   }

   /**
    * Create URI from URI part. Each URI part can contains templates.
    *
    * @param schema the schema URI part
    * @param userInfo the user info URI part
    * @param host the host name URI part
    * @param port the port number URI part
    * @param path the path URI part
    * @param query the query string URI part
    * @param fragment the fragment URI part
    * @param values the values which must be used instead templates parameters
    * @param encode if true then encode value before add it in URI, otherwise
    *        value must be validate to legal characters
    * @return the URI string
    */
   public static String createUriWithValues(String schema, String userInfo, String host, int port, String path,
      String query, String fragment, Object[] values, boolean encode)
   {

      Map<String, String> m = new HashMap<String, String>();
      StringBuffer sb = new StringBuffer();
      int p = 0;

      if (schema != null)
      {
         p = appendUriPart(sb, schema, UriComponent.SCHEME, values, p, m, false);
         sb.append(':');
      }
      if (userInfo != null || host != null || port != -1)
      {
         sb.append('/').append('/');

         if (userInfo != null && userInfo.length() > 0)
         {
            p = appendUriPart(sb, userInfo, UriComponent.USER_INFO, values, p, m, encode);
            sb.append('@');
         }
         if (host != null)
            p = appendUriPart(sb, host, UriComponent.HOST, values, p, m, encode);

         if (port != -1)
         {
            sb.append(':');
            p = appendUriPart(sb, "" + port, UriComponent.PORT, values, p, m, encode);
         }

      }

      if (path != null)
      {
         if (sb.length() > 0 && path.length() > 0 && path.charAt(0) != '/')
            sb.append('/');
         p = appendUriPart(sb, path, UriComponent.PATH, values, p, m, encode);
      }

      if (query != null && query.length() > 0)
      {
         sb.append('?');
         p = appendUriPart(sb, query, UriComponent.QUERY, values, p, m, encode);
      }

      if (fragment != null && fragment.length() > 0)
      {
         sb.append('#');
         p = appendUriPart(sb, fragment, UriComponent.FRAGMENT, values, p, m, encode);
      }

      return sb.toString();

   }

   /**
    * @param sb the StringBuffer for appending URI part
    * @param uriPart URI part
    * @param component the URI component
    * @param values values map
    * @param encode if true then encode value before add it in URI, otherwise
    *        value must be validate to legal characters
    */
   private static void appendUriPart(StringBuffer sb, String uriPart, int component,
      Map<String, ? extends Object> values, boolean encode)
   {

      if (!hasUriTemplates(uriPart))
      {
         sb.append(uriPart);
         return;
      }

      Matcher m = UriTemplateParser.URI_PARAMETERS_PATTERN.matcher(uriPart);

      int start = 0;
      while (m.find())
      {
         sb.append(uriPart, start, m.start()); // 'static' part
         String param = uriPart.substring(m.start() + 1, m.end() - 1);

         Object o = values.get(param);
         if (o == null)
            throw new IllegalArgumentException("Not found corresponding value for parameter " + param);

         String value = o.toString();
         sb.append(encode ? UriComponent.encode(value, component, true) : UriComponent.recognizeEncode(value,
            component, true));
         start = m.end();
      }

      // copy the last part or uriPart
      sb.append(uriPart, start, uriPart.length());
   }

   /**
    * @param sb the StringBuffer for appending URI part
    * @param uriPart URI part
    * @param component the URI component
    * @param sourceValues the source array of values
    * @param offset the offset in array
    * @param values values map, keep parameter/value pair which have been
    *        already found. From java docs:
    *        <p>
    *        All instances of the same template parameter will be replaced by
    *        the same value that corresponds to the position of the first
    *        instance of the template parameter. e.g. the template "{a}/{b}/{a}"
    *        with values {"x", "y", "z"} will result in the the URI "x/y/x",
    *        <i>not</i> "x/y/z".
    *        </p>
    * @param encode if true then encode value before add it in URI, otherwise
    *        value must be validate to legal characters
    * @return offset
    */
   private static int appendUriPart(StringBuffer sb, String uriPart, int component, Object[] sourceValues, int offset,
      Map<String, String> values, boolean encode)
   {

      if (!hasUriTemplates(uriPart))
      {
         sb.append(uriPart);
         return offset;
      }

      Matcher m = UriTemplateParser.URI_PARAMETERS_PATTERN.matcher(uriPart);

      int start = 0;
      while (m.find())
      {
         sb.append(uriPart, start, m.start()); // 'static' part
         String param = uriPart.substring(m.start() + 1, m.end() - 1);

         String value = values.get(param);
         if (value != null)
         {
            // Value already known, then don't take new one from array. Value from
            // map is already validate or encoded, so do nothing about it
            sb.append(value);
         }
         else
         {
            // Value is unknown, we met it first time, then process it and keep in
            // map. Value will be encoded (or validate)before putting in map.
            if (offset < sourceValues.length)
               value = sourceValues[offset++].toString();

            if (value != null)
            {
               value =
                  encode ? UriComponent.encode(value, component, true) : UriComponent.recognizeEncode(value, component,
                     true);
               values.put(param, value);
               sb.append(value);
            }
            else
               throw new IllegalArgumentException("Not found corresponding value for parameter " + param);

         }
         start = m.end();
      }

      // copy the last part or uriPart
      sb.append(uriPart, start, uriPart.length());

      return offset;
   }

   /**
    * Check does given URI string has templates.
    *
    * @param uri the URI which must be checked
    * @return true if URI has templates false otherwise
    */
   private static boolean hasUriTemplates(String uri)
   {
      return uri.indexOf('{') != -1;
   }

}
