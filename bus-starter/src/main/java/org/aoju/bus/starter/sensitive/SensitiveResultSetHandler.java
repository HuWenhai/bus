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
package org.aoju.bus.starter.sensitive;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Builder;
import org.aoju.bus.sensitive.annotation.Privacy;
import org.aoju.bus.sensitive.annotation.Sensitive;
import org.aoju.bus.starter.SpringAware;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 数据解密脱敏
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {java.sql.Statement.class})})
public class SensitiveResultSetHandler implements Interceptor {

    private static final String MAPPED_STATEMENT = "mappedStatement";

    /**
     * 获得真正的处理对象,可能多层代理.
     *
     * @param <T>    泛型
     * @param target 对象
     * @return the object
     */
    private static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("hi.target"));
        }
        return (T) target;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final List<Object> results = (List<Object>) invocation.proceed();

        if (results.isEmpty()) {
            return results;
        }

        SensitiveProperties properties = SpringAware.getBean(SensitiveProperties.class);
        if (ObjectUtils.isNotEmpty(properties) && !properties.isDebug()) {
            final ResultSetHandler statementHandler = realTarget(invocation.getTarget());
            final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            final MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT);
            final ResultMap resultMap = mappedStatement.getResultMaps().isEmpty() ? null : mappedStatement.getResultMaps().get(0);

            Sensitive sensitive = results.get(0).getClass().getAnnotation(Sensitive.class);
            if (ObjectUtils.isEmpty(sensitive)) {
                return results;
            }

            final Map<String, Privacy> privacyMap = getSensitiveByResultMap(resultMap);
            for (Object obj : results) {
                // 数据解密
                if (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value())
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
                    final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
                    for (Map.Entry<String, Privacy> entry : privacyMap.entrySet()) {
                        Privacy privacy = entry.getValue();
                        if (ObjectUtils.isNotEmpty(privacy) && StringUtils.isNotEmpty(privacy.value())) {
                            if (Builder.ALL.equals(privacy.value()) || Builder.OUT.equals(privacy.value())) {
                                String property = entry.getKey();
                                String value = (String) objMetaObject.getValue(property);
                                if (StringUtils.isNotEmpty(value)) {
                                    if (ObjectUtils.isEmpty(properties)) {
                                        throw new InstrumentException("please check the request.crypto.decrypt");
                                    }
                                    String decryptValue = org.aoju.bus.crypto.Builder.decrypt(properties.getDecrypt().getType(), properties.getDecrypt().getKey(), value, Charset.UTF_8);
                                    objMetaObject.setValue(property, decryptValue);
                                }
                            }
                        }
                    }
                }
                // 数据脱敏
                if ((Builder.ALL.equals(sensitive.value()) || Builder.SENS.equals(sensitive.value()))
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
                    Builder.on(obj);
                }
            }
        }
        return results;
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private Map<String, Privacy> getSensitiveByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }
        return getSensitiveByType(resultMap.getType());
    }

    private Map<String, Privacy> getSensitiveByType(Class<?> clazz) {
        Map<String, Privacy> sensitiveFieldMap = new HashMap<>(16);
        for (Field field : clazz.getDeclaredFields()) {
            Privacy sensitiveField = field.getAnnotation(Privacy.class);
            if (sensitiveField != null) {
                sensitiveFieldMap.put(field.getName(), sensitiveField);
            }
        }
        return sensitiveFieldMap;
    }

}
