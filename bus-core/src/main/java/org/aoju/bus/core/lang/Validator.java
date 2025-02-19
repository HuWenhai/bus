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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.consts.RegEx;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字段验证器
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Validator {

    /**
     * 给定值是否为<code>null</code>
     *
     * @param value 值
     * @return 是否为<code>null</code>
     */
    public static boolean isNull(Object value) {
        return null == value;
    }

    /**
     * 给定值是否不为<code>null</code>
     *
     * @param value 值
     * @return 是否不为<code>null</code>
     */
    public static boolean isNotNull(Object value) {
        return null != value;
    }

    /**
     * 检查指定值是否为<code>null</code>
     *
     * @param <T>              被检查的对象类型
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws InstrumentException 检查不满足条件抛出的异常
     */
    public static <T> T validateNotNull(T value, String errorMsgTemplate, Object... params) throws InstrumentException {
        if (isNull(value)) {
            throw new InstrumentException(errorMsgTemplate, params);
        }
        return value;
    }

    /**
     * 验证是否为空
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     */
    public static boolean isEmpty(Object value) {
        return (null == value || (value instanceof String && StringUtils.isEmpty(value)));
    }

    /**
     * 验证是否为空
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     */
    public static boolean isNotEmpty(Object value) {
        return false == isEmpty(value);
    }

    /**
     * 验证是否为空,为空时抛出异常
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateNotEmpty(Object value, String errorMsg) throws InstrumentException {
        if (isEmpty(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否相等
     * 当两值都为null返回true
     *
     * @param t1 对象1
     * @param t2 对象2
     * @return 当两值都为null或相等返回true
     */
    public static boolean equal(Object t1, Object t2) {
        return ObjectUtils.equal(t1, t2);
    }

    /**
     * 验证是否相等,不相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws InstrumentException 验证异常
     */
    public static void validateEqual(Object t1, Object t2, String errorMsg) throws InstrumentException {
        if (false == equal(t1, t2)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否不等,相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws InstrumentException 验证异常
     */
    public static void validateNotEqual(Object t1, Object t2, String errorMsg) throws InstrumentException {
        if (equal(t1, t2)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否非空且与指定值相等
     * 当数据为空时抛出验证异常
     * 当两值不等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws InstrumentException 验证异常
     */
    public static void validateNotEmptyAndEqual(Object t1, Object t2, String errorMsg) throws InstrumentException {
        validateNotEmpty(t1, errorMsg);
        validateEqual(t1, t2, errorMsg);
    }

    /**
     * 验证是否非空且与指定值相等
     * 当数据为空时抛出验证异常
     * 当两值相等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws InstrumentException 验证异常
     */
    public static void validateNotEmptyAndNotEqual(Object t1, Object t2, String errorMsg) throws InstrumentException {
        validateNotEmpty(t1, errorMsg);
        validateNotEqual(t1, t2, errorMsg);
    }

    /**
     * 通过正则表达式验证
     *
     * @param regex 正则
     * @param value 值
     * @return 是否匹配正则
     */
    public static boolean isMactchRegex(String regex, String value) {
        return PatternUtils.isMatch(regex, value);
    }

    /**
     * 通过正则表达式验证
     * 不符合正则
     *
     * @param regex    正则
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateMatchRegex(String regex, String value, String errorMsg) throws InstrumentException {
        if (false == isMactchRegex(regex, value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 通过正则表达式验证
     *
     * @param pattern 正则模式
     * @param value   值
     * @return 是否匹配正则
     */
    public static boolean isMactchRegex(Pattern pattern, String value) {
        return PatternUtils.isMatch(pattern, value);
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param value 值
     * @return 是否为英文字母 、数字和下划线
     */
    public static boolean isGeneral(String value) {
        return isMactchRegex(RegEx.GENERAL, value);
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateGeneral(String value, String errorMsg) throws InstrumentException {
        if (false == isGeneral(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度,负数自动识别为0
     * @param max   最大长度,0或负数表示不限制最大长度
     * @return 是否为给定长度范围的英文字母 、数字和下划线
     */
    public static boolean isGeneral(String value, int min, int max) {
        String reg = "^\\w{" + min + "," + max + "}$";
        if (min < 0) {
            min = 0;
        }
        if (max <= 0) {
            reg = "^\\w{" + min + ",}$";
        }
        return isMactchRegex(reg, value);
    }

    /**
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param value    值
     * @param min      最小长度,负数自动识别为0
     * @param max      最大长度,0或负数表示不限制最大长度
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateGeneral(String value, int min, int max, String errorMsg) throws InstrumentException {
        if (false == isGeneral(value, min, max)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度,负数自动识别为0
     * @return 是否为给定最小长度的英文字母 、数字和下划线
     */
    public static boolean isGeneral(String value, int min) {
        return isGeneral(value, min, 0);
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param value    值
     * @param min      最小长度,负数自动识别为0
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateGeneral(String value, int min, String errorMsg) throws InstrumentException {
        validateGeneral(value, min, 0, errorMsg);
    }

    /**
     * 判断字符串是否全部为字母组成,包括大写和小写字母和汉字
     *
     * @param value 值
     * @return 是否全部为字母组成, 包括大写和小写字母和汉字
     * @since 3.3.0
     */
    public static boolean isLetter(String value) {
        return StringUtils.isAllCharMatch(value, new org.aoju.bus.core.lang.Matcher<Character>() {
            @Override
            public boolean match(Character t) {
                return Character.isLetter(t);
            }
        });
    }

    /**
     * 验证是否全部为字母组成,包括大写和小写字母和汉字
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     * @since 3.3.0
     */
    public static void validateLetter(String value, String errorMsg) throws InstrumentException {
        if (false == isLetter(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 判断字符串是否全部为大写字母
     *
     * @param value 值
     * @return 是否全部为大写字母
     * @since 3.3.0
     */
    public static boolean isUpperCase(String value) {
        return StringUtils.isAllCharMatch(value, new org.aoju.bus.core.lang.Matcher<Character>() {
            @Override
            public boolean match(Character t) {
                return Character.isUpperCase(t);
            }
        });
    }

    /**
     * 验证字符串是否全部为大写字母
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     * @since 3.3.0
     */
    public static void validateUpperCase(String value, String errorMsg) throws InstrumentException {
        if (false == isUpperCase(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 判断字符串是否全部为小写字母
     *
     * @param value 值
     * @return 是否全部为小写字母
     * @since 3.3.0
     */
    public static boolean isLowerCase(String value) {
        return StringUtils.isAllCharMatch(value, new org.aoju.bus.core.lang.Matcher<Character>() {
            @Override
            public boolean match(Character t) {
                return Character.isLowerCase(t);
            }
        });
    }

    /**
     * 验证字符串是否全部为小写字母
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     * @since 3.3.0
     */
    public static void validateLowerCase(String value, String errorMsg) throws InstrumentException {
        if (false == isLowerCase(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证该字符串是否是数字
     *
     * @param value 字符串内容
     * @return 是否是数字
     */
    public static boolean isNumber(String value) {
        return NumberUtils.isNumber(value);
    }

    /**
     * 验证是否为数字
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateNumber(String value, String errorMsg) throws InstrumentException {
        if (false == isNumber(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证该字符串是否是字母（包括大写和小写字母）
     *
     * @param value 字符串内容
     * @return 是否是字母（包括大写和小写字母）
     */
    public static boolean isWord(String value) {
        return isMactchRegex(RegEx.WORD, value);
    }

    /**
     * 验证是否为字母（包括大写和小写字母）
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateWord(String value, String errorMsg) throws InstrumentException {
        if (false == isWord(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为货币
     *
     * @param value 值
     * @return 是否为货币
     */
    public static boolean isMoney(String value) {
        return isMactchRegex(RegEx.MONEY, value);
    }

    /**
     * 验证是否为货币
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateMoney(String value, String errorMsg) throws InstrumentException {
        if (false == isMoney(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value 值
     * @return 是否为邮政编码（中国）
     */
    public static boolean isZipCode(String value) {
        return isMactchRegex(RegEx.ZIP_CODE, value);
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateZipCode(String value, String errorMsg) throws InstrumentException {
        if (false == isZipCode(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为可用邮箱地址
     *
     * @param value 值
     * @return 否为可用邮箱地址
     */
    public static boolean isEmail(String value) {
        return isMactchRegex(RegEx.EMAIL, value);
    }

    /**
     * 验证是否为可用邮箱地址
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateEmail(String value, String errorMsg) throws InstrumentException {
        if (false == isEmail(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public static boolean isPhone(String value) {
        return isMactchRegex(RegEx.PHONE, value);
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public static boolean isMobile(String value) {
        return isMactchRegex(RegEx.MOBILE, value);
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateMobile(String value, String errorMsg) throws InstrumentException {
        if (false == isMobile(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为身份证号码（18位中国）
     * 出生日期只支持到到2999年
     *
     * @param value 值
     * @return 是否为身份证号码（18位中国）
     */
    public static boolean isCitizenId(String value) {
        return isMactchRegex(RegEx.CITIZEN_ID, value);
    }

    /**
     * 验证是否为身份证号码（18位中国）
     * 出生日期只支持到到2999年
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateCitizenIdNumber(String value, String errorMsg) throws InstrumentException {
        if (false == isCitizenId(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为生日
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 是否为生日
     */
    public static boolean isBirthday(int year, int month, int day) {
        // 验证年
        int thisYear = DateUtils.thisYear();
        if (year < 1900 || year > thisYear) {
            return false;
        }

        // 验证月
        if (month < 1 || month > 12) {
            return false;
        }

        // 验证日
        if (day < 1 || day > 31) {
            return false;
        }
        if ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31) {
            return false;
        }
        if (month == 2) {
            return day <= 29 && (day != 29 || false != DateUtils.isLeapYear(year));
        }
        return true;
    }

    /**
     * 验证是否为生日
     * 只支持以下几种格式：
     * <ul>
     * <li>yyyyMMdd</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyyMMdd</li>
     * <li>yyyy年MM月dd日</li>
     * </ul>
     *
     * @param value 值
     * @return 是否为生日
     */
    public static boolean isBirthday(String value) {
        if (isMactchRegex(RegEx.BIRTHDAY, value)) {
            Matcher matcher = RegEx.BIRTHDAY.matcher(value);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(3));
                int day = Integer.parseInt(matcher.group(5));
                return isBirthday(year, month, day);
            }
        }
        return false;
    }

    /**
     * 验证验证是否为生日
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateBirthday(String value, String errorMsg) throws InstrumentException {
        if (false == isBirthday(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param value 值
     * @return 是否为IPV4地址
     */
    public static boolean isIpv4(String value) {
        return isMactchRegex(RegEx.IPV4, value);
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateIpv4(String value, String errorMsg) throws InstrumentException {
        if (false == isIpv4(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为MAC地址
     *
     * @param value 值
     * @return 是否为MAC地址
     */
    public static boolean isMac(String value) {
        return isMactchRegex(RegEx.MAC_ADDRESS, value);
    }

    /**
     * 验证是否为MAC地址
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateMac(String value, String errorMsg) throws InstrumentException {
        if (false == isMac(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param value 值
     * @return 是否为中国车牌号
     * @since 3.1.9
     */
    public static boolean isPlateNumber(String value) {
        return isMactchRegex(RegEx.PLATE_NUMBER, value);
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     * @since 3.1.9
     */
    public static void validatePlateNumber(String value, String errorMsg) throws InstrumentException {
        if (false == isPlateNumber(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为URL
     *
     * @param value 值
     * @return 是否为URL
     */
    public static boolean isUrl(String value) {
        try {
            new java.net.URL(value);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否为URL
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateUrl(String value, String errorMsg) throws InstrumentException {
        if (false == isUrl(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为英文
     *
     * @param value 值
     * @return 是否为汉字
     */
    public static boolean isEnglish(String value) {
        return isMactchRegex("^" + RegEx.WORD_PATTERN + "+$", value);
    }

    /**
     * 验证是否为汉字
     *
     * @param value 值
     * @return 是否为汉字
     */
    public static boolean isChinese(String value) {
        return isMactchRegex("^" + RegEx.CHINESE_PATTERN + "+$", value);
    }

    /**
     * 验证是否为汉字
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateChinese(String value, String errorMsg) throws InstrumentException {
        if (false == isChinese(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param value 值
     * @return 是否为中文字、英文字母、数字和下划线
     */
    public static boolean isGeneralWithChinese(String value) {
        return isMactchRegex(RegEx.GENERAL_WITH_CHINESE, value);
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateGeneralWithChinese(String value, String errorMsg) throws InstrumentException {
        if (false == isGeneralWithChinese(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为UUID
     * 包括带横线标准格式和不带横线的简单模式
     *
     * @param value 值
     * @return 是否为UUID
     */
    public static boolean isUUID(String value) {
        return isMactchRegex(RegEx.UUID, value) || isMactchRegex(RegEx.UUID_SIMPLE, value);
    }

    /**
     * 验证是否为UUID
     * 包括带横线标准格式和不带横线的简单模式
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateUUID(String value, String errorMsg) throws InstrumentException {
        if (false == isUUID(value)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为UUID
     * 包括带横线标准格式和不带横线的简单模式
     *
     * @param value 值
     * @return 是否为UUID
     */
    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 是否满足
     */
    public static boolean isBetween(Number value, Number min, Number max) {
        Assert.notNull(value);
        Assert.notNull(min);
        Assert.notNull(max);
        final double doubleValue = value.doubleValue();
        return (doubleValue >= min.doubleValue()) && (doubleValue <= max.doubleValue());
    }

    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value    值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param errorMsg 验证错误的信息
     * @throws InstrumentException 验证异常
     */
    public static void validateBetween(Number value, Number min, Number max, String errorMsg) throws InstrumentException {
        if (false == isBetween(value, min, max)) {
            throw new InstrumentException(errorMsg);
        }
    }

    /**
     * 验证是否为Hex（16进制）字符串
     *
     * @param value 值
     * @return 是否为Hex（16进制）字符串
     */
    public static boolean isHex(CharSequence value) {
        return isMactchRegex(RegEx.HEX, value.toString());
    }

}
