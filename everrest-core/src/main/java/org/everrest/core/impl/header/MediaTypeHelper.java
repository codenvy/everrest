/*
 * Copyright (c) 2012-2022 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import static jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static java.util.stream.Collectors.toList;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everrest.core.util.MediaTypeComparator;

public final class MediaTypeHelper {
  /** List which contains default media type. */
  private static final List<MediaType> DEFAULT_TYPE_LIST =
      Collections.singletonList(MediaType.WILDCARD_TYPE);

  /** WADL media type. */
  public static final MediaType WADL_TYPE = new MediaType("application", "vnd.sun.wadl+xml");

  /** Prefix of sub-type part of media types as application/*+xml. */
  public static final String EXT_PREFIX_SUBTYPE = "*+";

  /** Media types as application/atom+* or application/*+xml pattern. */
  public static final Pattern EXT_SUBTYPE_PATTERN = Pattern.compile("([^\\+]+)\\+(.+)");

  /** Media types as application/atom+* pattern. */
  public static final Pattern EXT_SUFFIX_SUBTYPE_PATTERN = Pattern.compile("([^\\+]+)\\+\\*");

  /** Media types as application/*+xml pattern. */
  public static final Pattern EXT_PREFIX_SUBTYPE_PATTERN = Pattern.compile("\\*\\+(.+)");

  /**
   * Creates range of acceptable media types for look up appropriate {@link MessageBodyReader},
   * {@link MessageBodyWriter} or {@link ContextResolver}. It provides set of media types in
   * descending ordering.
   *
   * <p>For given media type: {@code application/xml}
   * <li>{@code application/xml}
   * <li>{@code application&#47;*+xml}
   * <li>{@code application&#47;*}
   * <li>{@code *&#47;*}
   */
  public static Iterator<MediaType> createDescendingMediaTypeIterator(MediaType type) {
    return new MediaTypeRange(type);
  }

  public static final class MediaTypeRange implements Iterator<MediaType> {
    private MediaType next;

    public MediaTypeRange(MediaType type) {
      next = (type == null) ? MediaType.WILDCARD_TYPE : type;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public MediaType next() {
      if (next == null) {
        throw new NoSuchElementException();
      }
      MediaType type = next;
      fetchNext();
      return type;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    void fetchNext() {
      MediaType current = next;
      if (current.isWildcardType() && current.isWildcardSubtype()) {
        next = null;
      } else if (current.isWildcardSubtype()) {
        // Type such as 'application/*' . Next one to be checked is '*/*'.
        // This type is always last for checking in our range.
        next = MediaType.WILDCARD_TYPE;
      } else {
        // Media type such as application/xml, application/atom+xml, application/*+xml or
        // application/xml+* .
        String type = current.getType();
        String subType = current.getSubtype();
        Matcher extMatcher = EXT_SUBTYPE_PATTERN.matcher(subType);
        if (extMatcher.matches()) {
          // Media type such as application/atom+xml or application/*+xml (sub-type extended!!!)
          String extSubtypePrefix = extMatcher.group(1);
          String extSubtype = extMatcher.group(2);
          if (MEDIA_TYPE_WILDCARD.equals(extSubtypePrefix)) {
            // Media type such as 'application/*+xml' (first part is wildcard). Next to be checked
            // will be 'application/*'.
            next = new MediaType(type, MEDIA_TYPE_WILDCARD);
          } else {
            // Media type such as 'application/atom+xml' next to be checked will be
            // 'application/*+xml'
            next = new MediaType(type, EXT_PREFIX_SUBTYPE + extSubtype);
          }
        } else {
          // Media type without extension such as 'application/xml'.
          // Next will be 'application/*+xml' since extensions should support pure xml as well.
          next = new MediaType(type, EXT_PREFIX_SUBTYPE + subType);
        }
      }
    }
  }
  ;

  /**
   * Creates a list of media type for given &#64;Consumes annotation. If parameter mime is {@code
   * null} then returns list with single element {@link MediaType#WILDCARD_TYPE}.
   *
   * @param mime the Consumes annotation.
   * @return ordered list of media types.
   */
  public static List<MediaType> createConsumesList(Consumes mime) {
    if (mime == null) {
      return DEFAULT_TYPE_LIST;
    }

    return createMediaTypesList(mime.value());
  }

  /**
   * Creates a list of media type for given Produces annotation. If parameter mime is {@code null}
   * then return list with single element {@link MediaType#WILDCARD_TYPE}.
   *
   * @param mime the Produces annotation.
   * @return ordered list of media types.
   */
  public static List<MediaType> createProducesList(Produces mime) {
    if (mime == null) {
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
  public static boolean isConsume(List<MediaType> consumes, MediaType contentType) {
    return consumes.stream().anyMatch(consume -> isMatched(consume, contentType));
  }

  /**
   * Create a list of media type from string array.
   *
   * @param mimes source string array
   * @return ordered list of media types
   */
  private static List<MediaType> createMediaTypesList(String[] mimes) {
    return Arrays.stream(mimes)
        .map(MediaType::valueOf)
        .sorted(new MediaTypeComparator())
        .collect(toList());
  }

  /**
   * Looking for first accept media type that is matched to first media type from {@code
   * producedByResource}.
   *
   * @param acceptMediaTypes See {@link AcceptMediaType}
   * @param producedByResource list of produces media type, See {@link Produces}
   * @return the best found compatible accept media type or {@code null} if media types are not
   *     compatible
   */
  public static AcceptMediaType findFistCompatibleAcceptMediaType(
      List<AcceptMediaType> acceptMediaTypes, List<MediaType> producedByResource) {
    for (AcceptMediaType accept : acceptMediaTypes) {
      if (accept.isWildcardType()) {
        return accept;
      }
      for (MediaType produce : producedByResource) {
        if (produce.isCompatible(accept.getMediaType())) {
          return accept;
        }
      }
    }

    return null;
  }

  /**
   * Checks that types {@code mediaTypeOne} and type {@code mediaTypeTwo} are compatible. The
   * operation is commutative.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li><i>text/plain</i> and <i>text/*</i> are compatible
   *   <li><i>application/atom+xml</i> and <i>application/atom+*</i> are compatible
   * </ul>
   *
   * @param mediaTypeOne media type
   * @param mediaTypeTwo media type
   * @return {@code true} if types compatible and {@code false} otherwise
   */
  public static boolean isCompatible(MediaType mediaTypeOne, MediaType mediaTypeTwo) {
    if (mediaTypeOne == null || mediaTypeTwo == null) {
      throw new IllegalArgumentException("Null arguments are not allowed");
    }

    if (mediaTypeOne.isWildcardType() || mediaTypeTwo.isWildcardType()) {
      return true;
    }

    if (mediaTypeOne.getType().equalsIgnoreCase(mediaTypeTwo.getType())) {

      if (mediaTypeOne.isWildcardSubtype()
          || mediaTypeTwo.isWildcardSubtype()
          || mediaTypeOne.getSubtype().equalsIgnoreCase(mediaTypeTwo.getSubtype())) {

        return true;
      }

      Matcher extSubtypeMatcherOne = EXT_SUBTYPE_PATTERN.matcher(mediaTypeOne.getSubtype());
      Matcher extSubtypeMatcherTwo = EXT_SUBTYPE_PATTERN.matcher(mediaTypeTwo.getSubtype());
      boolean extSubtypeMatcherOneMatches = extSubtypeMatcherOne.matches();
      boolean extSubtypeMatcherTwoMatches = extSubtypeMatcherTwo.matches();

      if (!extSubtypeMatcherOneMatches && extSubtypeMatcherTwoMatches) {

        // one is type such as application/xml
        // two is type such as application/atom+xml, application/*+xml, application/xml+*
        return mediaTypeOne.getSubtype().equalsIgnoreCase(extSubtypeMatcherTwo.group(1))
            || mediaTypeOne.getSubtype().equalsIgnoreCase(extSubtypeMatcherTwo.group(2));

      } else if (extSubtypeMatcherOneMatches && !extSubtypeMatcherTwoMatches) {

        // one is type such as application/atom+xml, application/*+xml, application/xml+*
        // two is type such as application/xml
        return mediaTypeTwo.getSubtype().equalsIgnoreCase(extSubtypeMatcherOne.group(1))
            || mediaTypeTwo.getSubtype().equalsIgnoreCase(extSubtypeMatcherOne.group(2));

      } else if (extSubtypeMatcherOneMatches && extSubtypeMatcherTwoMatches) {

        // both types are extended types
        String subtypePrefixOne = extSubtypeMatcherOne.group(1);
        String subTypeSuffixOne = extSubtypeMatcherOne.group(2);
        String subTypePrefixTwo = extSubtypeMatcherTwo.group(1);
        String subtypeSuffixTwo = extSubtypeMatcherTwo.group(2);

        if (subtypePrefixOne.equalsIgnoreCase(subTypePrefixTwo)
            && (MEDIA_TYPE_WILDCARD.equals(subTypeSuffixOne)
                || MEDIA_TYPE_WILDCARD.equals(subtypeSuffixTwo))) {
          // parts before '+' are the same and one of after '+' is wildcard '*'
          // For example two sub-types: atom+* and atom+xml
          return true;
        }
        if (subTypeSuffixOne.equalsIgnoreCase(subtypeSuffixTwo)
            && (MEDIA_TYPE_WILDCARD.equals(subtypePrefixOne)
                || MEDIA_TYPE_WILDCARD.equals(subTypePrefixTwo))) {
          // parts after '+' are the same and one of before '+' is wildcard '*'
          // For example two sub-types: *+xml and atom+xml
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks that type {@code checkMe} is matched to type {@code pattern}. NOTE The operation is NOT
   * commutative, e.g. matching of type {@code checkMe} to {@code pattern} does not guaranty that
   * {@code pattern} is matched to {@code checkMe}.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li><i>text/plain</i> is matched to <i>text/*</i> but type <i>text/*</i> is not matched to
   *       <i>text/plain</i>
   *   <li><i>application/atom+xml</i> is matched to <i>application/atom+*</i> but type
   *       <i>application/atom+*</i> is not matched to <i>application/atom+xml</i>
   * </ul>
   *
   * @param pattern pattern type
   * @param checkMe type to be checked
   * @return {@code true} if type {@code checkMe} is matched to {@code pattern} and {@code false}
   *     otherwise
   */
  public static boolean isMatched(MediaType pattern, MediaType checkMe) {
    if (pattern == null || checkMe == null) {
      throw new IllegalArgumentException("Null arguments are not allowed");
    }

    if (pattern.isWildcardType()) {
      return true;
    }

    if (pattern.getType().equalsIgnoreCase(checkMe.getType())) {
      if (pattern.isWildcardSubtype()
          || pattern.getSubtype().equalsIgnoreCase(checkMe.getSubtype())) {
        return true;
      }

      Matcher patternMatcher = EXT_SUBTYPE_PATTERN.matcher(pattern.getSubtype());
      Matcher checkMeMatcher = EXT_SUBTYPE_PATTERN.matcher(checkMe.getSubtype());

      if (patternMatcher.matches()) {
        String patternPrefix = patternMatcher.group(1);
        String patternSuffix = patternMatcher.group(2);

        if (!checkMeMatcher.matches()) {
          // pattern is type such as application/atom+xml, application/*+xml, application/xml+*
          // checkMe is type such as application/xml
          return checkMe.getSubtype().equalsIgnoreCase(patternPrefix)
              || checkMe.getSubtype().equalsIgnoreCase(patternSuffix);
        }

        // both types are extended types
        String checkMePrefix = checkMeMatcher.group(1);
        String checkMeSuffix = checkMeMatcher.group(2);

        if (MEDIA_TYPE_WILDCARD.equals(patternSuffix)
            && patternPrefix.equalsIgnoreCase(checkMePrefix)) {
          // parts before '+' are the same and pattern after '+' is wildcard '*'
          // For example two sub-types: atom+* and atom+xml
          return true;
        }

        if (MEDIA_TYPE_WILDCARD.equals(patternPrefix)
            && patternSuffix.equalsIgnoreCase(checkMeSuffix)) {
          // parts after '+' are the same and pattern before '+' is wildcard '*'
          // For example two sub-types: *+xml and atom+xml
          return true;
        }
      }
    }

    return false;
  }

  private MediaTypeHelper() {}
}
