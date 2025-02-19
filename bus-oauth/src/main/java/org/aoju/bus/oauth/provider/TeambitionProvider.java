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
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Teambition授权登录
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class TeambitionProvider extends DefaultProvider {

    public TeambitionProvider(Context context) {
        super(context, Registry.TEAMBITION);
    }

    public TeambitionProvider(Context context, StateCache stateCache) {
        super(context, Registry.TEAMBITION, stateCache);
    }

    /**
     * @param Callback 回调返回的参数
     * @return 所有信息
     */
    @Override
    protected AccToken getAccessToken(Callback Callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", context.getClientId());
        params.put("client_secret", context.getClientSecret());
        params.put("code", Callback.getCode());
        params.put("grant_type", "code");

        String response = HttpClient.post(source.accessToken(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + token.getAccessToken());

        String response = HttpClient.post(source.userInfo(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        token.setUid(object.getString("_id"));

        return Property.builder()
                .uuid(object.getString("_id"))
                .username(object.getString("name"))
                .nickname(object.getString("name"))
                .avatar(object.getString("avatarUrl"))
                .blog(object.getString("website"))
                .location(object.getString("location"))
                .email(object.getString("email"))
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("_userId", oldToken.getUid());
        params.put("refresh_token", oldToken.getRefreshToken());

        String response = HttpClient.post(source.refresh(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .refreshToken(object.getString("refresh_token"))
                        .build())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if ((object.containsKey("message") && object.containsKey("name"))) {
            throw new InstrumentException(object.getString("name") + ", " + object.getString("message"));
        }
    }

}
