/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class HeaderHelperTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void createsAcceptedCharsetListWhenHeaderIsNull() {
        List<AcceptToken> charsetList = HeaderHelper.createAcceptedCharsetList(null);
        assertEquals(newArrayList(new AcceptToken("*")), charsetList);
    }

    @Test
    public void createAcceptedCharsetListWhenHeaderIsEmptyString() {
        List<AcceptToken> charsetList = HeaderHelper.createAcceptedCharsetList("");
        assertEquals(newArrayList(new AcceptToken("*")), charsetList);
    }

    @Test
    public void createsAcceptedCharsetListFromHeader() {
        String charsets = "Windows-1251,utf-8; q   =0.9,*;q=0.7";
        List<AcceptToken> expectedCharsetList =
                newArrayList(new AcceptToken("windows-1251"), new AcceptToken("utf-8", 0.9f), new AcceptToken("*", 0.7f));

        List<AcceptToken> charsetList = HeaderHelper.createAcceptedCharsetList(charsets);

        assertEquals(expectedCharsetList, charsetList);
    }

    @Test
    public void createsAcceptedEncodingListWhenHeaderIsNull() {
        List<AcceptToken> charsetList = HeaderHelper.createAcceptedEncodingList(null);
        assertEquals(newArrayList(new AcceptToken("*")), charsetList);
    }

    @Test
    public void createsAcceptedEncodingListWhenHeaderIsEmptyString() {
        List<AcceptToken> charsetList = HeaderHelper.createAcceptedEncodingList("");
        assertEquals(newArrayList(new AcceptToken("*")), charsetList);
    }

    @Test
    public void createsAcceptedEncodingListFromHeader() {
        String encodings = "compress;q=0.5, gzip ;  q=1.0";
        List<AcceptToken> expectedEncodingList = newArrayList(new AcceptToken("gzip"), new AcceptToken("compress", 0.5f));

        List<AcceptToken> encodingList = HeaderHelper.createAcceptedEncodingList(encodings);

        assertEquals(expectedEncodingList, encodingList);
    }

    @Test
    public void createsAcceptedLanguageListWhenHeaderIsNull() throws Exception {
        List<AcceptLanguage> acceptedLanguageList = HeaderHelper.createAcceptedLanguageList(null);
        assertEquals(newArrayList(new AcceptLanguage(new Locale("*"))), acceptedLanguageList);
    }

    @Test
    public void createsAcceptedLanguageListWhenHeaderIsEmptyString() throws Exception {
        List<AcceptLanguage> acceptedLanguageList = HeaderHelper.createAcceptedLanguageList("");
        assertEquals(newArrayList(new AcceptLanguage(new Locale("*"))), acceptedLanguageList);
    }

    @Test
    public void createsAcceptedLanguageListFromHeader() throws Exception {
        String languages = "da;q=0.825,   en-GB,  en;q=0.8";
        List<AcceptLanguage> expectedAcceptedLanguageList =
                newArrayList(new AcceptLanguage(new Locale("en", "GB")), new AcceptLanguage(new Locale("da"), 0.825f), new AcceptLanguage(new Locale("en"), 0.8f));
        List<AcceptLanguage> acceptedLanguageList = HeaderHelper.createAcceptedLanguageList(languages);
        assertEquals(expectedAcceptedLanguageList, acceptedLanguageList);
    }

    @Test
    public void createsAcceptMediaTypeListWhenHeaderIsNull() {
            List<AcceptMediaType> acceptMediaTypeList = HeaderHelper.createAcceptMediaTypeList(null);
            List<AcceptMediaType> expectedAcceptMediaTypeList = newArrayList(new AcceptMediaType("*", "*"));

            assertEquals(expectedAcceptMediaTypeList, acceptMediaTypeList);
    }

    @Test
    public void createsAcceptMediaTypeListWhenHeaderIsEmptyString() {
        List<AcceptMediaType> acceptMediaTypeList = HeaderHelper.createAcceptMediaTypeList("");
        List<AcceptMediaType> expectedAcceptMediaTypeList = newArrayList(new AcceptMediaType("*", "*"));

        assertEquals(expectedAcceptMediaTypeList, acceptMediaTypeList);
    }

    @Test
    public void createsAcceptMediaTypeListFromHeader() {
        String header = "text/xml;  charset=utf8;q=0.825,    text/html;charset=utf8,  text/plain;charset=utf8;q=0.8";
        List<AcceptMediaType> acceptMediaTypeList = HeaderHelper.createAcceptMediaTypeList(header);
        List<AcceptMediaType> expectedAcceptMediaTypeList = newArrayList(
                new AcceptMediaType("text", "html", ImmutableMap.of("charset", "utf8")),
                new AcceptMediaType("text", "xml", ImmutableMap.of("charset", "utf8", "q", "0.825")),
                new AcceptMediaType("text", "plain", ImmutableMap.of("charset", "utf8", "q", "0.8")));
        assertEquals(expectedAcceptMediaTypeList, acceptMediaTypeList);
    }

    @DataProvider
    public static Object[][] forParsesCookies() {
        return new Object[][] {
                {"name=andrew",             newArrayList(new Cookie("name", "andrew"))},

                {"company=exo,name=andrew", newArrayList(new Cookie("company", "exo"), new Cookie("name", "andrew"))},

                {"company=exo;name=andrew", newArrayList(new Cookie("company", "exo"), new Cookie("name", "andrew"))},

                {"$Version=1;company=exo;$Path=/exo,$domain=exo.com;name=andrew",
                 newArrayList(new Cookie("company", "exo", "/exo", "exo.com"), new Cookie("name", "andrew"))},

                {"$Version=0;  company=exo;  $path=/exo, $Domain=exo.com;name=andrew,  $Domain=exo.org",
                 newArrayList(new Cookie("company", "exo", "/exo", "exo.com", 0), new Cookie("name", "andrew", null, "exo.org", 0))}
        };
    }

    @UseDataProvider("forParsesCookies")
    @Test
    public void parsesCookies(String cookiesHeader, List<Cookie> expectedCookies) {
        List<Cookie> cookies = HeaderHelper.parseCookies(cookiesHeader);
        assertEquals(expectedCookies, cookies);
    }

    @DataProvider
    public static Object[][] forParsesNewCookies() {
        return new Object[][] {
                {"name=andrew", new NewCookie("name", "andrew")},

                {"name=andrew;version=1;paTh=/path;Domain=codenvy.com;comment=\"comment\";max-age=300;expires=Thu, 29 Dec 2011 12:03:50 GMT;HttpOnly;secure",
                 new NewCookie("name", "andrew", "/path", "codenvy.com", 1, "comment", 300, date(2011, 12, 29, 12, 3, 50, "GMT"), true, true)},
        };
    }

    @UseDataProvider("forParsesNewCookies")
    @Test
    public void parsesNewCookies(String cookieHeader, NewCookie expectedNewCookie) {
        NewCookie newCookie = HeaderHelper.parseNewCookie(cookieHeader);

        assertNotNull(newCookie);
        assertEquals(expectedNewCookie.getName(), newCookie.getName());
        assertEquals(expectedNewCookie.getValue(), newCookie.getValue());
        if (expectedNewCookie.getExpiry() != null) {
            assertTrue(String.format("Expiry dates are not equal. Expected %s, actual %s", expectedNewCookie.getExpiry(), newCookie.getExpiry()),
                       Math.abs(expectedNewCookie.getExpiry().getTime() - newCookie.getExpiry().getTime()) < 1000);
        }
        assertEquals(expectedNewCookie.getMaxAge(), newCookie.getMaxAge());
        assertEquals(expectedNewCookie.getPath(), newCookie.getPath());
        assertEquals(expectedNewCookie.getDomain(), newCookie.getDomain());
        assertEquals(expectedNewCookie.getComment(), newCookie.getComment());
        assertEquals(expectedNewCookie.getVersion(), newCookie.getVersion());
        assertEquals(expectedNewCookie.isSecure(), newCookie.isSecure());
        assertEquals(expectedNewCookie.isHttpOnly(), newCookie.isHttpOnly());
    }

    @Test
    public void parsesANSI() {
        // EEE MMM d HH:mm:ss yyyy
        String dateHeader = "THU DEC 29 12:03:50 2011";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "GMT");

        Date date =  HeaderHelper.parseDateHeader(dateHeader);
        assertTrue(String.format("Dates are not equal. Expected %s, actual %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    @Test
    public void parsesRFC_1036() {
        // EEEE, dd-MMM-yy HH:mm:ss zzz
        String dateHeader = "Thursday, 29-Dec-11 12:03:50 EST";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "EST");

        Date date = HeaderHelper.parseDateHeader(dateHeader);

        assertTrue(String.format("Dates are not equal, %s and %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    @Test
    public void parsesRFC_1123() {
        // EEE, dd MMM yyyy HH:mm:ss zzz
        String dateHeader = "Thu, 29 Dec 2011 12:03:50 GMT";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "GMT");

        Date date = HeaderHelper.parseDateHeader(dateHeader);

        assertTrue(String.format("Dates are not equal, %s and %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    private static Date date(int year, int month, int day, int hours, int minutes, int seconds, String timeZone) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);

        return calendar.getTime();
    }

    @Test
    public void failsParseDateIfItDoesNotMatchToAnySupportedFormat() {
        String dateHeader = "12:03:50 GMT";
        thrown.expect(IllegalArgumentException.class);
        HeaderHelper.parseDateHeader(dateHeader);
    }

    @Test
    public void formatsDate() throws Exception {
        final Date date = date(2010, 1, 8, 2, 5, 0, "EET");
        assertEquals("Fri, 08 Jan 2010 00:05:00 GMT", HeaderHelper.formatDate(date));
    }

    @Test
    public void getsContentLengthAsLong() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle(HttpHeaders.CONTENT_LENGTH, "111");

        final long contentLength = HeaderHelper.getContentLengthLong(headers);
        assertEquals(111L, contentLength);
    }

    @DataProvider
    public static Object[][] forConvertsCollectionToString() {
        return new Object[][]{
                {newArrayList("a", "b", "c"), "a,b,c"},
                {newArrayList(), ""},
                {null, null}
        };
    }

    @UseDataProvider("forConvertsCollectionToString")
    @Test
    public void convertsCollectionToString(Collection<String> header, String expectedResult) throws Exception {
        final String result = HeaderHelper.convertToString(header);
        assertEquals(expectedResult, result);
    }

    @DataProvider
    public static Object[][] forAppendsStringInStringBuilderEscapesAndAddsQuoteIfNeed() {
        return new Object[][] {
                {"DoNotNeedAddQuotes",   "DoNotNeedAddQuotes"},
                {"Need\tAdd Quotes",     "\"Need\tAdd Quotes\""},
                {"Need Add \"Quotes\"",  "\"Need Add \\\"Quotes\\\"\""},
                {"NeedAdd\"Quotes\"",    "\"NeedAdd\\\"Quotes\\\"\""},
                {null, ""}
        };
    }

    @UseDataProvider("forAppendsStringInStringBuilderEscapesAndAddsQuoteIfNeed")
    @Test
    public void appendsStringInStringBuilderEscapesAndAddsQuoteIfNeed(String string, String expectedResult) throws Exception {
        StringBuilder builder = new StringBuilder();
        HeaderHelper.appendWithQuote(builder, string);
        assertEquals(expectedResult, builder.toString());
    }

    @DataProvider
    public static Object[][] forRemovesWhitespaces() {
        return new Object[][]{
                {"remove_white spaces",     "remove_whitespaces"},
                {"rem ove_white\t  spaces", "remove_whitespaces"},
                {"leave_as_is",             "leave_as_is"}
        };
    }

    @UseDataProvider("forRemovesWhitespaces")
    @Test
    public void removesWhitespaces(String string, String expectedResult) throws Exception {
        assertEquals(expectedResult, HeaderHelper.removeWhitespaces(string));
    }

    @DataProvider
    public static Object[][] forAddsQuotesIfHasWhitespace() {
        return new Object[][] {
                {"DoNotNeedAddQuotes",   "DoNotNeedAddQuotes"},
                {"Need\tAddQuotes",     "\"Need\tAddQuotes\""},
                {"Need Add Quotes",     "\"Need Add Quotes\""}
        };
    }

    @UseDataProvider("forAddsQuotesIfHasWhitespace")
    @Test
    public void addsQuotesIfHasWhitespace(String string, String expectedResult) throws Exception {
        assertEquals(expectedResult, HeaderHelper.addQuotesIfHasWhitespace(string));
    }

    @DataProvider
    public static Object[][] forParsesQualityValue() {
        return new Object[][]{
                {"0.75", 0.75f},
                {".3",   0.3f},
                {"1.0",  1.0f},
                {"0.0",  0.0f}
        };
    }

    @UseDataProvider("forParsesQualityValue")
    @Test
    public void parsesQualityValue(String qValueString, float expectedResult) throws Exception {
        assertEquals(expectedResult, HeaderHelper.parseQualityValue(qValueString), 0.0f);
    }

    @Test
    public void failsParseQualityValueWhenStringIsLongerThanFiveCharacters() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Quality value string has more then 5 characters");
        HeaderHelper.parseQualityValue("0.7777");
    }

    @Test
    public void failsParseQualityValueWhenValueIsGreaterThanOne() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Quality value can't be greater then 1.0");
        HeaderHelper.parseQualityValue("1.05");
    }

    @Test
    public void failsParseQualityValueWhenValueIsNotNumeric() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid quality value '0.5x'");
        HeaderHelper.parseQualityValue("0.5x");
    }

    @DataProvider
    public static Object[][] forChecksIsStringToken() {
        return new Object[][] {
                {"", -1},
                {"token", -1},
                {"is,not token", 2},
                {"is not:token", 6}
        };
    }

    @UseDataProvider("forChecksIsStringToken")
    @Test
    public void checksIsStringToken(String checkIsToken, int indexOfFirstInvalidChar) throws Exception {
        assertEquals(indexOfFirstInvalidChar, HeaderHelper.isToken(checkIsToken));
    }

    @DataProvider
    public static Object[][] forRemovesQuoteEscapes() {
        return new Object[][]{
                {"\\\"token\\\"",       "\"token\""},
                {"other \\\"token\\\"", "other \"token\""},
                {"token",               "token"}
        };
    }

    @UseDataProvider("forRemovesQuoteEscapes")
    @Test
    public void removesQuoteEscapes(String string, String expectedResult) throws Exception {
        assertEquals(expectedResult, HeaderHelper.removeQuoteEscapes(string));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void transformsObjectToStringWithHeaderDelegate() throws Exception {
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(runtimeDelegate);

        HeaderDelegate<String> headerDelegate = mock(HeaderDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(String.class)).thenReturn(headerDelegate);
        when(headerDelegate.toString("foo")).thenReturn("<foo>");

        assertEquals("<foo>", HeaderHelper.getHeaderAsString("foo"));
    }

    @Test
    public void transformsObjectToStringWithToStringMethodWhenHeaderDelegateDoesNotPresent() throws Exception {
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(runtimeDelegate);

        Object someObject = mock(Object.class);
        when(someObject.toString()).thenReturn("<foo>");

        assertEquals("<foo>", HeaderHelper.getHeaderAsString(someObject));
    }
}