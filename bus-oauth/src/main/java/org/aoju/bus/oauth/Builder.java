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
package org.aoju.bus.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构造URL
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
@Setter
public class Builder {

    private final Map<String, Object> params = new LinkedHashMap<>(7);
    private String baseUrl;

    private Builder() {

    }

    /**
     * @param baseUrl 基础路径
     * @return the new {@code UrlBuilder}
     */
    public static Builder fromBaseUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * 如果给定字符串{@code str}中不包含{@code appendStr},则在{@code str}后追加{@code appendStr}；
     * 如果已包含{@code appendStr},则在{@code str}后追加{@code otherwise}
     *
     * @param str       给定的字符串
     * @param appendStr 需要追加的内容
     * @param otherwise 当{@code appendStr}不满足时追加到{@code str}后的内容
     * @return 追加后的字符串
     */
    public static String appendIfNotContain(String str, String appendStr, String otherwise) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(appendStr)) {
            return str;
        }
        if (str.contains(appendStr)) {
            return str.concat(otherwise);
        }
        return str.concat(appendStr);
    }

    /**
     * map转字符串,转换后的字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param params 待转换的map
     * @param encode 是否转码
     * @return str
     */
    public static String parseMapToString(Map<String, Object> params, boolean encode) {
        List<String> paramList = new ArrayList<>();
        params.forEach((k, v) -> {
            if (ObjectUtils.isNull(v)) {
                paramList.add(k + "=");
            } else {
                String valueString = v.toString();
                paramList.add(k + "=" + (encode ? encode(valueString) : valueString));
            }
        });
        return CollUtils.join(paramList, "&");
    }

    /**
     * 编码
     *
     * @param value str
     * @return encode str
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, org.aoju.bus.core.consts.Charset.UTF_8.displayName());
            return encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException("Failed To Encode Uri", e);
        }
    }

    /**
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this UrlBuilder
     */
    public Builder queryParam(String key, Object value) {
        Assert.notBlank(key, "参数名不能为空");
        String valueAsString = (value != null ? value.toString() : null);
        this.params.put(key, valueAsString);
        return this;
    }

    /**
     * 构造url
     *
     * @return url
     */
    public String build() {
        return this.build(false);
    }

    /**
     * 构造url
     *
     * @param encode 转码
     * @return url
     */
    public String build(boolean encode) {
        if (MapUtils.isEmpty(this.params)) {
            return this.baseUrl;
        }
        String baseUrl = appendIfNotContain(this.baseUrl, "?", "&");
        String paramString = parseMapToString(this.params, encode);
        return baseUrl + paramString;
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        /**
         * 2000：正常；
         * other：调用异常,具体异常内容见{@code msg}
         */
        SUCCESS("2000", "Success"),
        FAILURE("5000", "Failure"),
        NOT_IMPLEMENTED("5001", "Not Implemented"),
        PARAMETER_INCOMPLETE("5002", "Parameter incomplete"),
        UNSUPPORTED("5003", "Unsupported operation"),
        NO_AUTH_SOURCE("5004", "AuthDefaultSource cannot be null"),
        UNIDENTIFIED_PLATFORM("5005", "Unidentified platform"),
        ILLEGAL_REDIRECT_URI("5006", "Illegal redirect uri"),
        ILLEGAL_REQUEST("5007", "Illegal request"),
        ILLEGAL_CODE("5008", "Illegal code");

        private String code;
        private String msg;

    }

    /**
     * 缓存类型
     */
    @Getter
    @ToString
    public enum Type {
        /**
         * 使用JustAuth内置的缓存
         */
        DEFAULT,
        /**
         * 使用Redis缓存
         */
        REDIS,
        /**
         * 自定义缓存
         */
        CUSTOM
    }

    @Getter
    @Setter
    @lombok.Builder
    public static class Token {

        private int expireIn;
        private String accessToken;
        private String refreshToken;
        private String uid;
        private String openId;
        private String accessCode;
        private String unionId;

        /**
         * Google附带属性
         */
        private String scope;
        private String tokenType;
        private String idToken;

        /**
         * 小米附带属性
         */
        private String macAlgorithm;
        private String macKey;

        /**
         * 企业微信附带属性
         */
        private String code;

    }

}
