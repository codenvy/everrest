/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import com.google.common.base.Joiner;

import org.everrest.core.header.QualityValue;
import org.everrest.core.impl.header.ListHeaderProducer.ListItemFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.everrest.core.impl.header.CookieBuilder.aCookie;
import static org.everrest.core.impl.header.NewCookieBuilder.aNewCookie;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.doesNotContain;
import static org.everrest.core.util.StringUtils.scan;

public class HeaderHelper {
    private HeaderHelper() {
    }

    /** Pattern for search whitespace and quote in string. */
    private static final Pattern WHITESPACE_QUOTE_PATTERN = Pattern.compile("[\\s\"]");

    /** Pattern for search whitespace in string. */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    /** Header separators. Header token MUST NOT contains any of it. */
    private static final boolean[] SEPARATORS = new boolean[128];

    static {
        for (char c : "()<>@,;:\"\\/[]?={}".toCharArray()) {
            SEPARATORS[c] = true;
        }
    }

    /** Accept all media type list. */
    private static final List<AcceptMediaType> ACCEPT_ALL_MEDIA_TYPE = Collections.singletonList(AcceptMediaType.DEFAULT);

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
    public static final Comparator<QualityValue> QUALITY_VALUE_COMPARATOR = new Comparator<QualityValue>() {
        @Override
        public int compare(QualityValue qualityValueOne, QualityValue qualityValueTwo) {
            float q1 = qualityValueOne.getQvalue();
            float q2 = qualityValueTwo.getQvalue();
            if (q1 < q2) {
                return 1;
            }
            if (q1 > q2) {
                return -1;
            }
            return 0;
        }
    };

    // accept headers

    /**
     * Accept media type producer.
     *
     * @see ListHeaderProducer
     */
    private static final ListHeaderProducer<AcceptMediaType> LIST_MEDIA_TYPE_PRODUCER = new ListHeaderProducer<>(new AcceptMediaTypeFactory());

    /**
     * Creates sorted by quality value accepted media type list.
     *
     * @param header
     *         source header string
     * @return List of AcceptMediaType
     */
    public static List<AcceptMediaType> createAcceptMediaTypeList(String header) {
        if (isNullOrEmpty(header) || WILDCARD.equals(header.trim())) {
            return ACCEPT_ALL_MEDIA_TYPE;
        }
        return LIST_MEDIA_TYPE_PRODUCER.createQualitySortedList(header);
    }

    /**
     * Accept language producer.
     *
     * @see ListHeaderProducer
     */
    private static final ListHeaderProducer<AcceptLanguage> LIST_LANGUAGE_PRODUCER = new ListHeaderProducer<>(new AcceptLanguageFactory());

    /**
     * Creates sorted by quality value accepted language list.
     *
     * @param header
     *         source header string
     * @return List of AcceptLanguage
     */
    public static List<AcceptLanguage> createAcceptedLanguageList(String header) {
        if (isNullOrEmpty(header) || "*".equals(header)) {
            return ACCEPT_ALL_LANGUAGE;
        }
        return LIST_LANGUAGE_PRODUCER.createQualitySortedList(header);
    }

    /**
     * Accept token producer. Useful for processing 'accept-charset' and 'accept-encoding' request headers.
     *
     * @see ListHeaderProducer
     */
    private static final ListHeaderProducer<AcceptToken> LIST_TOKEN_PRODUCER = new ListHeaderProducer<>(new AcceptTokenFactory());

    /**
     * Creates sorted by quality value 'accept-character' list.
     *
     * @param header
     *         source header string
     * @return List of accept charset tokens
     */
    public static List<AcceptToken> createAcceptedCharsetList(String header) {
        if (isNullOrEmpty(header) || "*".equals(header)) {
            return ACCEPT_ALL_TOKENS;
        }
        return LIST_TOKEN_PRODUCER.createQualitySortedList(header);
    }

    /**
     * Creates sorted by quality value 'accept-encoding' list.
     *
     * @param header
     *         source header string
     * @return List of accept encoding tokens
     */
    public static List<AcceptToken> createAcceptedEncodingList(String header) {
        if (isNullOrEmpty(header) || "*".equals(header)) {
            return ACCEPT_ALL_TOKENS;
        }
        return LIST_TOKEN_PRODUCER.createQualitySortedList(header);
    }

    /**
     * Parses cookie header string and create collection of cookie from it.
     *
     * @param cookiesString
     *         the cookie string.
     * @return collection of Cookie.
     */
    public static List<Cookie> parseCookies(String cookiesString) {
        final List<Cookie> cookies = new ArrayList<>();

        int p = 0;
        int n;
        CookieBuilder cookieBuilder = null;
        int version = Cookie.DEFAULT_VERSION;

        while (p < cookiesString.length()) {
            n = findCookieParameterSeparator(cookiesString, p);

            String pair = cookiesString.substring(p, n);

            String name;
            String value = "";

            int eq = scan(pair, '=');
            if (charAtIs(pair, eq, '=')) {
                name = pair.substring(0, eq).trim();
                value = pair.substring(eq + 1).trim();
                if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
            } else {
                name = pair.trim();
            }

            // Name of parameter not start from '$', then it is cookie name and value.
            // In header string name/value pair (name without '$') SHOULD be before
            // '$Path' and '$Domain' parameters, but '$Version' goes before name/value pair.
            if (doesNotContain(name, '$')) {
                if (cookieBuilder != null) {
                    cookies.add(cookieBuilder.build());
                }
                cookieBuilder = aCookie().withName(name).withValue(value);
                // version was kept before http://www.ietf.org/rfc/rfc2109.txt section 4.4
                cookieBuilder.withVersion(version);
            } else if ("$Version".equalsIgnoreCase(name)) {
                version = Integer.valueOf(value);
            } else if (cookieBuilder != null && "$Path".equalsIgnoreCase(name)) {
                cookieBuilder.withPath(value);
            } else if (cookieBuilder != null && "$Domain".equalsIgnoreCase(name)) {
                cookieBuilder.withDomain(value);
            }

            p = n + 1;
        }

        if (cookieBuilder != null) {
            cookies.add(cookieBuilder.build());
        }

        return cookies;
    }

    /**
     * Parses cookie header string and create collection of NewCookie from it.
     *
     * @param newCookieString
     *         the new cookie string.
     * @return collection of NewCookie.
     */
    public static NewCookie parseNewCookie(String newCookieString) {
        int p = 0;
        int n = findCookieParameterSeparator(newCookieString, p);
        int separator = -1;
        if (n > 0 && n < newCookieString.length()) {
            separator = newCookieString.charAt(n);
        }
        NewCookieBuilder newCookieBuilder = null;

        while (p < newCookieString.length()) {

            String pair = newCookieString.substring(p, n);

            String name;
            String value = "";

            int eq = scan(pair, '=');
            if (charAtIs(pair, eq, '=')) {
                name = pair.substring(0, eq).trim();
                value = pair.substring(eq + 1).trim();
                if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
            } else {
                name = pair.trim();
            }

            if (newCookieBuilder == null) {
                newCookieBuilder = aNewCookie().withName(name).withValue(value).withVersion(Cookie.DEFAULT_VERSION);
            } else {
                if (name.equalsIgnoreCase("version")) {
                    newCookieBuilder.withVersion(Integer.parseInt(value));
                } else if (name.equalsIgnoreCase("domain")) {
                    newCookieBuilder.withDomain(value);
                } else if (name.equalsIgnoreCase("path")) {
                    newCookieBuilder.withPath(value);
                } else if (name.equalsIgnoreCase("secure")) {
                    newCookieBuilder.withSecure(true);
                } else if (name.equalsIgnoreCase("HttpOnly")) {
                    newCookieBuilder.withHttpOnly(true);
                } else if (name.equalsIgnoreCase("Max-Age")) {
                    newCookieBuilder.withMaxAge(Integer.parseInt(value));
                } else if (name.equalsIgnoreCase("expires")) {
                    try {
                        newCookieBuilder.withExpiry(parseDateHeader(value));
                    } catch (IllegalArgumentException ignored) {
                        ignored.printStackTrace();
                    }
                } else if (name.equalsIgnoreCase("comment")) {
                    newCookieBuilder.withComment(value);
                }
            }
            if (separator == -1) {
                break;
            }
            p = n + 1;
            n = scan(newCookieString, p, (char)separator);
        }

        if (newCookieBuilder == null) {
            return null;
        }
        return newCookieBuilder.build();
    }

    // Date

    // HTTP applications have historically allowed three different formats
    // for the representation of date/time stamps
    // For example :
    // Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
    // Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
    // Sun Nov 6 08:49:37 1994        ; ANSI C's asctime() format

    private static class DateFormats {
        /** RFC 822, updated by RFC 1123. */
        static final String RFC_1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

        /** RFC 850, obsoleted by RFC 1036. */
        static final String RFC_1036_DATE_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss zzz";

        /** ANSI C's asctime() format. */
        static final String ANSI_C_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";

        static final SimpleDateFormat[] formats = createFormats();

        private static SimpleDateFormat[] createFormats() {
            SimpleDateFormat[] formats = new SimpleDateFormat[3];
            formats[0] = new SimpleDateFormat(RFC_1123_DATE_FORMAT, Locale.US);
            formats[1] = new SimpleDateFormat(RFC_1036_DATE_FORMAT, Locale.US);
            formats[2] = new SimpleDateFormat(ANSI_C_DATE_FORMAT, Locale.US);
            TimeZone tz = TimeZone.getTimeZone("GMT");
            formats[0].setTimeZone(tz);
            formats[1].setTimeZone(tz);
            formats[2].setTimeZone(tz);
            return formats;
        }
    }

    /**
     * Parses date header. Will try to found appropriated format for given date header. Format can be one of described in
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1" >HTTP/1.1 documentation</a> .
     *
     * @param header
     *         source date header
     * @return parsed Date
     */
    public static Date parseDateHeader(String header) {
        for (SimpleDateFormat format : DateFormats.formats) {
            try {
                return ((SimpleDateFormat)format.clone()).parse(header);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException(String.format("Not found appropriated date format for %s", header));
    }

    /**
     * Formats {@code date} in RFC 1123 format.
     *
     * @param date
     *         date
     * @return string in RFC 1123 format
     */
    public static String formatDate(Date date) {
        return ((DateFormat)DateFormats.formats[0].clone()).format(date);
    }

    //

    public static long getContentLengthLong(MultivaluedMap<String, String> httpHeaders) {
        String contentLengthHeader = httpHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
        return contentLengthHeader == null ? 0 : Long.parseLong(contentLengthHeader);
    }

    /**
     * Creates string representation of given object for adding to response header. Method uses {@link HeaderDelegate#toString()}.
     * If required implementation of HeaderDelegate is not accessible via {@link RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
     * then method {@code toString} of given object is used.
     *
     * @param o
     *         object
     * @return string representation of supplied type
     */
    @SuppressWarnings({"unchecked"})
    public static String getHeaderAsString(Object o) {
        HeaderDelegate headerDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(o.getClass());
        if (headerDelegate == null) {
            return o.toString();
        }
        return headerDelegate.toString(o);
    }

    /**
     * Convert Collection&lt;String&gt; to single String, where values separated by ','.
     *
     * @param collection
     *         the source list
     * @return String result
     */
    public static String convertToString(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        if (collection.isEmpty()) {
            return "";
        }

        return Joiner.on(',').join(collection);
    }

    /**
     * Appends string in given string builder. All quotes and whitespace are escaped.
     *
     * @param target
     *         string builder
     * @param appendMe
     *         string to append
     */
    static void appendWithQuote(StringBuilder target, String appendMe) {
        if (appendMe == null) {
            return;
        }
        Matcher matcher = WHITESPACE_QUOTE_PATTERN.matcher(appendMe);

        if (matcher.find()) {
            target.append('"');
            appendEscapeQuote(target, appendMe);
            target.append('"');
        } else {
            target.append(appendMe);
        }
    }

    /**
     * Appends string in given string builder. All quotes are escaped.
     *
     * @param target
     *         string builder
     * @param appendMe
     *         string to append
     */
    static void appendEscapeQuote(StringBuilder target, String appendMe) {
        for (int i = 0; i < appendMe.length(); i++) {
            char c = appendMe.charAt(i);
            if (c == '"') {
                target.append('\\');
            }
            target.append(c);
        }
    }

    /**
     * Removes all whitespace from given string.
     *
     * @param str
     *         the source string
     * @return the result string
     */
    static String removeWhitespaces(String str) {
        Matcher matcher = WHITESPACE_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.replaceAll("");
        }
        return str;
    }

    /**
     * Adds quotes to {@code String} if it consists whitespaces, otherwise {@code String} will be returned without changes.
     *
     * @param str
     *         the source string.
     * @return new string.
     */
    static String addQuotesIfHasWhitespace(String str) {
        Matcher matcher = WHITESPACE_PATTERN.matcher(str);
        if (matcher.find()) {
            return '"' + str + '"';
        }
        return str;
    }

    /**
     * Parses quality value. Quality value must have not more then 5 characters and must not be greater then 1 .
     *
     * @param qString
     *         string representation of quality value
     * @return quality value
     */
    static float parseQualityValue(String qString) {
        if (qString.length() > 5) {
            throw new IllegalArgumentException("Quality value string has more then 5 characters");
        }

        float qValue;
        try {
            qValue = Float.valueOf(qString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid quality value '%s'", qString));
        }
        if (qValue > 1.0F) {
            throw new IllegalArgumentException("Quality value can't be greater then 1.0");
        }

        return qValue;
    }

    /**
     * Checks that given string is token. Token contains only US-ASCII characters except separators, {@link #SEPARATORS} and controls.
     *
     * @param token
     *         the token
     * @return -1 if string has only valid characters otherwise index of first illegal character
     */
    static int isToken(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 127 || SEPARATORS[c]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The cookies parameters can be separated by ';' or ','. This method tries to find first available separator in cookie string.
     * If both are not found then string length is returned.
     *
     * @param cookie
     *         the cookie string.
     * @param start
     *         index for start searching.
     * @return the index of ',' or ';' or length of given cookie string.
     */
    private static int findCookieParameterSeparator(String cookie, int start) {
        int comma = scan(cookie, start, ',');
        int semicolon = scan(cookie, start, ';');
        return Math.min(comma, semicolon);
    }

    /**
     * Unescape '"' characters in string, e. g.
     * <p>
     * String \"hello \\\"someone\\\"\" will be changed to hello \"someone\"
     * </p>
     *
     * @param token
     *         token for processing
     * @return result
     */
    static String removeQuoteEscapes(String token) {
        StringBuilder sb = new StringBuilder();
        int length = token.length();

        for (int i = 0; i < length; i++) {
            if (charAtIs(token, i, '\\') && charAtIs(token, i + 1, '"')) {
                continue;
            }
            sb.append(token.charAt(i));
        }

        return sb.toString();
    }

    private static class AcceptTokenFactory implements ListItemFactory<AcceptToken> {
        @Override
        public AcceptToken createItem(String part) {
            return AcceptToken.valueOf(part);
        }
    }

    private static class AcceptMediaTypeFactory implements ListItemFactory<AcceptMediaType> {
        @Override
        public AcceptMediaType createItem(String part) {
            return AcceptMediaType.valueOf(part);
        }
    }

    private static class AcceptLanguageFactory implements ListItemFactory<AcceptLanguage> {
        @Override
        public AcceptLanguage createItem(String singleHeader) {
            return AcceptLanguage.valueOf(singleHeader);
        }
    }
}
