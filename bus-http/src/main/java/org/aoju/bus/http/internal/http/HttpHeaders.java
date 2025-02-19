/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http;

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.Url;
import org.aoju.bus.http.cookie.Cookie;
import org.aoju.bus.http.cookie.CookieJar;
import org.aoju.bus.http.header.Headers;
import org.aoju.bus.http.offers.Challenge;

import java.io.EOFException;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Headers and utilities for internal use by httpClient.
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public final class HttpHeaders {

    private static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
    private static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");

    private HttpHeaders() {
    }

    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    private static long stringToLong(String s) {
        if (s == null) return -1;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean varyMatches(
            Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Internal.equal(cachedRequest.values(field), newRequest.headers(field))) return false;
        }
        return true;
    }

    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains("*");
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            if (!"Vary".equalsIgnoreCase(responseHeaders.name(i))) continue;

            String value = responseHeaders.value(i);
            if (result.isEmpty()) {
                result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            }
            for (String varyField : value.split(",")) {
                result.add(varyField.trim());
            }
        }
        return result;
    }

    public static Headers varyHeaders(Response response) {
        Headers requestHeaders = response.networkResponse().request().headers();
        Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }

    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) return new Headers.Builder().build();

        Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = requestHeaders.size(); i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

    public static List<Challenge> parseChallenges(Headers responseHeaders, String headerName) {
        List<Challenge> result = new ArrayList<>();
        for (int h = 0; h < responseHeaders.size(); h++) {
            if (headerName.equalsIgnoreCase(responseHeaders.name(h))) {
                Buffer header = new Buffer().writeUtf8(responseHeaders.value(h));
                parseChallengeHeader(result, header);
            }
        }
        return result;
    }

    private static void parseChallengeHeader(List<Challenge> result, Buffer header) {
        String peek = null;

        while (true) {
            // Read a scheme name for this challenge if we don't have first already.
            if (peek == null) {
                skipWhitespaceAndCommas(header);
                peek = readToken(header);
                if (peek == null) return;
            }

            String schemeName = peek;

            // Read a token68, a sequence of parameters, or nothing.
            boolean commaPrefixed = skipWhitespaceAndCommas(header);
            peek = readToken(header);
            if (peek == null) {
                if (!header.exhausted()) return; // Expected a token; got something else.
                result.add(new Challenge(schemeName, Collections.<String, String>emptyMap()));
                return;
            }

            int eqCount = skipAll(header, (byte) '=');
            boolean commaSuffixed = skipWhitespaceAndCommas(header);

            // It's a token68 because there isn't a value after it.
            if (!commaPrefixed && (commaSuffixed || header.exhausted())) {
                result.add(new Challenge(schemeName, Collections.singletonMap(
                        (String) null, peek + repeat('=', eqCount))));
                peek = null;
                continue;
            }

            // It's a series of parameter names and values.
            Map<String, String> parameters = new LinkedHashMap<>();
            eqCount += skipAll(header, (byte) '=');
            while (true) {
                if (peek == null) {
                    peek = readToken(header);
                    if (skipWhitespaceAndCommas(header)) break; // We peeked a scheme name followed by ','.
                    eqCount = skipAll(header, (byte) '=');
                }
                if (eqCount == 0) break; // We peeked a scheme name.
                if (eqCount > 1) return; // Unexpected '=' characters.
                if (skipWhitespaceAndCommas(header)) return; // Unexpected ','.

                String parameterValue = !header.exhausted() && header.getByte(0) == '"'
                        ? readQuotedString(header)
                        : readToken(header);
                if (parameterValue == null) return; // Expected a value.
                String replaced = parameters.put(peek, parameterValue);
                peek = null;
                if (replaced != null) return; // Unexpected duplicate parameter.
                if (!skipWhitespaceAndCommas(header) && !header.exhausted()) return; // Expected ',' or EOF.
            }
            result.add(new Challenge(schemeName, parameters));
        }
    }

    private static boolean skipWhitespaceAndCommas(Buffer buffer) {
        boolean commaFound = false;
        while (!buffer.exhausted()) {
            byte b = buffer.getByte(0);
            if (b == ',') {
                buffer.readByte(); // Consume ','.
                commaFound = true;
            } else if (b == ' ' || b == '\t') {
                buffer.readByte(); // Consume space or tab.
            } else {
                break;
            }
        }
        return commaFound;
    }

    private static int skipAll(Buffer buffer, byte b) {
        int count = 0;
        while (!buffer.exhausted() && buffer.getByte(0) == b) {
            count++;
            buffer.readByte();
        }
        return count;
    }

    private static String readQuotedString(Buffer buffer) {
        if (buffer.readByte() != '\"') throw new IllegalArgumentException();
        Buffer result = new Buffer();
        while (true) {
            long i = buffer.indexOfElement(QUOTED_STRING_DELIMITERS);
            if (i == -1L) return null; // Unterminated quoted string.

            if (buffer.getByte(i) == '"') {
                result.write(buffer, i);
                buffer.readByte(); // Consume '"'.
                return result.readUtf8();
            }

            if (buffer.size() == i + 1L) return null; // Dangling escape.
            result.write(buffer, i);
            buffer.readByte(); // Consume '\'.
            result.write(buffer, 1L); // The escaped character.
        }
    }

    private static String readToken(Buffer buffer) {
        try {
            long tokenSize = buffer.indexOfElement(TOKEN_DELIMITERS);
            if (tokenSize == -1L) tokenSize = buffer.size();

            return tokenSize != 0L
                    ? buffer.readUtf8(tokenSize)
                    : null;
        } catch (EOFException e) {
            throw new AssertionError();
        }
    }

    private static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    public static void receiveHeaders(CookieJar cookieJar, Url url, Headers headers) {
        if (cookieJar == CookieJar.NO_COOKIES) return;

        List<Cookie> cookies = Cookie.parseAll(url, headers);
        if (cookies.isEmpty()) return;

        cookieJar.saveFromResponse(url, cookies);
    }

    public static boolean hasBody(Response response) {
        // HEAD requests never yield a body regardless of the response headers.
        if (response.request().method().equals("HEAD")) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < StatusLine.HTTP_CONTINUE || responseCode >= 200)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        if (contentLength(response) != -1
                || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return true;
        }

        return false;
    }

    public static int skipUntil(String input, int pos, String characters) {
        for (; pos < input.length(); pos++) {
            if (characters.indexOf(input.charAt(pos)) != -1) {
                break;
            }
        }
        return pos;
    }

    public static int skipWhitespace(String input, int pos) {
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            if (c != ' ' && c != '\t') {
                break;
            }
        }
        return pos;
    }

    public static int parseSeconds(String value, int defaultValue) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else if (seconds < 0) {
                return 0;
            } else {
                return (int) seconds;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
