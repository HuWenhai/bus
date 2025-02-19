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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.text.StrBuilder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 字符串处理类
 * 用于MD5,加解密和字符串编码转换
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class StringUtils extends TextUtils {

    public static final int INDEX_NOT_FOUND = -1;
    private static final int PAD_LIMIT = 8192;

    /**
     * 分别去空格
     *
     * @param array 数组
     * @return 数组
     */
    public static final String[] trim(String[] array) {
        if (ArrayUtils.isEmpty(array)) {
            return array;
        }
        String[] resultArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String param = array[i];
            resultArray[i] = trim(param);
        }
        return resultArray;
    }

    /**
     * 字符串去空格
     *
     * @param str 原始字符串
     * @return 返回字符串
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 重写toString方法,处理了空指针问题
     * (默认如果对象为null则替换成"")
     *
     * @param obj String类型的Object对象
     * @return 转换后的字符串
     */
    public static String toString(Object obj) {
        return toString(obj, "");
    }

    /**
     * 重写toString方法,处理了空指针问题
     *
     * @param obj          String类型的Object对象
     * @param defaultValue 如果obj是null,则以defaultValue的值返回
     * @return 转换后的字符串
     */
    public static String toString(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        return obj.toString();
    }

    /**
     * 校验对象是不是为null或者内容是""
     *
     * @param obj 字符串
     * @return true/false
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || obj.toString().equals("");
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 如果对象是字符串是否为空串空的定义如下:
     * 1、为null
     * 2、为""
     *
     * @param obj 对象
     * @return true/false
     * @since 3.3.0
     */
    public static boolean isEmptyIfStr(Object obj) {
        if (null == obj) {
            return true;
        } else if (obj instanceof CharSequence) {
            return 0 == ((CharSequence) obj).length();
        }
        return false;
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values != null && values.length != 0) {
            String[] var2 = values;
            int var3 = values.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String value = var2[var4];
                result &= !isEmpty(value);
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * 将base64字符串处理成String字节
     *
     * @param str base64的字符串
     * @return 原字节数据
     */
    public static byte[] base64ToByte(String str) {
        try {
            if (str == null) {
                return null;
            }
            return Base64.getDecoder().decode(str);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param str base64的字符串
     * @return 可显示的字符串
     */
    public static String base64ToString(String str) {
        try {
            if (str == null) {
                return null;
            }
            return new String(base64ToByte(str), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param str     base64的字符串
     * @param charset 编码格式(UTF-8/GBK)
     * @return 可显示的字符串
     */
    public static String base64ToString(String str, String charset) {
        try {
            if (str == null) {
                return null;
            }
            return new String(base64ToByte(str), charset);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将字节数据处理成base64字符串
     *
     * @param bts 字节数据
     * @return base64编码后的字符串(用于传输)
     */
    public static String toBase64(byte[] bts) {
        if (bts == null || bts.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bts);
    }

    /**
     * 将String处理成base64字符串
     * (用默认的String编码集)
     *
     * @param oldStr 原字符串
     * @return base64编码后的字符串(用于传输)
     */
    public static String toBase64(String oldStr) {
        if (oldStr == null) {
            return null;
        }
        byte[] bts = oldStr.getBytes();
        return Base64.getEncoder().encodeToString(bts);
    }

    /**
     * 将String处理成base64字符串
     * (用默认的String编码集)
     *
     * @param oldStr  原字符串
     * @param charset 字符编码
     * @return base64编码后的字符串(用于传输)
     */
    public static String toBase64(String oldStr, String charset) {
        try {
            if (oldStr == null) {
                return null;
            }
            byte[] bts = oldStr.getBytes(charset);
            return Base64.getEncoder().encodeToString(bts);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将字节数组换成16进制的字符串
     *
     * @param byteArray 字节数组
     * @return string
     */
    public static String byteArrayToHex(byte[] byteArray) {
        // 首先初始化一个字符数组,用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        // new一个字符数组,这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制,也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组,通过位运算（位运算效率高）,转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    /**
     * bytes字符串转换为Byte值
     * src Byte字符串,每个Byte之间没有分隔符
     *
     * @param hex 字符串
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        if (isEmpty(hex)) {
            return new byte[0];
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            String subStr = hex.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /**
     * 将byte转换为MD5
     *
     * @param data byte[]
     * @return 字符串
     */
    public static String toMD5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            return byteArrayToHex(digest.digest());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将字符串转换为MD5
     *
     * @param data String
     * @return 字符串
     */
    public static String toMD5(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data.getBytes());
            return byteArrayToHex(digest.digest());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将字符串转换为MD5
     *
     * @param data    String
     * @param charset 字符集
     * @return 字符串
     */
    public static String toMD5(String data, String charset) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data.getBytes(charset));
            return byteArrayToHex(digest.digest());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转换为全角
     *
     * @param input 需要转换的字符串
     * @return 全角字符串.
     */
    public static String toFullString(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            // 空格单独处理, 其余的偏移量为65248
            if (c[i] == ' ') {
                c[i] = '\u3000'; // 中文空格
            } else if (c[i] < 128) {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 转换为半角
     *
     * @param input 需要转换的字符串
     * @return 半角字符串
     */
    public static String toHalfString(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            // 是否是中文空格, 单独处理
            if (c[i] == '\u3000') {
                c[i] = ' ';
            }
            // 校验是否字符值是否在此数值之间
            else if (c[i] > 65248 && c[i] < (128 + 65248)) {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 将字符串转换为unicode编码
     *
     * @param input 要转换的字符串(主要是包含中文的字符串)
     * @return 转换后的unicode编码
     */
    public static String toUnicode(String input) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            // 取出每一个字符
            char c = input.charAt(i);
            String hexStr = Integer.toHexString(c);
            while (hexStr.length() < 4) {
                hexStr = "0" + hexStr;
            }
            // 转换为unicode
            unicode.append("\\u" + hexStr);
        }
        return unicode.toString();
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param str         被编码的字符串
     * @param isSkipAscii 是否跳过ASCII字符（只跳过可见字符）
     * @return Unicode字符串
     */
    public static String toUnicode(String str, boolean isSkipAscii) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        final int len = str.length();
        final TextUtils unicode = create(str.length() * 6);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (isSkipAscii && CharUtils.isAsciiPrintable(c)) {
                unicode.append(c);
            } else {
                unicode.append(HexUtils.toUnicodeHex(c));
            }
        }
        return unicode.toString();
    }

    /**
     * 将unicode编码还原为字符串
     *
     * @param input unicode编码的字符串
     * @return 原始字符串
     */
    public static String unicodeToString(String input) {
        StringBuilder string = new StringBuilder();
        String[] hex = input.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }


    /**
     * Unicode字符串转为普通字符串
     * Unicode字符串的表现方式为：\\uXXXX
     *
     * @param unicode     Unicode字符串
     * @param isSkipAscii 是跳过Ascii
     * @return 普通字符串
     */
    public static String unicodeToString(String unicode, boolean isSkipAscii) {
        if (StringUtils.isBlank(unicode)) {
            return unicode;
        }

        if (isSkipAscii) {
            final int len = unicode.length();
            final TextUtils sb = create(len);
            int i = -1;
            int pos = 0;
            while ((i = indexOfIgnoreCase(unicode, "\\u", pos)) != -1) {
                sb.append(unicode, pos, i);//写入Unicode符之前的部分
                pos = i;
                if (i + 5 < len) {
                    char c = 0;
                    try {
                        c = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
                        sb.append(c);
                        pos = i + 6;//跳过整个Unicode符
                    } catch (NumberFormatException e) {
                        //非法Unicode符,跳过
                        sb.append(unicode, pos, i + 2);//写入"\\u"
                        pos = i + 2;
                    }
                } else {
                    pos = i;//非Unicode符,结束
                    break;
                }
            }

            if (pos < len) {
                sb.append(unicode, pos, len);
            }
            return sb.toString();
        }
        return unicodeToString(unicode);
    }

    /**
     * 将字符串转换为url参数形式(中文和特殊字符会以%xx表示)
     *
     * @param input 字符串
     * @return 字符串
     */
    public static String toUrlStr(String input) {
        return toUrlStr(input, "UTF-8");
    }

    /**
     * 将字符串转换为url参数形式(中文和特殊字符会以%xx表示)
     *
     * @param input   字符串
     * @param charset 编码
     * @return 字符串
     */
    public static String toUrlStr(String input, String charset) {
        try {
            return URLEncoder.encode(input, charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将url参数形式的字符串转换为原始字符串(中文和特殊字符会以%xx表示)
     *
     * @param input 字符串
     * @return 字符串
     */
    public static String urlStrToString(String input) {
        return urlStrToString(input, "UTF-8");
    }

    /**
     * 将url参数形式的字符串转换为原始字符串(中文和特殊字符会以%xx表示)
     *
     * @param input   字符串
     * @param charset 编码
     * @return 字符串
     */
    public static String urlStrToString(String input, String charset) {
        try {
            return URLDecoder.decode(input, charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DES加密
     *
     * @param text    要加密的数据
     * @param token   约定密串
     * @param charset 原文的编码集
     * @return 加密后的密文
     */
    public static String toDES(String text, String token, String charset) {
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            byte[] btToken = charset == null ? token.getBytes() : token.getBytes(charset);
            byte[] btText = charset == null ? text.getBytes() : text.getBytes(charset);
            DESKeySpec desKeySpec = new DESKeySpec(btToken);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(btToken);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bts = cipher.doFinal(btText);
            return toBase64(bts);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * DES加密
     *
     * @param text  要加密的数据
     * @param token 约定密串
     * @return 加密后的密文
     */
    public static String toDES(String text, String token) {
        return toDES(text, token, null);
    }

    /**
     * DES解密
     *
     * @param text    要加密的数据
     * @param token   约定密串
     * @param charset 原文的编码集
     * @return 原文字符串
     */
    public static String DESToString(String text, String token, String charset) {
        try {
            byte[] bytesrc = base64ToByte(text);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            byte[] btToken = charset == null ? token.getBytes() : token.getBytes(charset);
            DESKeySpec desKeySpec = new DESKeySpec(btToken);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(btToken);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] retByte = cipher.doFinal(bytesrc);
            return charset == null ? new String(retByte) : new String(retByte, charset);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * DES解密
     *
     * @param text  要加密的数据
     * @param token 约定密串
     * @return 原文字符串
     */
    public static String DESToString(String text, String token) {
        return DESToString(text, token, null);
    }

    /**
     * AES加密
     *
     * @param text    要加密的数据
     * @param token   约定密串
     * @param charset 原数据字符集
     * @return 加密后的密文
     */
    public static String toAES(String text, String token, String charset) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(charset == null ? token.getBytes() : token.getBytes(charset)));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] btText = charset == null ? text.getBytes() : text.getBytes(charset);
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] bts = cipher.doFinal(btText);
            return toBase64(bts);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * AES加密
     *
     * @param text  要加密的数据
     * @param token 约定密串
     * @return 加密后的密文
     */
    public static String toAES(String text, String token) {
        return toAES(text, token, null);
    }

    /**
     * AES解密
     *
     * @param text    要加密的数据
     * @param token   约定密串
     * @param charset 原数据字符集
     * @return 解密后的原文
     */
    public static String AESToString(String text, String token, String charset) {
        try {
            byte[] btText = base64ToByte(text);
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(charset == null ? token.getBytes() : token.getBytes(charset)));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] bts = cipher.doFinal(btText);
            return charset == null ? new String(bts) : new String(bts, charset);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * AES解密
     *
     * @param text  要加密的数据
     * @param token 约定密串
     * @return 解密后的原文
     */
    public static String AESToString(String text, String token) {
        return AESToString(text, token, null);
    }

    /**
     * 自定义二级压缩(最终显示为base64后的串)
     *
     * @param bts 原始字符串
     * @return 压缩后的字符串
     */
    public static String compress2(byte[] bts) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte now = 0;
            int count = 0;
            for (int i = 0; i < bts.length; i++) {
                if (count == 0) {
                    now = bts[i];
                    count++;
                    continue;
                }
                if (now == bts[i] && count < 0xFF) {
                    count++;
                } else {
                    bout.write(count);
                    bout.write(now);
                    count = 0;
                    // 重新本次循环
                    i--;
                }
            }
            // 补全最后一次
            bout.write(count);
            bout.write(now);
            count = 0;
            bout.flush();

            // 第二次处理
            bts = bout.toByteArray();
            ByteArrayOutputStream head = new ByteArrayOutputStream();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            for (int i = 0; i < bts.length; i += 2) {
                if (count == 0) {
                    now = bts[i];
                    count = 1;
                    body.write(bts[i + 1]);
                    continue;
                }
                if (now == bts[i] && count < 0xFF) {
                    count++;
                    body.write(bts[i + 1]);
                } else {
                    head.write(count);
                    head.write(now);
                    count = 0;
                    // 重新本次循环
                    i -= 2;
                }
            }
            // 补全最后一次
            head.write(count);
            head.write(now);
            head.flush();
            body.flush();
            // 计算压缩头大小
            int size = head.size();
            // 合并
            ByteArrayOutputStream all = new ByteArrayOutputStream();
            all.write(size >> 24);
            all.write(size >> 16);
            all.write(size >> 8);
            all.write(size);
            all.write(head.toByteArray());
            all.write(body.toByteArray());
            all.flush();
            return StringUtils.toBase64(all.toByteArray());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 自定义二级压缩(最终显示为base64后的串)
     *
     * @param val     原始字符串
     * @param charset 字符编码
     * @return 压缩后的字符串
     */
    public static String compress2(String val, String... charset) {
        if (StringUtils.isEmpty(val)) {
            return val;
        }
        try {
            return compress2(
                    charset != null && charset.length > 0 ? val.getBytes(charset[0]) : val.getBytes());
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 自定义二级解压缩
     *
     * @param val     base64后的压缩数据
     * @param charset 字符编码
     * @return 解压后的原数据
     */
    public static String unCompress2(String val, String... charset) {
        try {
            byte[] bts = StringUtils.base64ToByte(val);
            int size = (bts[0] << 24) + (bts[1] << 16) + (bts[2] << 8) + bts[3];
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            // 首次解压
            int count = 4 + size;
            for (int i = 4; i < 4 + size; i += 2) {
                int k = bts[i];
                byte bt = bts[i + 1];
                for (int j = 0; j < k; j++) {
                    body.write(bt);
                    body.write(bts[count]);
                    count++;
                }
            }
            body.flush();
            bts = body.toByteArray();
            // 二次解压
            ByteArrayOutputStream all = new ByteArrayOutputStream();
            for (int i = 0; i < bts.length; i += 2) {
                for (int j = 0; j < bts[i]; j++) {
                    all.write(bts[i + 1]);
                }
            }
            all.flush();
            bts = all.toByteArray();
            return charset != null && charset.length > 0 ? new String(bts, charset[0]) : new String(bts);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 系统压缩(返回base64后的压缩数据)
     *
     * @param bytes 字节
     * @return string
     */
    public static String compress(byte[] bytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.close();
            return StringUtils.toBase64(out.toByteArray());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 系统压缩(返回base64后的压缩数据)
     *
     * @param val     字符
     * @param charset 编码
     * @return string
     */
    public static String compress(String val, String... charset) {
        if (StringUtils.isEmpty(val)) {
            return val;
        }
        try {
            return compress(charset != null && charset.length > 0 ? val.getBytes(charset[0]) : val.getBytes());
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 系统解压缩
     *
     * @param val     字符
     * @param charset 编码
     * @return string
     */
    public static String unCompress(String val, String... charset) {
        try {
            if (StringUtils.isEmpty(val)) {
                return val;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(base64ToByte(val));
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return charset != null && charset.length > 0 ? new String(out.toByteArray(), charset[0])
                    : new String(out.toByteArray());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static String substringBetween(String str, String open, String close) {
        if (str != null && open != null && close != null) {
            int start = str.indexOf(open);
            if (start != -1) {
                int end = str.indexOf(close, start + open.length());
                if (end != -1) {
                    return str.substring(start + open.length(), end);
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * 比较两个字符串（大小写敏感）
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abc&quot;)  = false
     * equals(&quot;abc&quot;, null)  = false
     * equals(&quot;abc&quot;, &quot;abc&quot;) = true
     * equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     * </pre>
     *
     * @param str1 要比较的字符串1
     * @param str2 要比较的字符串2
     * @return 如果两个字符串相同, 或者都是null, 则返回true
     */
    public static boolean equals(CharSequence str1, CharSequence str2) {
        return equals(str1, str2, false);
    }

    /**
     * 比较两个字符串（大小写不敏感）
     *
     * <pre>
     * equalsIgnoreCase(null, null)   = true
     * equalsIgnoreCase(null, &quot;abc&quot;)  = false
     * equalsIgnoreCase(&quot;abc&quot;, null)  = false
     * equalsIgnoreCase(&quot;abc&quot;, &quot;abc&quot;) = true
     * equalsIgnoreCase(&quot;abc&quot;, &quot;ABC&quot;) = true
     * </pre>
     *
     * @param str1 要比较的字符串1
     * @param str2 要比较的字符串2
     * @return 如果两个字符串相同, 或者都是null, 则返回true
     */
    public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
        return equals(str1, str2, true);
    }

    /**
     * 比较两个字符串是否相等
     *
     * @param str1       要比较的字符串1
     * @param str2       要比较的字符串2
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同, 或者都是null, 则返回true
     * @since 5.2.5
     */
    public static boolean equals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        if (null == str1) {
            // 只有两个都为null才判断相等
            return str2 == null;
        }
        if (null == str2) {
            // 字符串2空,字符串1非空,直接false
            return false;
        }

        if (ignoreCase) {
            return str1.toString().equalsIgnoreCase(str2.toString());
        } else {
            return str1.equals(str2);
        }
    }

    /**
     * 格式化字符串
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 如果想输出 {} 使用 \\转义 { 即可,如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b
     * 转义{}： 	format("this is \\{} for {}", "a", "b") =》 this is \{} for a
     * 转义\：		format("this is \\\\{} for {}", "a", "b") =》 this is \a for b
     *
     * @param val      字符串模板
     * @param argArray 参数列表
     * @return 结果
     */
    public static String format(final String val, final Object... argArray) {
        if (isBlank(val) || ArrayUtils.isEmpty(argArray)) {
            return val;
        }
        final int strPatternLength = val.length();

        //初始化定义好的长度以获得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;//记录已经处理到的位置
        int delimIndex;//占位符所在位置
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = val.indexOf(Symbol.DELIM, handledPosition);
            if (delimIndex == -1) {//剩余部分无占位符
                if (handledPosition == 0) { //不带占位符的模板直接返回
                    return val;
                } else { //字符串模板剩余部分不再包含占位符,加入剩余部分后返回结果
                    sbuf.append(val, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && val.charAt(delimIndex - 1) == Symbol.C_BACKSLASH) {//转义符
                    if (delimIndex > 1 && val.charAt(delimIndex - 2) == Symbol.C_BACKSLASH) {//双转义符
                        //转义符之前还有一个转义符,占位符依旧有效
                        sbuf.append(val, handledPosition, delimIndex - 1);
                        sbuf.append(utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        //占位符被转义
                        argIndex--;
                        sbuf.append(val, handledPosition, delimIndex - 1);
                        sbuf.append(Symbol.C_DELIM_LEFT);
                        handledPosition = delimIndex + 1;
                    }
                } else {//正常占位符
                    sbuf.append(val, handledPosition, delimIndex);
                    sbuf.append(utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        //加入最后一个占位符后所有的字符
        sbuf.append(val, handledPosition, val.length());

        return sbuf.toString();
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @param obj 对象
     * @return 字符串
     */
    public static String utf8Str(Object obj) {
        return str(obj, StandardCharsets.UTF_8);
    }


    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @param obj         对象
     * @param charsetName 字符集
     * @return 字符串
     */
    public static String str(Object obj, String charsetName) {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @param obj     对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return str((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return str((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer) obj, charset);
        } else if (ArrayUtils.isArray(obj)) {
            return ArrayUtils.toString(obj);
        }

        return obj.toString();
    }

    /**
     * 将byte数组转为字符串
     *
     * @param bytes   byte数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String str(byte[] bytes, String charset) {
        return str(bytes, isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 将Byte数组转为字符串
     *
     * @param bytes   byte数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String str(Byte[] bytes, String charset) {
        return str(bytes, isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String str(Byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte.byteValue();
        }

        return str(bytes, charset);
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     *
     * @param data    数据
     * @param charset 字符集,如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String str(ByteBuffer data, String charset) {
        if (data == null) {
            return null;
        }

        return str(data, Charset.forName(charset));
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     *
     * @param data    数据
     * @param charset 字符集,如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * {@link CharSequence} 转为字符串,null安全
     *
     * @param cs {@link CharSequence}
     * @return 字符串
     */
    public static String str(CharSequence cs) {
        return null == cs ? null : cs.toString();
    }

    /**
     * 切割指定位置之前部分的字符串
     *
     * @param string  字符串
     * @param toIndex 切割到的位置（不包括）
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(CharSequence string, int toIndex) {
        return sub(string, 0, toIndex);
    }

    /**
     * 切割指定位置之后部分的字符串
     *
     * @param string    字符串
     * @param fromIndex 切割开始的位置（包括）
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSuf(CharSequence string, int fromIndex) {
        if (isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    /**
     * 改进JDK subString
     * index从0开始计算,最后一个字符为-1
     * 如果from和to位置一样,返回 ""
     * 如果from或to为负数,则按照length从后向前数位置,如果绝对值大于字符串长度,则from归到0,to归到length
     * 如果经过修正的index中from大于to,则互换
     * abcdefgh 2 3 =》 c
     * abcdefgh 2 -3 =》 cde
     *
     * @param str       String
     * @param fromIndex 开始的index（包括）
     * @param toIndex   结束的index（不包括）
     * @return 字串
     */
    public static String sub(CharSequence str, int fromIndex, int toIndex) {
        if (isEmpty(str)) {
            return str(str);
        }
        int len = str.length();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return "";
        }

        return str.toString().substring(fromIndex, toIndex);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str 被切分的字符串
     * @return 切分后的集合
     */
    public static List<String> splitPath(String str) {
        return splitPath(str, 0);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> splitPath(String str, int limit) {
        return split(str, Symbol.C_SLASH, limit, true, true);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str 被切分的字符串
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(String str) {
        return toArray(splitPath(str));
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(String str, int limit) {
        return toArray(splitPath(str, limit));
    }

    /**
     * 切分字符串,去除切分后每个元素两边的空白符,去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     * @since 3.1.9
     */
    public static List<String> splitTrim(CharSequence str, char separator) {
        return splitTrim(str.toString(), separator, true);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitTrim(String str, char separator, boolean ignoreEmpty) {
        return split(str, separator, 0, true, ignoreEmpty);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitTrim(String str, String separator, boolean ignoreEmpty) {
        return split(str, separator, true, ignoreEmpty);
    }

    /**
     * 切分字符串,大小写敏感,去除每个元素两边空白符
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(String str, char separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty, false);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitTrim(String str, String separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty);
    }

    /**
     * 切分字符串,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitIgnoreCase(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitIgnoreCase(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> splitTrimIgnoreCase(String str, String separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty, true);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数
     * @return 切分后的集合
     */
    public static String[] splitToArray(CharSequence str, char separator, int limit) {
        if (null == str) {
            return new String[]{};
        }
        return splitToArray(str.toString(), separator, limit, false, false);
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, int limit) {
        return toArray(split(str, limit));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return toArray(split(str, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return toArray(split(str, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 通过正则切分字符串为字符串数组
     *
     * @param str              被切分的字符串
     * @param separatorPattern 分隔符正则{@link Pattern}
     * @param limit            限制分片数
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, Pattern separatorPattern, int limit, boolean isTrim, boolean ignoreEmpty) {
        return toArray(split(str, separatorPattern, limit, isTrim, ignoreEmpty));
    }

    /**
     * 通过正则切分字符串
     *
     * @param str            字符串
     * @param separatorRegex 分隔符正则
     * @param limit          限制分片数
     * @param isTrim         是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty    是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitByRegex(String str, String separatorRegex, int limit, boolean isTrim, boolean ignoreEmpty) {
        final Pattern pattern = PatternUtils.get(separatorRegex);
        return split(str, pattern, limit, isTrim, ignoreEmpty);
    }

    /**
     * @param text 每字符串
     * @param len  每一个小节的长度
     * @return 截取后的字符串数组
     */
    public static String[] splitByLength(String text, int len) {
        int partCount = text.length() / len;
        int lastPartCount = text.length() % len;
        int fixPart = 0;
        if (lastPartCount != 0) {
            fixPart = 1;
        }

        final String[] strs = new String[partCount + fixPart];
        for (int i = 0; i < partCount + fixPart; i++) {
            if (i == partCount + fixPart - 1 && lastPartCount != 0) {
                strs[i] = text.substring(i * len, i * len + lastPartCount);
            } else {
                strs[i] = text.substring(i * len, i * len + len);
            }
        }
        return strs;
    }

    /**
     * 切分字符串
     *
     * @param str 被切分的字符串
     * @return 字符串
     */
    public static String split(String str) {
        return split(str, Symbol.COMMA, Symbol.COMMA);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @param reserve   替换后的分隔符
     * @return 字符串
     */
    public static String split(String str, CharSequence separator, CharSequence reserve) {
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty(str)) {
            String[] arr = split(str, separator);
            for (int i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sb.append("'").append(arr[i]).append("'");
                } else {
                    sb.append(reserve).append("'").append(arr[i]).append("'");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static String[] split(CharSequence str, CharSequence separator) {
        if (str == null) {
            return new String[]{};
        }
        final String separatorStr = (null == separator) ? null : separator.toString();
        return splitToArray(str.toString(), separatorStr, 0, false, false);
    }

    /**
     * 切分字符串,去除切分后每个元素两边的空白符,去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     * @since 3.1.9
     */
    public static List<String> split(String str, char separator) {
        return split(str, separator, -1);
    }

    /**
     * 切分字符串
     * a#b#c =》 [a,b,c]
     * a##b#c =》 [a,"",b,c]
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, char separator) {
        return split(str, separator, 0);
    }

    /**
     * 使用空白符切分字符串
     * 切分后的字符串两边不包含空白符,空串或空白符串并不做为元素之一
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> split(String str, int limit) {
        if (isEmpty(str)) {
            return new ArrayList<String>(0);
        }
        if (limit == 1) {
            return addToList(new ArrayList<String>(1), str, true, true);
        }

        final ArrayList<String> list = new ArrayList<>();
        int len = str.length();
        int start = 0;//切分后每个部分的起始
        for (int i = 0; i < len; i++) {
            if (CharUtils.isBlankChar(str.charAt(i))) {
                addToList(list, str.substring(start, i), true, true);
                start = i + 1;//i+1同时将start与i保持一致

                //检查是否超出范围（最大允许limit-1个,剩下一个留给末尾字符串）
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            }
        }
        return addToList(list, str.substring(start, len), true, true);//收尾
    }

    /**
     * 切分字符串,不去除切分后每个元素两边的空白符,不去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数,-1不限制
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, char separator, int limit) {
        return split(str.toString(), separator, limit, false, false);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, 0, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串,不忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, String separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, -1, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串,大小写敏感
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, false);
    }


    /**
     * 切分字符串,不忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * 通过正则切分字符串
     *
     * @param str              字符串
     * @param separatorPattern 分隔符正则{@link Pattern}
     * @param limit            限制分片数
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, Pattern separatorPattern, int limit, boolean isTrim, boolean ignoreEmpty) {
        if (isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return addToList(new ArrayList<>(1), str, isTrim, ignoreEmpty);
        }

        if (null == separatorPattern) {//分隔符为空时按照空白符切分
            return split(str, limit);
        }

        final Matcher matcher = separatorPattern.matcher(str);
        final ArrayList<String> list = new ArrayList<>();
        int len = str.length();
        int start = 0;
        while (matcher.find()) {
            addToList(list, str.substring(start, matcher.start()), isTrim, ignoreEmpty);
            start = matcher.end();

            if (limit > 0 && list.size() > limit - 2) {
                break;
            }
        }
        return addToList(list, str.substring(start, len), isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> split(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty, boolean ignoreCase) {
        if (isEmpty(str)) {
            return new ArrayList<String>(0);
        }
        if (limit == 1) {
            return addToList(new ArrayList<String>(1), str, isTrim, ignoreEmpty);
        }

        final ArrayList<String> list = new ArrayList<>(limit > 0 ? limit : 16);
        int len = str.length();
        int start = 0;//切分后每个部分的起始
        for (int i = 0; i < len; i++) {
            if (NumberUtils.equals(separator, str.charAt(i), ignoreCase)) {
                addToList(list, str.substring(start, i), isTrim, ignoreEmpty);
                start = i + 1;//i+1同时将start与i保持一致

                //检查是否超出范围（最大允许limit-1个,剩下一个留给末尾字符串）
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            }
        }
        return addToList(list, str.substring(start, len), isTrim, ignoreEmpty);//收尾
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     * @since 5.2.5
     */
    public static List<String> split(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty, boolean ignoreCase) {
        if (isEmpty(str)) {
            return new ArrayList<String>(0);
        }
        if (limit == 1) {
            return addToList(new ArrayList<String>(1), str, isTrim, ignoreEmpty);
        }

        if (isEmpty(separator)) {//分隔符为空时按照空白符切分
            return split(str, limit);
        } else if (separator.length() == 1) {//分隔符只有一个字符长度时按照单分隔符切分
            return split(str, separator.charAt(0), limit, isTrim, ignoreEmpty, ignoreCase);
        }

        final ArrayList<String> list = new ArrayList<>();
        int len = str.length();
        int separatorLen = separator.length();
        int start = 0;
        int i = 0;
        while (i < len) {
            i = indexOf(str, separator, start, ignoreCase);
            if (i > -1) {
                addToList(list, str.substring(start, i), isTrim, ignoreEmpty);
                start = i + separatorLen;

                //检查是否超出范围（最大允许limit-1个,剩下一个留给末尾字符串）
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            } else {
                break;
            }
        }
        return addToList(list, str.substring(start, len), isTrim, ignoreEmpty);
    }

    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * 指定字符是否在字符串中出现过
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @return 是否包含
     * @since 3.1.9
     */
    public static boolean contains(CharSequence str, char searchChar) {
        return indexOf(str, searchChar) > -1;
    }

    /**
     * 除去字符串头尾部的空白符,如果字符串是null,依然返回null
     *
     * @param str  要处理的字符串
     * @param mode -1表示trimStart,0表示trim全部, 1表示trimEnd
     * @return 除去指定字符后的的字符串, 如果原字串为null, 则返回null
     */
    public static String trim(CharSequence str, int mode) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        int start = 0;
        int end = length;

        // 扫描字符串头部
        if (mode <= 0) {
            while ((start < end) && (CharUtils.isBlankChar(str.charAt(start)))) {
                start++;
            }
        }

        // 扫描字符串尾部
        if (mode >= 0) {
            while ((start < end) && (CharUtils.isBlankChar(str.charAt(end - 1)))) {
                end--;
            }
        }

        if ((start > 0) || (end < length)) {
            return str.toString().substring(start, end);
        }

        return str.toString();
    }

    /**
     * 字符串是否以给定字符开始
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否开始
     */
    public static boolean startWith(CharSequence str, char c) {
        return c == str.charAt(0);
    }

    /**
     * 是否以指定字符串开头
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param str          被监测字符串
     * @param prefix       开头字符串
     * @param isIgnoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean isIgnoreCase) {
        if (null == str || null == prefix) {
            return null == str && null == prefix;
        }

        if (isIgnoreCase) {
            return str.toString().toLowerCase().startsWith(prefix.toString().toLowerCase());
        } else {
            return str.toString().startsWith(prefix.toString());
        }
    }

    /**
     * 是否以指定字符串开头
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, false);
    }

    /**
     * 是否以指定字符串开头,忽略大小写
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }

    public static String capitalize(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            char firstChar = str.charAt(0);
            return Character.isTitleCase(firstChar) ? str : (new StringBuilder(strLen)).append(Character.toTitleCase(firstChar)).append(str.substring(1)).toString();
        } else {
            return str;
        }
    }

    public static String uncapitalize(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            char firstChar = str.charAt(0);
            return Character.isLowerCase(firstChar) ? str : (new StringBuilder(strLen)).append(Character.toLowerCase(firstChar)).append(str.substring(1)).toString();
        } else {
            return str;
        }
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar) {
        return indexOf(str, searchChar, 0);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @param start      起始位置,如果小于0,从0开始查找
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar, int start) {
        if (str instanceof String) {
            return ((String) str).indexOf(searchChar, start);
        } else {
            return indexOf(str, searchChar, start, -1);
        }
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @param start      起始位置,如果小于0,从0开始查找
     * @param end        终止位置,如果超过str.length()则默认查找到字符串末尾
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar, int start, int end) {
        final int len = str.length();
        if (start < 0 || start > len) {
            start = 0;
        }
        if (end > len || end < 0) {
            end = len;
        }
        for (int i = start; i < end; i++) {
            if (str.charAt(i) == searchChar) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * <pre>
     * StringUtils.indexOfIgnoreCase(null, *, *)          = -1
     * StringUtils.indexOfIgnoreCase(*, null, *)          = -1
     * StringUtils.indexOfIgnoreCase("", "", 0)           = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringUtils.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @return 位置
     * @since 5.2.5
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * 指定范围内查找字符串
     *
     * <pre>
     * StringUtils.indexOfIgnoreCase(null, *, *)          = -1
     * StringUtils.indexOfIgnoreCase(*, null, *)          = -1
     * StringUtils.indexOfIgnoreCase("", "", 0)           = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringUtils.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @param fromIndex 起始位置
     * @return 位置
     * @since 5.2.5
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int fromIndex) {
        return indexOf(str, searchStr, fromIndex, true);
    }

    /**
     * 指定范围内反向查找字符串
     *
     * @param str        字符串
     * @param searchStr  需要查找位置的字符串
     * @param fromIndex  起始位置
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     * @since 5.2.5
     */
    public static int indexOf(final CharSequence str, CharSequence searchStr, int fromIndex, boolean ignoreCase) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        final int endLimit = str.length() - searchStr.length() + 1;
        if (fromIndex > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return fromIndex;
        }

        if (false == ignoreCase) {
            // 不忽略大小写调用JDK方法
            return str.toString().indexOf(searchStr.toString(), fromIndex);
        }

        for (int i = fromIndex; i < endLimit; i++) {
            if (isSubEquals(str, i, searchStr, 0, searchStr.length(), true)) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @return 位置
     * @since 5.2.5
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return lastIndexOfIgnoreCase(str, searchStr, str.length());
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @param fromIndex 起始位置,从后往前计数
     * @return 位置
     * @since 5.2.5
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int fromIndex) {
        return lastIndexOf(str, searchStr, fromIndex, true);
    }

    /**
     * 指定范围内查找字符串
     *
     * @param str        字符串
     * @param searchStr  需要查找位置的字符串
     * @param fromIndex  起始位置,从后往前计数
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     * @since 5.2.5
     */
    public static int lastIndexOf(final CharSequence str, final CharSequence searchStr, int fromIndex, boolean ignoreCase) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        fromIndex = Math.min(fromIndex, str.length());

        if (searchStr.length() == 0) {
            return fromIndex;
        }

        if (false == ignoreCase) {
            // 不忽略大小写调用JDK方法
            return str.toString().lastIndexOf(searchStr.toString(), fromIndex);
        }

        for (int i = fromIndex; i > 0; i--) {
            if (isSubEquals(str, i, searchStr, 0, searchStr.length(), true)) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回字符串 searchStr 在字符串 str 中第 ordinal 次出现的位置
     * 如果 str=null 或 searchStr=null 或 ordinal小于等于0 则返回-1
     *
     * <pre>
     * StringUtils.ordinalIndexOf(null, *, *)          = -1
     * StringUtils.ordinalIndexOf(*, null, *)          = -1
     * StringUtils.ordinalIndexOf("", "", *)           = 0
     * StringUtils.ordinalIndexOf("aabaabaa", "a", 1)  = 0
     * StringUtils.ordinalIndexOf("aabaabaa", "a", 2)  = 1
     * StringUtils.ordinalIndexOf("aabaabaa", "b", 1)  = 2
     * StringUtils.ordinalIndexOf("aabaabaa", "b", 2)  = 5
     * StringUtils.ordinalIndexOf("aabaabaa", "ab", 1) = 1
     * StringUtils.ordinalIndexOf("aabaabaa", "ab", 2) = 4
     * StringUtils.ordinalIndexOf("aabaabaa", "", 1)   = 0
     * StringUtils.ordinalIndexOf("aabaabaa", "", 2)   = 0
     * </pre>
     *
     * @param str       被检查的字符串,可以为null
     * @param searchStr 被查找的字符串,可以为null
     * @param ordinal   第几次出现的位置
     * @return 查找到的位置
     * @since 3.2.3
     */
    public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
        if (str == null || searchStr == null || ordinal <= 0) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return 0;
        }
        int found = 0;
        int index = INDEX_NOT_FOUND;
        do {
            index = str.indexOf(searchStr, index + 1);
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    /**
     * 截取两个字符串的不同部分（长度一致）,判断截取的子串是否相同
     * 任意一个字符串为null返回false
     *
     * @param str1       第一个字符串
     * @param start1     第一个字符串开始的位置
     * @param str2       第二个字符串
     * @param start2     第二个字符串开始的位置
     * @param length     截取长度
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     * @since 5.2.5
     */
    public static boolean isSubEquals(CharSequence str1, int start1, CharSequence str2, int start2, int length, boolean ignoreCase) {
        if (null == str1 || null == str2) {
            return false;
        }

        return str1.toString().regionMatches(ignoreCase, start1, str2.toString(), start2, length);
    }

    /**
     * 是否包含空字符串
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... strs) {
        if (ArrayUtils.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重复某个字符
     *
     * @param c     被重复的字符
     * @param count 重复的数目,如果小于等于0则返回""
     * @return 重复字符字符串
     */
    public static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }

        char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * 重复某个字符串
     *
     * @param str   被重复的字符
     * @param count 重复的数目
     * @return 重复字符字符串
     */
    public static String repeat(CharSequence str, int count) {
        if (null == str) {
            return null;
        }
        if (count <= 0) {
            return "";
        }
        if (count == 1 || str.length() == 0) {
            return str.toString();
        }

        // 检查
        final int len = str.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        final char[] array = new char[size];
        str.toString().getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {// n <<= 1相当于n *2
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * 反转字符串
     * 例如：abcd =》dcba
     *
     * @param str 被反转的字符串
     * @return 反转后的字符串
     * @since 3.1.9
     */
    public static String reverse(String str) {
        char[] chars = str.toCharArray();
        ArrayUtils.reverse(chars);
        return new String(chars);
    }

    /**
     * 编码字符串
     * 使用系统默认编码
     *
     * @param str 字符串
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str) {
        return bytes(str, Charset.defaultCharset());
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str, String charset) {
        return bytes(str, isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str, Charset charset) {
        if (str == null) {
            return null;
        }

        if (null == charset) {
            return str.toString().getBytes();
        }
        return str.toString().getBytes(charset);
    }

    /**
     * 将驼峰式命名的字符串转换为下划线方式 如果转换前的驼峰式命名的字符串为空,则返回空字符串
     * 例如：HelloWorld=》hello_world
     *
     * @param camelCaseStr 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String toUnderlineCase(CharSequence camelCaseStr) {
        if (camelCaseStr == null) {
            return null;
        }

        final int length = camelCaseStr.length();
        StringBuilder sb = new StringBuilder();
        char c;
        boolean isPreUpperCase = false;
        for (int i = 0; i < length; i++) {
            c = camelCaseStr.charAt(i);
            boolean isNextUpperCase = true;
            if (i < (length - 1)) {
                isNextUpperCase = Character.isUpperCase(camelCaseStr.charAt(i + 1));
            }
            if (Character.isUpperCase(c)) {
                if (!isPreUpperCase || !isNextUpperCase) {
                    if (i > 0) {
                        sb.append(Symbol.UNDERLINE);
                    }
                }
                isPreUpperCase = true;
            } else {
                isPreUpperCase = false;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 删除指定字符串
     * 是否在开始位置,否则返回源字符串
     * A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.
     *
     * <pre>
     * StringUtils.removeStart(null, *)      = null
     * StringUtils.removeStart("", *)        = ""
     * StringUtils.removeStart(*, null)      = *
     * StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
     * StringUtils.removeStart("domain.com", "www.")       = "domain.com"
     * StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeStart("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     * @since 2.1.0
     */
    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：str=setName, preLength=3 =》 return name
     *
     * @param str       被处理的字符串
     * @param preLength 去掉的长度
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence str, int preLength) {
        if (str == null) {
            return null;
        }
        if (str.length() > preLength) {
            char first = Character.toLowerCase(str.charAt(preLength));
            if (str.length() > preLength + 1) {
                return first + str.toString().substring(preLength + 1);
            }
            return String.valueOf(first);
        } else {
            return str.toString();
        }
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：str=setName, prefix=set =》 return name
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence str, CharSequence prefix) {
        return lowerFirst(removePrefix(str, prefix));
    }


    /**
     * 去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 preffix, 返回原字符串
     */
    public static String removePrefix(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str(str);
        }

        final String str2 = str.toString();
        if (str2.startsWith(prefix.toString())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 忽略大小写去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 prefix, 返回原字符串
     */
    public static String removePrefixIgnoreCase(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str(str);
        }

        final String str2 = str.toString();
        if (str2.toLowerCase().startsWith(prefix.toString().toLowerCase())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 去掉指定后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 切掉后的字符串, 若后缀不是 suffix, 返回原字符串
     */
    public static String removeSuffix(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str(str);
        }

        final String str2 = str.toString();
        if (str2.endsWith(suffix.toString())) {
            return subPre(str2, str2.length() - suffix.length());// 截取前半段
        }
        return str2;
    }

    /**
     * 原字符串首字母大写并在其首部添加指定字符串 例如：str=name, preString=get =》 return getName
     *
     * @param str       被处理的字符串
     * @param preString 添加的首部
     * @return 处理后的字符串
     */
    public static String upperFirstAndAddPre(CharSequence str, String preString) {
        if (str == null || preString == null) {
            return null;
        }
        return preString + upperFirst(str);
    }

    /**
     * 大写首字母
     * 例如：str = name, return Name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String upperFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 小写首字母
     * 例如：str = Name, return name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String lowerFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 将字符串加入List中
     *
     * @param list        列表
     * @param part        被加入的部分
     * @param isTrim      是否去除两端空白符
     * @param ignoreEmpty 是否略过空字符串（空字符串不做为一个元素）
     * @return 列表
     */
    private static List<String> addToList(List<String> list, String part, boolean isTrim, boolean ignoreEmpty) {
        if (isTrim) {
            part = part.trim();
        }
        if (false == ignoreEmpty || false == part.isEmpty()) {
            list.add(part);
        }
        return list;
    }

    /**
     * List转Array
     *
     * @param list List
     * @return Array
     */
    private static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    /**
     * Gets the substring before the last occurrence of a separator.
     * The separator is not returned.
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the last occurrence of the separator,
     */
    public static String substringBeforeLast(final String str, final String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Gets the substring after the last occurrence of a separator.
     * The separator is not returned.
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     */
    public static String substringAfterLast(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return Normal.EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return Normal.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式 如果转换前的下划线大写方式命名的字符串为空,则返回空字符串
     * 例如：hello_world=》helloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(CharSequence name) {
        if (null == name) {
            return null;
        }

        String name2 = name.toString();
        if (name2.contains(Symbol.UNDERLINE)) {
            final StringBuilder sb = new StringBuilder(name2.length());
            boolean upperCase = false;
            for (int i = 0; i < name2.length(); i++) {
                char c = name2.charAt(i);

                if (c == Symbol.C_UNDERLINE) {
                    upperCase = true;
                } else if (upperCase) {
                    sb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return sb.toString();
        } else {
            return name2;
        }
    }

    /**
     * 当给定字符串为null时,转换为Empty
     *
     * @param str 被转换的字符串
     * @return 转换后的字符串
     */
    public static String nullToEmpty(CharSequence str) {
        return nullToDefault(str, Normal.EMPTY);
    }

    /**
     * 如果字符串是<code>null</code>,则返回指定默认字符串,否则返回字符串本身
     *
     * <pre>
     * nullToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * nullToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;&quot;
     * nullToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * nullToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String nullToDefault(CharSequence str, String defaultStr) {
        return (str == null) ? defaultStr : str.toString();
    }

    /**
     * 字符串的每一个字符是否都与定义的匹配器匹配
     *
     * @param value   字符串
     * @param matcher 匹配器
     * @return 是否全部匹配
     * @since 3.2.3
     */
    public static boolean isAllCharMatch(CharSequence value, org.aoju.bus.core.lang.Matcher<Character> matcher) {
        if (isBlank(value)) {
            return false;
        }
        int len = value.length();
        boolean isAllMatch = true;
        for (int i = 0; i < len; i++) {
            isAllMatch &= matcher.match(value.charAt(i));
        }
        return isAllMatch;
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringUtils.remove(null, *)        = null
     * StringUtils.remove("", *)          = ""
     * StringUtils.remove(*, null)        = *
     * StringUtils.remove(*, "")          = *
     * StringUtils.remove("queued", "ue") = "qd"
     * StringUtils.remove("queued", "zz") = "queued"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     * @since 2.1.0
     */
    public static String remove(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replace(str, remove, Normal.EMPTY, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringUtils.removeIgnoreCase(null, *)        = null
     * StringUtils.removeIgnoreCase("", *)          = ""
     * StringUtils.removeIgnoreCase(*, null)        = *
     * StringUtils.removeIgnoreCase(*, "")          = *
     * StringUtils.removeIgnoreCase("queued", "ue") = "qd"
     * StringUtils.removeIgnoreCase("queued", "zz") = "queued"
     * StringUtils.removeIgnoreCase("quEUed", "UE") = "qd"
     * StringUtils.removeIgnoreCase("queued", "zZ") = "queued"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for (case insensitive) and remove, may be
     *               null
     * @return the substring with the string removed if found, {@code null} if
     * null String input
     * @since 3.5.0
     */
    public static String removeIgnoreCase(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replaceIgnoreCase(str, remove, Normal.EMPTY, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringUtils.remove(null, *)       = null
     * StringUtils.remove("", *)         = ""
     * StringUtils.remove("queued", 'u') = "qeed"
     * StringUtils.remove("queued", 'z') = "queued"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the char to search for and remove, may be null
     * @return the substring with the char removed if found,
     * {@code null} if null String input
     * @since 2.1.0
     */
    public static String remove(final String str, final char remove) {
        if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
            return str;
        }
        final char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringUtils.removeAll(null, *)      = null
     * StringUtils.removeAll("any", null)  = "any"
     * StringUtils.removeAll("any", "")    = "any"
     * StringUtils.removeAll("any", ".*")  = ""
     * StringUtils.removeAll("any", ".+")  = ""
     * StringUtils.removeAll("abc", ".?")  = ""
     * StringUtils.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\nB"
     * StringUtils.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
     * StringUtils.removeAll("ABCabc123abc", "[a-z]")     = "ABC123"
     * </pre>
     *
     * @param text  text to remove from, may be null
     * @param regex the regular expression to which this string is to be matched
     * @return the text with any removes processed,
     * {@code null} if null String input
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #replaceAll(String, String, String)
     * @see String#replaceAll(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     * @since 3.5.0
     */
    public static String removeAll(final String text, final String regex) {
        return replaceAll(text, regex, Normal.EMPTY);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.removeFirst(null, *)      = null
     * StringUtils.removeFirst("any", null)  = "any"
     * StringUtils.removeFirst("any", "")    = "any"
     * StringUtils.removeFirst("any", ".*")  = ""
     * StringUtils.removeFirst("any", ".+")  = ""
     * StringUtils.removeFirst("abc", ".?")  = "bc"
     * StringUtils.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\n&lt;__&gt;B"
     * StringUtils.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
     * StringUtils.removeFirst("ABCabc123", "[a-z]")          = "ABCbc123"
     * StringUtils.removeFirst("ABCabc123abc", "[a-z]+")      = "ABC123abc"
     *
     * @param text  text to remove from, may be null
     * @param regex the regular expression to which this string is to be matched
     * @return the text with the first replacement processed,
     * {@code null} if null String input
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #replaceFirst(String, String, String)
     * @see String#replaceFirst(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     * @since 3.5.0
     */
    public static String removeFirst(final String text, final String regex) {
        return replaceFirst(text, regex, Normal.EMPTY);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replaceAll(null, *, *)       = null
     * StringUtils.replaceAll("any", null, *)   = "any"
     * StringUtils.replaceAll("any", *, null)   = "any"
     * StringUtils.replaceAll("", "", "zzz")    = "zzz"
     * StringUtils.replaceAll("", ".*", "zzz")  = "zzz"
     * StringUtils.replaceAll("", ".+", "zzz")  = ""
     * StringUtils.replaceAll("abc", "", "ZZ")  = "ZZaZZbZZcZZ"
     * StringUtils.replaceAll("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\nz"
     * StringUtils.replaceAll("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
     * StringUtils.replaceAll("ABCabc123", "[a-z]", "_")       = "ABC___123"
     * StringUtils.replaceAll("ABCabc123", "[^A-Z0-9]+", "_")  = "ABC_123"
     * StringUtils.replaceAll("ABCabc123", "[^A-Z0-9]+", "")   = "ABC123"
     * StringUtils.replaceAll("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum_dolor_sit"
     *
     * @param text        text to search and replace in, may be null
     * @param regex       the regular expression to which this string is to be matched
     * @param replacement the string to be substituted for each match
     * @return the text with any replacements processed,
     * {@code null} if null String input
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceAll(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     * @since 3.5.0
     */
    public static String replaceAll(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) {
            return text;
        }
        return text.replaceAll(regex, replacement);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replaceFirst(null, *, *)       = null
     * StringUtils.replaceFirst("any", null, *)   = "any"
     * StringUtils.replaceFirst("any", *, null)   = "any"
     * StringUtils.replaceFirst("", "", "zzz")    = "zzz"
     * StringUtils.replaceFirst("", ".*", "zzz")  = "zzz"
     * StringUtils.replaceFirst("", ".+", "zzz")  = ""
     * StringUtils.replaceFirst("abc", "", "ZZ")  = "ZZabc"
     * StringUtils.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\n&lt;__&gt;"
     * StringUtils.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
     * StringUtils.replaceFirst("ABCabc123", "[a-z]", "_")          = "ABC_bc123"
     * StringUtils.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "_")  = "ABC_123abc"
     * StringUtils.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "")   = "ABC123abc"
     * StringUtils.replaceFirst("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum  dolor   sit"
     *
     * @param text        text to search and replace in, may be null
     * @param regex       the regular expression to which this string is to be matched
     * @param replacement the string to be substituted for the first match
     * @return the text with the first replacement processed,
     * {@code null} if null String input
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceFirst(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     * @since 3.5.0
     */
    public static String replaceFirst(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) {
            return text;
        }
        return text.replaceFirst(regex, replacement);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * {@code null} if null String input
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replace(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     * StringUtils.replaceIgnoreCase(null, *, *)        = null
     * StringUtils.replaceIgnoreCase("", *, *)          = ""
     * StringUtils.replaceIgnoreCase("any", null, *)    = "any"
     * StringUtils.replaceIgnoreCase("any", *, null)    = "any"
     * StringUtils.replaceIgnoreCase("any", "", *)      = "any"
     * StringUtils.replaceIgnoreCase("aba", "a", null)  = "aba"
     * StringUtils.replaceIgnoreCase("abA", "A", "")    = "b"
     * StringUtils.replaceIgnoreCase("aba", "A", "z")   = "zbz"
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * {@code null} if null String input
     * @see #replaceIgnoreCase(String text, String searchString, String replacement, int max)
     * @since 3.5.0
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement) {
        return replaceIgnoreCase(text, searchString, replacement, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     * {@code null} if null String input
     */
    public static String replace(final String text, final String searchString, final String replacement, final int max) {
        return replace(text, searchString, replacement, max, false);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replace(null, *, *, *, false)         = null
     * StringUtils.replace("", *, *, *, false)           = ""
     * StringUtils.replace("any", null, *, *, false)     = "any"
     * StringUtils.replace("any", *, null, *, false)     = "any"
     * StringUtils.replace("any", "", *, *, false)       = "any"
     * StringUtils.replace("any", *, *, 0, false)        = "any"
     * StringUtils.replace("abaa", "a", null, -1, false) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1, false)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, true)   = "zbaa"
     * StringUtils.replace("abAa", "a", "z", 2, true)   = "zbza"
     * StringUtils.replace("abAa", "a", "z", -1, true)  = "zbzz"
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or {@code -1} if no maximum
     * @param ignoreCase   if true replace is case insensitive, otherwise case sensitive
     * @return the text with any replacements processed,
     * {@code null} if null String input
     */
    private static String replace(final String text, String searchString, final String replacement, int max, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        String searchText = text;
        if (ignoreCase) {
            searchText = text.toLowerCase();
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * for the first {@code max} values of the search String.
     * StringUtils.replaceIgnoreCase(null, *, *, *)         = null
     * StringUtils.replaceIgnoreCase("", *, *, *)           = ""
     * StringUtils.replaceIgnoreCase("any", null, *, *)     = "any"
     * StringUtils.replaceIgnoreCase("any", *, null, *)     = "any"
     * StringUtils.replaceIgnoreCase("any", "", *, *)       = "any"
     * StringUtils.replaceIgnoreCase("any", *, *, 0)        = "any"
     * StringUtils.replaceIgnoreCase("abaa", "a", null, -1) = "abaa"
     * StringUtils.replaceIgnoreCase("abaa", "a", "", -1)   = "b"
     * StringUtils.replaceIgnoreCase("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replaceIgnoreCase("abaa", "A", "z", 1)   = "zbaa"
     * StringUtils.replaceIgnoreCase("abAa", "a", "z", 2)   = "zbza"
     * StringUtils.replaceIgnoreCase("abAa", "a", "z", -1)  = "zbzz"
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     * {@code null} if null String input
     * @since 3.5.0
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement, final int max) {
        return replace(text, searchString, replacement, max, true);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replaceEach(null, *, *)        = null
     * StringUtils.replaceEach("", *, *)          = ""
     * StringUtils.replaceEach("aba", null, null) = "aba"
     * StringUtils.replaceEach("aba", new String[0], null) = "aba"
     * StringUtils.replaceEach("aba", null, new String[0]) = "aba"
     * StringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
     * StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
     * StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
     * (example of how it does not repeat)
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
     *
     * @param text            text to search and replace in, no-op if null
     * @param searchList      the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @return the text with any replacements processed, {@code null} if
     * null String input
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *                                  and/or size 0)
     * @since 2.4.0
     */
    public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
        return replaceEach(text, searchList, replacementList, false, 0);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replaceEachRepeatedly(null, *, *) = null
     * StringUtils.replaceEachRepeatedly("", *, *) = ""
     * StringUtils.replaceEachRepeatedly("aba", null, null) = "aba"
     * StringUtils.replaceEachRepeatedly("aba", new String[0], null) = "aba"
     * StringUtils.replaceEachRepeatedly("aba", null, new String[0]) = "aba"
     * StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, null) = "aba"
     * StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{""}) = "b"
     * StringUtils.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"a"}) = "aba"
     * StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}) = "wcte"
     * (example of how it repeats)
     * StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}) = "tcte"
     * StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}) = IllegalStateException
     *
     * @param text            text to search and replace in, no-op if null
     * @param searchList      the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @return the text with any replacements processed, {@code null} if
     * null String input
     * @throws IllegalStateException    if the search is repeating and there is an endless loop due
     *                                  to outputs of one being inputs to another
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *                                  and/or size 0)
     * @since 2.4.0
     */
    public static String replaceEachRepeatedly(final String text, final String[] searchList, final String[] replacementList) {
        final int timeToLive = searchList == null ? 0 : searchList.length;
        return replaceEach(text, searchList, replacementList, true, timeToLive);
    }

    /**
     * 替换字符串中的指定字符串.
     * {@link #replaceEachRepeatedly(String, String[], String[])}
     * StringUtils.replaceEach(null, *, *, *, *) = null
     * StringUtils.replaceEach("", *, *, *, *) = ""
     * StringUtils.replaceEach("aba", null, null, *, *) = "aba"
     * StringUtils.replaceEach("aba", new String[0], null, *, *) = "aba"
     * StringUtils.replaceEach("aba", null, new String[0], *, *) = "aba"
     * StringUtils.replaceEach("aba", new String[]{"a"}, null, *, *) = "aba"
     * StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}, *, >=0) = "b"
     * StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}, *, >=0) = "aba"
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *, >=0) = "wcte"
     * (example of how it repeats)
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false, >=0) = "dcte"
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true, >=2) = "tcte"
     * StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *, *) = IllegalStateException
     *
     * @param text            text to search and replace in, no-op if null
     * @param searchList      the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @param repeat          if true, then replace repeatedly
     *                        until there are no more possible replacements or timeToLive < 0
     * @param timeToLive      if less than 0 then there is a circular reference and endless
     *                        loop
     * @return the text with any replacements processed, {@code null} if
     * null String input
     * @throws IllegalStateException    if the search is repeating and there is an endless loop due
     *                                  to outputs of one being inputs to another
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *                                  and/or size 0)
     * @since 2.4.0
     */
    private static String replaceEach(
            final String text,
            final String[] searchList,
            final String[] replacementList,
            final boolean repeat,
            final int timeToLive) {
        if (text == null || text.isEmpty() || searchList == null ||
                searchList.length == 0 || replacementList == null || replacementList.length == 0) {
            return text;
        }

        if (timeToLive < 0) {
            throw new IllegalStateException("Aborting to protect against StackOverflowError - " +
                    "output of one loop is the input of another");
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;

        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                    searchList[i].isEmpty() || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else {
                if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }

        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        int increase = 0;

        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater;
            }
        }

        increase = Math.min(increase, text.length() / 5);

        final StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else {
                    if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                    }
                }
            }

        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        final String result = buf.toString();
        if (!repeat) {
            return result;
        }

        return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * This is a null-safe version of {@link String#replace(char, char)}.
     * StringUtils.replaceChars(null, *, *)        = null
     * StringUtils.replaceChars("", *, *)          = ""
     * StringUtils.replaceChars("abcba", 'b', 'y') = "aycya"
     * StringUtils.replaceChars("abcba", 'z', 'y') = "abcba"
     *
     * @param str         String to replace characters in, may be null
     * @param searchChar  the character to search for, may be null
     * @param replaceChar the character to replace, may be null
     * @return modified String, {@code null} if null string input
     * @since 2.0.0
     */
    public static String replaceChars(final String str, final char searchChar, final char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringUtils.replaceChars(null, *, *)           = null
     * StringUtils.replaceChars("", *, *)             = ""
     * StringUtils.replaceChars("abc", null, *)       = "abc"
     * StringUtils.replaceChars("abc", "", *)         = "abc"
     * StringUtils.replaceChars("abc", "b", null)     = "ac"
     * StringUtils.replaceChars("abc", "b", "")       = "ac"
     * StringUtils.replaceChars("abcba", "bc", "yz")  = "ayzya"
     * StringUtils.replaceChars("abcba", "bc", "y")   = "ayya"
     * StringUtils.replaceChars("abcba", "bc", "yzx") = "ayzya"
     *
     * @param str          String to replace characters in, may be null
     * @param searchChars  a set of characters to search for, may be null
     * @param replaceChars a set of characters to replace, may be null
     * @return modified String, {@code null} if null string input
     * @since 2.0.0
     */
    public static String replaceChars(final String str, final String searchChars, String replaceChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return str;
        }
        if (replaceChars == null) {
            replaceChars = Normal.EMPTY;
        }
        boolean modified = false;
        final int replaceCharsLength = replaceChars.length();
        final int strLength = str.length();
        final StringBuilder buf = new StringBuilder(strLength);
        for (int i = 0; i < strLength; i++) {
            final char ch = str.charAt(i);
            final int index = searchChars.indexOf(ch);
            if (index >= 0) {
                modified = true;
                if (index < replaceCharsLength) {
                    buf.append(replaceChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        if (modified) {
            return buf.toString();
        }
        return str;
    }

    /**
     * 替换字符串中的指定字符串,忽略大小写
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceIgnoreCase(CharSequence str, CharSequence searchStr, CharSequence replacement) {
        return replace(str, 0, searchStr, replacement, true);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement) {
        return replace(str, 0, searchStr, replacement, false);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement, boolean ignoreCase) {
        return replace(str, 0, searchStr, replacement, ignoreCase);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param fromIndex   开始位置（包括）
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, int fromIndex, CharSequence searchStr, CharSequence replacement, boolean ignoreCase) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return str(str);
        }
        if (null == replacement) {
            replacement = Normal.EMPTY;
        }

        final int strLength = str.length();
        final int searchStrLength = searchStr.length();
        if (fromIndex > strLength) {
            return str(str);
        } else if (fromIndex < 0) {
            fromIndex = 0;
        }

        final TextUtils result = create(strLength + 16);
        if (0 != fromIndex) {
            result.append(str.subSequence(0, fromIndex));
        }

        int preIndex = fromIndex;
        int index = fromIndex;
        while ((index = indexOf(str, searchStr, preIndex, ignoreCase)) > -1) {
            result.append(str.subSequence(preIndex, index));
            result.append(replacement);
            preIndex = index + searchStrLength;
        }

        if (preIndex < strLength) {
            // 结尾部分
            result.append(str.subSequence(preIndex, strLength));
        }
        return result.toString();
    }

    /**
     * 替换指定字符串的指定区间内字符为固定字符
     *
     * @param str          字符串
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @param replacedChar 被替换的字符
     * @return 替换后的字符串
     * @since 5.2.5
     */
    public static String replace(CharSequence str, int startInclude, int endExclude, char replacedChar) {
        if (isEmpty(str)) {
            return str(str);
        }
        final int strLength = str.length();
        if (startInclude > strLength) {
            return str(str);
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大于结束位置,不替换
            return str(str);
        }

        final char[] chars = new char[strLength];
        for (int i = 0; i < strLength; i++) {
            if (i >= startInclude && i < endExclude) {
                chars[i] = replacedChar;
            } else {
                chars[i] = str.charAt(i);
            }
        }
        return new String(chars);
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     *
     * @param str          字符串
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @return 替换后的字符串
     */
    public static String hide(CharSequence str, int startInclude, int endExclude) {
        return replace(str, startInclude, endExclude, '*');
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     * 提供的chars为所有需要被替换的字符,例如："\r\n",则"\r"和"\n"都会被替换,哪怕他们单独存在
     *
     * @param str         被检查的字符串
     * @param chars       需要替换的字符列表,用一个字符串表示这个字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     * @since 5.2.5
     */
    public static String replaceChars(CharSequence str, String chars, CharSequence replacedStr) {
        if (isEmpty(str) || isEmpty(chars)) {
            return str(str);
        }
        return replaceChars(str, chars.toCharArray(), replacedStr);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     *
     * @param str         被检查的字符串
     * @param chars       需要替换的字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     * @since 5.2.5
     */
    public static String replaceChars(CharSequence str, char[] chars, CharSequence replacedStr) {
        if (isEmpty(str) || ArrayUtils.isEmpty(chars)) {
            return str(str);
        }

        final Set<Character> set = new HashSet<>(chars.length);
        for (char c : chars) {
            set.add(c);
        }
        int strLen = str.length();
        final StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < strLen; i++) {
            c = str.charAt(i);
            builder.append(set.contains(c) ? replacedStr : c);
        }
        return builder.toString();
    }


    /**
     * 清理空白字符
     *
     * @param str 被清理的字符串
     * @return 清理后的字符串
     */
    public static String cleanBlank(CharSequence str) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (false == CharUtils.isBlankChar(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 包装指定字符串
     * 当前缀和后缀一致时使用此方法
     *
     * @param str             被包装的字符串
     * @param prefixAndSuffix 前缀和后缀
     * @return 包装后的字符串
     * @since 3.1.9
     */
    public static String wrap(CharSequence str, CharSequence prefixAndSuffix) {
        return wrap(str, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 包装指定字符串
     *
     * @param str    被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrap(CharSequence str, CharSequence prefix, CharSequence suffix) {
        return nullToEmpty(prefix).concat(nullToEmpty(str)).concat(nullToEmpty(suffix));
    }

    /**
     * 包装多个字符串
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param strs            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefixAndSuffix, CharSequence... strs) {
        return wrapAll(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * 包装多个字符串
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param strs   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefix, CharSequence suffix, CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrap(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str    字符串
     * @param prefix 前置字符串
     * @param suffix 后置字符串
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, String prefix, String suffix) {
        if (isWrap(str, prefix, suffix)) {
            return sub(str, prefix.length(), str.length() - suffix.length());
        }
        return str.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str    字符串
     * @param prefix 前置字符
     * @param suffix 后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, char prefix, char suffix) {
        if (isEmpty(str)) {
            return str(str);
        }
        if (str.charAt(0) == prefix && str.charAt(str.length() - 1) == suffix) {
            return sub(str, 1, str.length() - 1);
        }
        return str.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str             字符串
     * @param prefixAndSuffix 前置和后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, char prefixAndSuffix) {
        return unWrap(str, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, String prefix, String suffix) {
        if (ArrayUtils.hasNull(str, prefix, suffix)) {
            return false;
        }
        final String str2 = str.toString();
        return str2.startsWith(prefix) && str2.endsWith(suffix);
    }

    /**
     * 指定字符串是否被同一字符包装（前后都有这些字符串）
     *
     * @param str     字符串
     * @param wrapper 包装字符串
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, String wrapper) {
        return isWrap(str, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被同一字符包装（前后都有这些字符串）
     *
     * @param str     字符串
     * @param wrapper 包装字符
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, char wrapper) {
        return isWrap(str, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param str        字符串
     * @param prefixChar 前缀
     * @param suffixChar 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, char prefixChar, char suffixChar) {
        if (null == str) {
            return false;
        }

        return str.charAt(0) == prefixChar && str.charAt(str.length() - 1) == suffixChar;
    }

    /**
     * 字符串是否以给定字符结尾
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否结尾
     */
    public static boolean endWith(CharSequence str, char c) {
        return c == str.charAt(str.length() - 1);
    }

    /**
     * 是否以指定字符串结尾
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param str          被监测字符串
     * @param suffix       结尾字符串
     * @param isIgnoreCase 是否忽略大小写
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence str, CharSequence suffix, boolean isIgnoreCase) {
        if (null == str || null == suffix) {
            return null == str && null == suffix;
        }

        if (isIgnoreCase) {
            return str.toString().toLowerCase().endsWith(suffix.toString().toLowerCase());
        } else {
            return str.toString().endsWith(suffix.toString());
        }
    }

    /**
     * 是否以指定字符串结尾
     *
     * @param str    被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence str, CharSequence suffix) {
        return endWith(str, suffix, false);
    }

    /**
     * 是否以指定字符串结尾,忽略大小写
     *
     * @param str    被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWithIgnoreCase(CharSequence str, CharSequence suffix) {
        return endWith(str, suffix, true);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param str            被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     * @since 3.1.9
     */
    public static String strip(CharSequence str, CharSequence prefixOrSuffix) {
        return strip(str, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     * @since 3.1.9
     */
    public static String strip(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(str)) {
            return str(str);
        }
        int from = 0;
        int to = str.length();

        String str2 = str.toString();
        if (startWith(str2, prefix)) {
            from = prefix.length();
        }
        if (endWith(str2, suffix)) {
            to -= suffix.length();
        }
        return str2.substring(from, to);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param str            被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     * @since 3.1.9
     */
    public static String stripIgnoreCase(CharSequence str, CharSequence prefixOrSuffix) {
        return stripIgnoreCase(str, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     * @since 3.1.9
     */
    public static String stripIgnoreCase(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(str)) {
            return str(str);
        }
        int from = 0;
        int to = str.length();

        String str2 = str.toString();
        if (startWithIgnoreCase(str2, prefix)) {
            from = prefix.length();
        }
        if (endWithIgnoreCase(str2, suffix)) {
            to -= suffix.length();
        }
        return str2.substring(from, to);
    }

    /**
     * 如果给定字符串不是以prefix开头的,在开头补充 prefix
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 补充后的字符串
     */
    public static String addPrefixIfNot(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str(str);
        }

        final String str2 = str.toString();
        final String prefix2 = prefix.toString();
        if (false == str2.startsWith(prefix2)) {
            return prefix2.concat(str2);
        }
        return str2;
    }

    /**
     * 如果给定字符串不是以suffix结尾的,在尾部补充 suffix
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 补充后的字符串
     */
    public static String addSuffixIfNot(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str(str);
        }

        final String str2 = str.toString();
        final String suffix2 = suffix.toString();
        if (false == str2.endsWith(suffix2)) {
            return str2.concat(suffix2);
        }
        return str2;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     * @since 5.2.5
     */
    public static boolean containsAny(CharSequence str, CharSequence... testStrs) {
        return null != getContainsStr(str, testStrs);
    }

    /**
     * 查找指定字符串是否包含指定字符列表中的任意一个字符
     *
     * @param str       指定字符串
     * @param testChars 需要检查的字符数组
     * @return 是否包含任意一个字符
     */
    public static boolean containsAny(CharSequence str, char... testChars) {
        if (false == isEmpty(str)) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (ArrayUtils.contains(testChars, str.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查指定字符串中是否只包含给定的字符
     *
     * @param str       字符串
     * @param testChars 检查的字符
     * @return 字符串含有非检查的字符, 返回false
     */
    public static boolean containsOnly(CharSequence str, char... testChars) {
        if (false == isEmpty(str)) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (false == ArrayUtils.contains(testChars, str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 给定字符串是否包含空白符（空白符包括空格、制表符、全角空格和不间断空格）
     * 如果给定字符串为null或者"",则返回false
     *
     * @param str 字符串
     * @return 是否包含空白符
     */
    public static boolean containsBlank(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int length = str.length();
        if (0 == length) {
            return false;
        }

        for (int i = 0; i < length; i += 1) {
            if (CharUtils.isBlankChar(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 被包含的第一个字符串
     * @since 5.2.5
     */
    public static String getContainsStr(CharSequence str, CharSequence... testStrs) {
        if (isEmpty(str) || ArrayUtils.isEmpty(testStrs)) {
            return null;
        }
        for (CharSequence checkStr : testStrs) {
            if (str.toString().contains(checkStr)) {
                return checkStr.toString();
            }
        }
        return null;
    }

    /**
     * 是否包含特定字符,忽略大小写,如果给定两个参数都为<code>null</code>,返回true
     *
     * @param str     被检测字符串
     * @param testStr 被测试是否包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(CharSequence str, CharSequence testStr) {
        if (null == str) {
            // 如果被监测字符串和
            return null == testStr;
        }
        return str.toString().toLowerCase().contains(testStr.toString().toLowerCase());
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     * 忽略大小写
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     * @since 5.2.5
     */
    public static boolean containsAnyIgnoreCase(CharSequence str, CharSequence... testStrs) {
        return null != getContainsStrIgnoreCase(str, testStrs);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     * 忽略大小写
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 被包含的第一个字符串
     * @since 5.2.5
     */
    public static String getContainsStrIgnoreCase(CharSequence str, CharSequence... testStrs) {
        if (isEmpty(str) || ArrayUtils.isEmpty(testStrs)) {
            return null;
        }
        for (CharSequence testStr : testStrs) {
            if (containsIgnoreCase(str, testStr)) {
                return testStr.toString();
            }
        }
        return null;
    }


    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串（null或""）或者分隔字符串为null,返回原字符串
     * 如果分隔字符串为空串"",则返回空串,如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * StringUtils.subBefore(null, *)      = null
     * StringUtils.subBefore("", *)        = ""
     * StringUtils.subBefore("abc", "a")   = ""
     * StringUtils.subBefore("abcba", "b") = "a"
     * StringUtils.subBefore("abc", "c")   = "ab"
     * StringUtils.subBefore("abc", "d")   = "abc"
     * StringUtils.subBefore("abc", "")    = ""
     * StringUtils.subBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个）,true为选取最后一个
     * @return 切割后的字符串
     * @since 3.1.1
     */
    public static String subBefore(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string) || separator == null) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return Normal.EMPTY;
        }
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos) {
            return str;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串（null或""）或者分隔字符串为null,返回原字符串
     * 如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * StringUtils.subBefore(null, *)      = null
     * StringUtils.subBefore("", *)        = ""
     * StringUtils.subBefore("abc", 'a')   = ""
     * StringUtils.subBefore("abcba", 'b') = "a"
     * StringUtils.subBefore("abc", 'c')   = "ab"
     * StringUtils.subBefore("abc", 'd')   = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个）,true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(separator) : str.indexOf(separator);
        if (INDEX_NOT_FOUND == pos) {
            return str;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串（null或""）,返回原字符串
     * 如果分隔字符串为空串（null或""）,则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * StringUtils.subAfter(null, *)      = null
     * StringUtils.subAfter("", *)        = ""
     * StringUtils.subAfter(*, null)      = ""
     * StringUtils.subAfter("abc", "a")   = "bc"
     * StringUtils.subAfter("abcba", "b") = "cba"
     * StringUtils.subAfter("abc", "c")   = ""
     * StringUtils.subAfter("abc", "d")   = ""
     * StringUtils.subAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个）,true为选取最后一个
     * @return 切割后的字符串
     * @since 3.1.1
     */
    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }
        if (separator == null) {
            return Normal.EMPTY;
        }
        final String str = string.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos || (string.length() - 1) == pos) {
            return Normal.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串（null或""）,返回原字符串
     * 如果分隔字符串为空串（null或""）,则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * StringUtils.subAfter(null, *)      = null
     * StringUtils.subAfter("", *)        = ""
     * StringUtils.subAfter("abc", 'a')   = "bc"
     * StringUtils.subAfter("abcba", 'b') = "cba"
     * StringUtils.subAfter("abc", 'c')   = ""
     * StringUtils.subAfter("abc", 'd')   = ""
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个）,true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }
        final String str = string.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(separator) : str.indexOf(separator);
        if (INDEX_NOT_FOUND == pos) {
            return Normal.EMPTY;
        }
        return str.substring(pos + 1);
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     * <p>
     * 栗子：
     *
     * <pre>
     * StringUtils.subBetween("wx[b]yz", "[", "]") = "b"
     * StringUtils.subBetween(null, *, *)          = null
     * StringUtils.subBetween(*, null, *)          = null
     * StringUtils.subBetween(*, *, null)          = null
     * StringUtils.subBetween("", "", "")          = ""
     * StringUtils.subBetween("", "", "]")         = null
     * StringUtils.subBetween("", "[", "]")        = null
     * StringUtils.subBetween("yabcz", "", "")     = ""
     * StringUtils.subBetween("yabcz", "y", "z")   = "abc"
     * StringUtils.subBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     *
     * @param str    被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     * @since 3.1.1
     */
    public static String subBetween(CharSequence str, CharSequence before, CharSequence after) {
        if (str == null || before == null || after == null) {
            return null;
        }

        final String str2 = str.toString();
        final String before2 = before.toString();
        final String after2 = after.toString();

        final int start = str2.indexOf(before2);
        if (start != INDEX_NOT_FOUND) {
            final int end = str2.indexOf(after2, start + before2.length());
            if (end != INDEX_NOT_FOUND) {
                return str2.substring(start + before2.length(), end);
            }
        }
        return null;
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     * <p>
     * 栗子：
     *
     * <pre>
     * StringUtils.subBetween(null, *)            = null
     * StringUtils.subBetween("", "")             = ""
     * StringUtils.subBetween("", "tag")          = null
     * StringUtils.subBetween("tagabctag", null)  = null
     * StringUtils.subBetween("tagabctag", "")    = ""
     * StringUtils.subBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param str            被切割的字符串
     * @param beforeAndAfter 截取开始和结束的字符串标识
     * @return 截取后的字符串
     * @since 3.1.1
     */
    public static String subBetween(CharSequence str, CharSequence beforeAndAfter) {
        return subBetween(str, beforeAndAfter, beforeAndAfter);
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isBlank(str)) {
            return false;
        }
        if (str.length() < (prefix.length() + suffix.length())) {
            return false;
        }

        final String str2 = str.toString();
        return str2.startsWith(prefix.toString()) && str2.endsWith(suffix.toString());
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence str, char prefix, char suffix) {
        if (isBlank(str)) {
            return false;
        }
        if (str.length() < 2) {
            return false;
        }

        return str.charAt(0) == prefix && str.charAt(str.length() - 1) == suffix;
    }

    // 转换Class数组成字符串格式
    public static String toString(Class<?>[] parameterTypes) {
        if (ArrayUtils.isEmpty(parameterTypes)) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Class<?> clazz : parameterTypes) {
            builder.append("," + clazz.getCanonicalName());
        }

        String parameter = builder.toString().trim();
        if (parameter.length() > 0) {
            return parameter.substring(1);
        }

        return "";
    }

    // 转换String数组成字符串格式
    public static String toString(String[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append("," + value);
        }

        String parameter = builder.toString().trim();
        if (parameter.length() > 0) {
            return parameter.substring(1);
        }

        return "";
    }

    /**
     * Checks if any of the CharSequences are empty ("") or null or whitespace only.
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     *
     * <pre>
     * StringUtils.isAnyBlank((String) null)    = true
     * StringUtils.isAnyBlank((String[]) null)  = false
     * StringUtils.isAnyBlank(null, "foo")      = true
     * StringUtils.isAnyBlank(null, null)       = true
     * StringUtils.isAnyBlank("", "bar")        = true
     * StringUtils.isAnyBlank("bob", "")        = true
     * StringUtils.isAnyBlank("  bob  ", null)  = true
     * StringUtils.isAnyBlank(" ", "bar")       = true
     * StringUtils.isAnyBlank(new String[] {})  = false
     * StringUtils.isAnyBlank(new String[]{""}) = true
     * StringUtils.isAnyBlank("foo", "bar")     = false
     * </pre>
     *
     * @param css the CharSequences to check, may be null or empty
     * @return {@code true} if any of the CharSequences are empty or null or whitespace only
     * @since 3.2.0
     */
    public static boolean isAnyBlank(final CharSequence... css) {
        if (ArrayUtils.isEmpty(css)) {
            return false;
        }
        for (final CharSequence cs : css) {
            if (isBlank(cs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes control characters (char &lt;= 32) from both
     * ends of this String returning {@code null} if the String is
     * empty ("") after the trim or if it is {@code null}.
     *
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String,
     * {@code null} if only chars &lt;= 32, empty or null String input
     * @since 2.0.0
     */
    public static String trimToNull(final String str) {
        final String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * Removes control characters (char &lt;= 32) from both
     * ends of this String returning an empty String ("") if the String
     * is empty ("") after the trim or if it is {@code null}.
     *
     * <pre>
     * StringUtils.trimToEmpty(null)          = ""
     * StringUtils.trimToEmpty("")            = ""
     * StringUtils.trimToEmpty("     ")       = ""
     * StringUtils.trimToEmpty("abc")         = "abc"
     * StringUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String, or an empty String if {@code null} input
     * @since 2.0.0
     */
    public static String trimToEmpty(final String str) {
        return str == null ? Normal.EMPTY : str.trim();
    }

    public static String replaceBlank(String str) {
        String val = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            val = m.replaceAll("");
        }
        return val;
    }

    /**
     * Returns either the passed in String, or if the String is
     * {@code null}, the value of {@code defaultStr}.
     *
     * <pre>
     * StringUtils.defaultString(null, "NULL")  = "NULL"
     * StringUtils.defaultString("", "NULL")    = ""
     * StringUtils.defaultString("bat", "NULL") = "bat"
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param defaultStr the default String to return
     *                   if the input is {@code null}, may be null
     * @return the passed in String, or the default if it was {@code null}
     * @see String#valueOf(Object)
     */
    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Copy the given {@code Collection} into a {@code String} array.
     * The {@code Collection} must contain {@code String} elements only.
     *
     * @param collection the {@code Collection} to copy
     * @return the {@code String} array
     */
    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

    /**
     * Copy the given Enumeration into a {@code String} array.
     * The Enumeration must contain {@code String} elements only.
     *
     * @param enumeration the Enumeration to copy
     * @return the {@code String} array
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return toStringArray(Collections.list(enumeration));
    }

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Right pad a String with a specified String.
     * <p>
     * The String is padded to the size of {@code size}.
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)      = null
     * StringUtils.rightPad("", 3, "z")      = "zzz"
     * StringUtils.rightPad("bat", 3, "yz")  = "bat"
     * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
     * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringUtils.rightPad("bat", 1, "yz")  = "bat"
     * StringUtils.rightPad("bat", -1, "yz") = "bat"
     * StringUtils.rightPad("bat", 5, null)  = "bat  "
     * StringUtils.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }


    /**
     * Right pad a String with a specified character.
     * <p>
     * The String is padded to the size of {@code size}.
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)     = null
     * StringUtils.rightPad("", 3, 'z')     = "zzz"
     * StringUtils.rightPad("bat", 3, 'z')  = "bat"
     * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
     * StringUtils.rightPad("bat", 1, 'z')  = "bat"
     * StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0.0
     */
    public static String rightPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
    }

    /**
     * Gets the leftmost {@code len} characters of a String.
     * <p>
     * If {@code len} characters are not available, or the
     * String is {@code null}, the String will be returned without
     * an exception. An empty String is returned if len is negative.
     *
     * <pre>
     * StringUtils.left(null, *)    = null
     * StringUtils.left(*, -ve)     = ""
     * StringUtils.left("", *)      = ""
     * StringUtils.left("abc", 0)   = ""
     * StringUtils.left("abc", 2)   = "ab"
     * StringUtils.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the leftmost characters from, may be null
     * @param len the length of the required String
     * @return the leftmost characters, {@code null} if null String input
     */
    public static String left(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    /**
     * Gets the rightmost {@code len} characters of a String.
     * <p>
     * If {@code len} characters are not available, or the String
     * is {@code null}, the String will be returned without an
     * an exception. An empty String is returned if len is negative.
     *
     * <pre>
     * StringUtils.right(null, *)    = null
     * StringUtils.right(*, -ve)     = ""
     * StringUtils.right("", *)      = ""
     * StringUtils.right("abc", 0)   = ""
     * StringUtils.right("abc", 2)   = "bc"
     * StringUtils.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the rightmost characters from, may be null
     * @param len the length of the required String
     * @return the rightmost characters, {@code null} if null String input
     */
    public static String right(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    /**
     * Gets {@code len} characters from the middle of a String.
     * <p>
     * If {@code len} characters are not available, the remainder
     * of the String will be returned without an exception. If the
     * String is {@code null}, {@code null} will be returned.
     * An empty String is returned if len is negative or exceeds the
     * length of {@code str}.
     *
     * <pre>
     * StringUtils.mid(null, *, *)    = null
     * StringUtils.mid(*, *, -ve)     = ""
     * StringUtils.mid("", 0, *)      = ""
     * StringUtils.mid("abc", 0, 2)   = "ab"
     * StringUtils.mid("abc", 0, 4)   = "abc"
     * StringUtils.mid("abc", 2, 4)   = "c"
     * StringUtils.mid("abc", 4, 2)   = ""
     * StringUtils.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param str the String to get the characters from, may be null
     * @param pos the position to start from, negative treated as zero
     * @param len the length of the required String
     * @return the middle characters, {@code null} if null String input
     */
    public static String mid(final String str, int pos, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return Normal.EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= pos + len) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringUtils.leftPad(null, *)   = null
     * StringUtils.leftPad("", 3)     = "   "
     * StringUtils.leftPad("bat", 3)  = "bat"
     * StringUtils.leftPad("bat", 5)  = "  bat"
     * StringUtils.leftPad("bat", 1)  = "bat"
     * StringUtils.leftPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size the size to pad to
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String leftPad(final String str, final int size) {
        return leftPad(str, size, ' ');
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0.0
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    /**
     * 获取字符串的长度,如果为null返回0
     *
     * @param cs a 字符串
     * @return 字符串的长度, 如果为null返回0
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * 构建新的字符串
     *
     * @param original     原始对象
     * @param middle       中间隐藏信息
     * @param prefixLength 前边信息长度
     * @return 构建后的新字符串
     */
    public static String buildString(final Object original,
                                     final String middle,
                                     final int prefixLength) {
        if (ObjectUtils.isNull(original)) {
            return null;
        }

        final String string = original.toString();
        final int stringLength = string.length();

        String prefix = "";
        String suffix = "";

        if (stringLength >= prefixLength) {
            prefix = string.substring(0, prefixLength);
        } else {
            prefix = string.substring(0, stringLength);
        }

        int suffixLength = stringLength - prefix.length() - middle.length();
        if (suffixLength > 0) {
            suffix = string.substring(stringLength - suffixLength);
        }

        return prefix + middle + suffix;
    }

    /**
     * 连接多个字符串为一个
     *
     * @param isNullToEmpty 是否null转为""
     * @param strs          字符串数组
     * @return 连接后的字符串
     */
    public static String concat(boolean isNullToEmpty, CharSequence... strs) {
        final StrBuilder sb = new StrBuilder();
        for (CharSequence str : strs) {
            sb.append(isNullToEmpty ? nullToEmpty(str) : str);
        }
        return sb.toString();
    }

    /**
     * 给定字符串中的字母是否全部为大写,判断依据如下：
     *
     * <pre>
     * 1. 大写字母包括A-Z
     * 2. 其它非字母的Unicode符都算作大写
     * </pre>
     *
     * @param str 被检查的字符串
     * @return 是否全部为大写
     */
    public static boolean isUpperCase(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (Character.isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串中的字母是否全部为小写,判断依据如下：
     *
     * <pre>
     * 1. 小写字母包括a-z
     * 2. 其它非字母的Unicode符都算作小写
     * </pre>
     *
     * @param str 被检查的字符串
     * @return 是否全部为小写
     */
    public static boolean isLowerCase(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串转为bytes后的byte数（byte长度）
     *
     * @param cs      字符串
     * @param charset 编码
     * @return byte长度
     */
    public static int byteLength(CharSequence cs, Charset charset) {
        return cs == null ? 0 : cs.toString().getBytes(charset).length;
    }

    /**
     * 切换给定字符串中的大小写 大写转小写,小写转大写
     *
     * <pre>
     * StringUtils.swapCase(null)                 = null
     * StringUtils.swapCase("")                   = ""
     * StringUtils.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param str 字符串
     * @return 交换后的字符串
     */
    public static String swapCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }

        final char[] buffer = str.toCharArray();

        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (Character.isUpperCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
            } else if (Character.isTitleCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                buffer[i] = Character.toUpperCase(ch);
            }
        }
        return new String(buffer);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     * 字符填充于字符串前
     *
     * @param str        被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @return 填充后的字符串
     * @since 3.1.9
     */
    public static String fillBefore(String str, char filledChar, int len) {
        return fill(str, filledChar, len, true);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     * 字符填充于字符串后
     *
     * @param strVal  被填充的字符串
     * @param charVal 填充的字符
     * @param len     填充长度
     * @return 填充后的字符串
     * @since 3.1.9
     */
    public static String fillAfter(String strVal, char charVal, int len) {
        return fill(strVal, charVal, len, false);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     *
     * @param strVal  被填充的字符串
     * @param charVal 填充的字符
     * @param len     填充长度
     * @param isPre   是否填充在前
     * @return 填充后的字符串
     * @since 3.1.9
     */
    public static String fill(String strVal, char charVal, int len, boolean isPre) {
        final int strLen = strVal.length();
        if (strLen > len) {
            return strVal;
        }

        String filled = repeat(charVal, len - strLen);
        return isPre ? filled.concat(strVal) : strVal.concat(filled);
    }

    /**
     * 输出指定长度字符
     *
     * @param count   长度
     * @param charVal 字符
     * @return 填充后的字符串
     */
    public static String fill(int count, char charVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal 0.");
        }
        char[] chs = new char[count];
        for (int i = 0; i < count; i++) {
            chs[i] = charVal;
        }
        return new String(chs);
    }

    /**
     * 输出指定长度字符
     *
     * @param count  长度
     * @param strVal 字符
     * @return 填充后的字符串
     */
    public static String fill(int count, String strVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal 0.");
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(strVal);
        }
        return sb.toString();
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * 创建StrBuilder对象
     *
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder() {
        return new StrBuilder();
    }

    /**
     * 创建StringBuilder对象
     *
     * @param capacity 初始大小
     * @return StringBuilder对象
     */
    public static StringBuilder builder(int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * 创建StrBuilder对象
     *
     * @param capacity 初始大小
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder(int capacity) {
        return new StrBuilder(capacity);
    }

    /**
     * 创建StringBuilder对象
     *
     * @param strs 初始字符串列表
     * @return StringBuilder对象
     */
    public static StringBuilder builder(CharSequence... strs) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence str : strs) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 创建StrBuilder对象
     *
     * @param strs 初始字符串列表
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder(CharSequence... strs) {
        return new StrBuilder(strs);
    }

    /**
     * 获得StringReader
     *
     * @param str 字符串
     * @return StringReader
     */
    public static StringReader getReader(CharSequence str) {
        if (null == str) {
            return null;
        }
        return new StringReader(str.toString());
    }

    /**
     * 获得StringWriter
     *
     * @return StringWriter
     */
    public static StringWriter getWriter() {
        return new StringWriter();
    }

    /**
     * 统计指定内容中包含指定字符串的数量
     * 参数为 {@code null} 或者 "" 返回 {@code 0}.
     *
     * <pre>
     * StringUtils.count(null, *)       = 0
     * StringUtils.count("", *)         = 0
     * StringUtils.count("abba", null)  = 0
     * StringUtils.count("abba", "")    = 0
     * StringUtils.count("abba", "a")   = 2
     * StringUtils.count("abba", "ab")  = 1
     * StringUtils.count("abba", "xxx") = 0
     * </pre>
     *
     * @param content      被查找的字符串
     * @param strForSearch 需要查找的字符串
     * @return 查找到的个数
     */
    public static int count(CharSequence content, CharSequence strForSearch) {
        if (hasEmpty(content, strForSearch) || strForSearch.length() > content.length()) {
            return 0;
        }

        int count = 0;
        int idx = 0;
        final String content2 = content.toString();
        final String strForSearch2 = strForSearch.toString();
        while ((idx = content2.indexOf(strForSearch2, idx)) > -1) {
            count++;
            idx += strForSearch.length();
        }
        return count;
    }

    /**
     * 统计指定内容中包含指定字符的数量
     *
     * @param content       内容
     * @param charForSearch 被统计的字符
     * @return 包含数量
     */
    public static int count(CharSequence content, char charForSearch) {
        int count = 0;
        if (isEmpty(content)) {
            return 0;
        }
        int contentLength = content.length();
        for (int i = 0; i < contentLength; i++) {
            if (charForSearch == content.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将字符串切分为N等份
     *
     * @param str        字符串
     * @param partLength 每等份的长度
     * @return 切分后的数组
     * @since 3.1.9
     */
    public static String[] cut(CharSequence str, int partLength) {
        if (null == str) {
            return null;
        }
        int len = str.length();
        if (len < partLength) {
            return new String[]{str.toString()};
        }
        int part = NumberUtils.count(len, partLength);
        final String[] array = new String[part];

        final String str2 = str.toString();
        for (int i = 0; i < part; i++) {
            array[i] = str2.substring(i * partLength, (i == part - 1) ? len : (partLength + i * partLength));
        }
        return array;
    }

    /**
     * 将给定字符串,变成 "xxx...xxx" 形式的字符串
     *
     * @param str       字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String brief(CharSequence str, int maxLength) {
        if (null == str) {
            return null;
        }
        if ((str.length() + 3) <= maxLength) {
            return str.toString();
        }
        int w = maxLength / 2;
        int l = str.length();

        final String str2 = str.toString();
        return format("{}...{}", str2.substring(0, maxLength - w), str2.substring(l - w));
    }

    /**
     * 是否包含空字符串
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... strs) {
        if (ArrayUtils.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 首字母变小写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToLower(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToUpper(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 清理字符串,清理出某些不可见字符和一些sql特殊字符
     *
     * @param txt 文本
     * @return {String}
     */
    public static String cleanText(String txt) {
        if (txt == null) {
            return null;
        }
        return Pattern.compile("[`'\"|/,;()-+*%#·•�　\\s]").matcher(txt).replaceAll(Normal.EMPTY);
    }

    /**
     * 获取标识符,用于参数清理
     *
     * @param param 参数
     * @return 清理后的标识符
     */
    public static String cleanIdentifier(String param) {
        if (param == null) {
            return null;
        }
        StringBuilder paramBuilder = new StringBuilder();
        for (int i = 0; i < param.length(); i++) {
            char c = param.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                paramBuilder.append(c);
            }
        }
        return paramBuilder.toString();
    }

}
