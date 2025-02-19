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
package org.aoju.bus.core.date.format;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 日期格式化器缓存
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
abstract class FormatCache<F extends Format> {

    /**
     * No date or no time. Used in same parameters as DateFormat.SHORT or DateFormat.LONG
     */
    static final int NONE = -1;
    private static final ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache = new ConcurrentHashMap<>(7);
    private final ConcurrentMap<MultipartKey, F> cInstanceCache = new ConcurrentHashMap<>(7);

    /**
     * <p>
     * Gets a date/time format for the specified styles and locale.
     * </p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param locale    The non-null locale of the desired format
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from test criteria; do not make public or protected
    static String getPatternForStyle(final Integer dateStyle, final Integer timeStyle, final Locale locale) {
        final MultipartKey key = new MultipartKey(dateStyle, timeStyle, locale);

        String pattern = cDateTimeInstanceCache.get(key);
        if (pattern == null) {
            try {
                DateFormat formatter;
                if (dateStyle == null) {
                    formatter = DateFormat.getTimeInstance(timeStyle.intValue(), locale);
                } else if (timeStyle == null) {
                    formatter = DateFormat.getDateInstance(dateStyle.intValue(), locale);
                } else {
                    formatter = DateFormat.getDateTimeInstance(dateStyle.intValue(), timeStyle.intValue(), locale);
                }
                pattern = ((SimpleDateFormat) formatter).toPattern();
                final String previous = cDateTimeInstanceCache.putIfAbsent(key, pattern);
                if (previous != null) {
                    // even though it doesn't matter if another thread put the pattern
                    // it's still good practice to return the String instance that is
                    // actually in the ConcurrentMap
                    pattern = previous;
                }
            } catch (final ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    /**
     * 使用默认的pattern、timezone和locale获得缓存中的实例
     *
     * @return a date/time formatter
     */
    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * 使用 pattern, time zone and locale 获得对应的 格式化器
     *
     * @param pattern  非空日期格式,使用与 {@link SimpleDateFormat}相同格式
     * @param timeZone 时区,默认当前时区
     * @param locale   地区,默认使用当前地区
     * @return 格式化器
     * @throws IllegalArgumentException pattern 无效或<criteria>null</criteria>
     */
    public F getInstance(final String pattern, TimeZone timeZone, Locale locale) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final MultipartKey key = new MultipartKey(pattern, timeZone, locale);
        F format = cInstanceCache.get(key);
        if (format == null) {
            format = createInstance(pattern, timeZone, locale);
            final F previousValue = cInstanceCache.putIfAbsent(key, format);
            if (previousValue != null) {
                // another thread snuck in and did the same work
                // we should return the instance that is in ConcurrentMap
                format = previousValue;
            }
        }
        return format;
    }

    /**
     * 创建格式化器
     *
     * @param pattern  非空日期格式,使用与 {@link SimpleDateFormat}相同格式
     * @param timeZone 时区,默认当前时区
     * @param locale   地区,默认使用当前地区
     * @return 格式化器
     * @throws IllegalArgumentException pattern 无效或<criteria>null</criteria>
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * <p>
     * Gets a date/time formatter instance using the specified style, time zone and locale.
     * </p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // This must remain private, see LANG-884
    private F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    /**
     * <p>
     * Gets a date/time formatter instance using the specified style, time zone and locale.
     * </p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>
     * Gets a date formatter instance using the specified style, time zone and locale.
     * </p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateInstance(final int dateStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(Integer.valueOf(dateStyle), null, timeZone, locale);
    }

    /**
     * <p>
     * Gets a time formatter instance using the specified style, time zone and locale.
     * </p>
     *
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getTimeInstance(final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(null, Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>
     * Helper class to hold multi-part Map keys
     * </p>
     */
    private static class MultipartKey {
        private final Object[] keys;
        private int hashCode;

        /**
         * Constructs an instance of <criteria>MultipartKey</criteria> to hold the specified objects.
         *
         * @param keys the set of objects that make up the key. Each key may be null.
         */
        public MultipartKey(final Object... keys) {
            this.keys = keys;
        }


        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MultipartKey other = (MultipartKey) obj;
            return false != Arrays.equals(keys, other.keys);
        }


        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int rc = 0;
                for (final Object key : keys) {
                    if (key != null) {
                        rc = rc * 7 + key.hashCode();
                    }
                }
                hashCode = rc;
            }
            return hashCode;
        }
    }

}
