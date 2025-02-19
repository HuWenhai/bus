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

import org.aoju.bus.http.Internal;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Best-effort parser for HTTP dates.
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public final class HttpDate {
    /**
     * The last four-digit year: "Fri, 31 Dec 9999 23:59:59 GMT".
     */
    public static final long MAX_DATE = 253402300799999L;

    /**
     * Most websites serve cookies in the blessed format. Eagerly create the parser to ensure such
     * cookies are on the fast path.
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    // Date format specified by RFC 7231 section 7.1.1.1.
                    DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    rfc1123.setLenient(false);
                    rfc1123.setTimeZone(Internal.UTC);
                    return rfc1123;
                }
            };

    /**
     * If we fail to parse a date in a non-standard format, try each of these formats in sequence.
     */
    private static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[]{
            // HTTP formats required by RFC2616 but with any timezone.
            "EEE, dd MMM yyyy HH:mm:ss zzz", // RFC 822, updated by RFC 1123 with any TZ
            "EEEE, dd-MMM-yy HH:mm:ss zzz", // RFC 850, obsoleted by RFC 1036 with any TZ.
            "EEE MMM d HH:mm:ss yyyy", // ANSI C's asctime() format
            // Alternative formats.
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH-mm-ss z",
            "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z",
            "EEE dd MMM yyyy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z",
            "EEE dd MMM yy HH:mm:ss z",
            "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MM-yyyy HH:mm:ss z",

            /* RI bug 6641315 claims a cookie of this format was once served by www.yahoo.com */
            "EEE MMM d yyyy HH:mm:ss z",
    };

    private static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS =
            new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];

    private HttpDate() {
    }

    public static Date parse(String value) {
        if (value.length() == 0) {
            return null;
        }

        ParsePosition position = new ParsePosition(0);
        Date result = STANDARD_DATE_FORMAT.get().parse(value, position);
        if (position.getIndex() == value.length()) {
            // STANDARD_DATE_FORMAT must match exactly; all text must be consumed, e.g. no ignored
            // non-standard trailing "+01:00". Those cases are covered below.
            return result;
        }
        synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    // Set the timezone to use when interpreting formats that don't have a timezone. GMT is
                    // specified by RFC 7231.
                    format.setTimeZone(Internal.UTC);
                    BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                position.setIndex(0);
                result = format.parse(value, position);
                if (position.getIndex() != 0) {
                    // Something was parsed. It's possible the entire string was not consumed but we ignore
                    // that. If any of the BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS ended in "'GMT'" we'd have
                    // to also check that position.getIndex() == value.length() otherwise parsing might have
                    // terminated early, ignoring things like "+01:00". Leaving this as != 0 means that any
                    // trailing junk is ignored.
                    return result;
                }
            }
        }
        return null;
    }

    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

}
