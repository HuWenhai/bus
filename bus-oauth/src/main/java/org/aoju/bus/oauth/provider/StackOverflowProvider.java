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
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * Stack Overflow登录
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class StackOverflowProvider extends DefaultProvider {

    public StackOverflowProvider(Context context) {
        super(context, Registry.STACK);
    }

    public StackOverflowProvider(Context context, StateCache stateCache) {
        super(context, Registry.STACK, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        String accessTokenUrl = accessTokenUrl(Callback.getCode());
        String response = HttpClient.post(source.accessToken(), parseQueryToMap(accessTokenUrl));
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String userInfoUrl = Builder.fromBaseUrl(this.source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("site", "stackoverflow")
                .queryParam("key", this.context.getStackOverflowKey())
                .build();
        JSONObject object = JSONObject.parseObject(HttpClient.get(userInfoUrl));
        this.checkResponse(object);
        JSONObject userObj = object.getJSONArray("items").getJSONObject(0);

        return Property.builder()
                .uuid(userObj.getString("user_id"))
                .avatar(userObj.getString("profile_image"))
                .location(userObj.getString("location"))
                .nickname(userObj.getString("display_name"))
                .blog(userObj.getString("website_url"))
                .gender(Normal.Gender.UNKNOWN)
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
                .queryParam("client_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", "read_inbox")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new InstrumentException(object.getString("error_description"));
        }
    }

}
