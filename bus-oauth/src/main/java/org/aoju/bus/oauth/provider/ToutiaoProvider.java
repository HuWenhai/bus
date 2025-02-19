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
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * 今日头条登录
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class ToutiaoProvider extends DefaultProvider {

    public ToutiaoProvider(Context context) {
        super(context, Registry.TOUTIAO);
    }

    public ToutiaoProvider(Context context, StateCache stateCache) {
        super(context, Registry.TOUTIAO, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        JSONObject object = JSONObject.parseObject(doGetAuthorizationCode(Callback.getCode()));

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .openId(object.getString("open_id"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));

        this.checkResponse(object);

        JSONObject user = object.getJSONObject("data");

        boolean isAnonymousUser = user.getIntValue("uid_type") == 14;
        String anonymousUserName = "匿名用户";

        return Property.builder()
                .uuid(user.getString("uid"))
                .username(isAnonymousUser ? anonymousUserName : user.getString("screen_name"))
                .nickname(isAnonymousUser ? anonymousUserName : user.getString("screen_name"))
                .avatar(user.getString("avatar_url"))
                .remark(user.getString("description"))
                .gender(Normal.Gender.getGender(user.getString("gender")))
                .token(token)
                .source(source.toString())
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_key", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("auth_only", 1)
                .queryParam("display", 0)
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_key", context.getClientId())
                .queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("client_key", context.getClientId())
                .queryParam("access_token", token.getAccessToken())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_code")) {
            throw new InstrumentException(Error.getErrorCode(object.getString("error_code")).getDesc());
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Error {
        /**
         * 0：正常；
         * other：调用异常,具体异常内容见{@code desc}
         */
        EC0("0", "接口调用成功"),
        EC1("1", "API配置错误,未传入Client Key"),
        EC2("2", "API配置错误,Client Key错误,请检查是否和开放平台的ClientKey一致"),
        EC3("3", "没有授权信息"),
        EC4("4", "响应类型错误"),
        EC5("5", "授权类型错误"),
        EC6("6", "client_secret错误"),
        EC7("7", "authorize_code过期"),
        EC8("8", "指定url的scheme不是https"),
        EC9("9", "接口内部错误,请联系头条技术"),
        EC10("10", "access_token过期"),
        EC11("11", "缺少access_token"),
        EC12("12", "参数缺失"),
        EC13("13", "url错误"),
        EC21("21", "域名与登记域名不匹配"),
        EC999("999", "未知错误,请联系头条技术");

        private String code;
        private String desc;

        public static Error getErrorCode(String errorCode) {
            Error[] errorCodes = Error.values();
            for (Error code : errorCodes) {
                if (code.getCode() == errorCode) {
                    return code;
                }
            }
            return EC999;
        }
    }

}
