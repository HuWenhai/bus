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

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.getter.BasicType;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.CollUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * 字典对象,扩充了HashMap中的方法
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Dict extends LinkedHashMap<String, Object> implements BasicType<String> {

    private static final long serialVersionUID = 6135423866861206530L;

    /**
     * 构造
     */
    public Dict() {
        super();
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     */
    public Dict(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始容量
     * @param loadFactor      容量增长因子,0~1,即达到容量的百分之多少时扩容
     */
    public Dict(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public Dict(Map<String, Object> m) {
        super((null == m) ? new HashMap<String, Object>() : m);
    }

    /**
     * 创建Dict
     *
     * @return Dict
     */
    public static Dict create() {
        return new Dict();
    }

    /**
     * 将PO对象转为Dict
     *
     * @param <T>  Bean类型
     * @param bean Bean对象
     * @return Vo
     */
    public static <T> Dict parse(T bean) {
        return create().parseBean(bean);
    }

    /**
     * 转换为Bean对象
     *
     * @param <T>  Bean类型
     * @param bean Bean
     * @return Bean
     */
    public <T> T toBean(T bean) {
        return toBean(bean, false);
    }

    /**
     * 转换为Bean对象
     *
     * @param <T>  Bean类型
     * @param bean Bean
     * @return Bean
     * @since 3.3.1
     */
    public <T> T toBeanIgnoreCase(T bean) {
        BeanUtils.fillBeanWithMapIgnoreCase(this, bean, false);
        return bean;
    }

    /**
     * 转换为Bean对象
     *
     * @param <T>           Bean类型
     * @param bean          Bean
     * @param isToCamelCase 是否转换为驼峰模式
     * @return Bean
     */
    public <T> T toBean(T bean, boolean isToCamelCase) {
        BeanUtils.fillBeanWithMap(this, bean, isToCamelCase, false);
        return bean;
    }

    /**
     * 转换为Bean对象,并使用驼峰法模式转换
     *
     * @param <T>  Bean类型
     * @param bean Bean
     * @return Bean
     */
    public <T> T toBeanWithCamelCase(T bean) {
        BeanUtils.fillBeanWithMap(this, bean, true, false);
        return bean;
    }

    /**
     * 填充Value Object对象
     *
     * @param <T>   Bean类型
     * @param clazz Value Object（或者POJO）的类
     * @return vo
     */
    public <T> T toBean(Class<T> clazz) {
        return BeanUtils.mapToBean(this, clazz, false);
    }

    /**
     * 填充Value Object对象,忽略大小写
     *
     * @param <T>   Bean类型
     * @param clazz Value Object（或者POJO）的类
     * @return vo
     */
    public <T> T toBeanIgnoreCase(Class<T> clazz) {
        return BeanUtils.mapToBeanIgnoreCase(this, clazz, false);
    }

    /**
     * 将值对象转换为Dict
     * 类名会被当作表名,小写第一个字母
     *
     * @param <T>  Bean类型
     * @param bean 值对象
     * @return 自己
     */
    public <T> Dict parseBean(T bean) {
        Assert.notNull(bean, "Bean class must be not null");
        this.putAll(BeanUtils.beanToMap(bean));
        return this;
    }

    /**
     * 将值对象转换为Dict
     * 类名会被当作表名,小写第一个字母
     *
     * @param <T>               Bean类型
     * @param bean              值对象
     * @param isToUnderlineCase 是否转换为下划线模式
     * @param ignoreNullValue   是否忽略值为空的字段
     * @return 自己
     */
    public <T> Dict parseBean(T bean, boolean isToUnderlineCase, boolean ignoreNullValue) {
        Assert.notNull(bean, "Bean class must be not null");
        this.putAll(BeanUtils.beanToMap(bean, isToUnderlineCase, ignoreNullValue));
        return this;
    }

    /**
     * 与给定实体对比并去除相同的部分
     * 此方法用于在更新操作时避免所有字段被更新,跳过不需要更新的字段 version from 2.0.0
     *
     * @param <T>          字典对象类型
     * @param dict         字典对象
     * @param withoutNames 不需要去除的字段名
     */
    public <T extends Dict> void removeEqual(T dict, String... withoutNames) {
        HashSet<String> withoutSet = CollUtils.newHashSet(withoutNames);
        for (Map.Entry<String, Object> entry : dict.entrySet()) {
            if (withoutSet.contains(entry.getKey())) {
                continue;
            }

            final Object value = this.get(entry.getKey());
            if (null != value && value.equals(entry.getValue())) {
                this.remove(entry.getKey());
            }
        }
    }

    /**
     * 过滤Map保留指定键值对,如果键不存在跳过
     *
     * @param keys 键列表
     * @return Dict 结果
     */
    public Dict filter(String... keys) {
        final Dict result = new Dict(keys.length, 1);

        for (String key : keys) {
            if (this.containsKey(key)) {
                result.put(key, this.get(key));
            }
        }
        return result;
    }

    /**
     * 设置列
     *
     * @param attr  属性
     * @param value 值
     * @return 本身
     */
    public Dict set(String attr, Object value) {
        this.put(attr, value);
        return this;
    }

    /**
     * 设置列,当键或值为null时忽略
     *
     * @param attr  属性
     * @param value 值
     * @return 本身
     */
    public Dict setIgnoreNull(String attr, Object value) {
        if (null != attr && null != value) {
            set(attr, value);
        }
        return this;
    }

    @Override
    public Object getObj(String key) {
        return super.get(key);
    }

    /**
     * 获得特定类型值
     *
     * @param <T>          值类型
     * @param attr         字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    public <T> T get(String attr, T defaultValue) {
        final Object result = get(attr);
        return (T) (result != null ? result : defaultValue);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public String getStr(String attr) {
        return Convert.toString(get(attr), null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public Integer getInt(String attr) {
        return Convert.toInt(get(attr), null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public Long getLong(String attr) {
        return Convert.toLong(get(attr), null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public Float getFloat(String attr) {
        return Convert.toFloat(get(attr), null);
    }

    @Override
    public Short getShort(String attr) {
        return Convert.toShort(get(attr), null);
    }

    @Override
    public Character getChar(String attr) {
        return Convert.toChar(get(attr), null);
    }

    @Override
    public Double getDouble(String attr) {
        return Convert.toDouble(get(attr), null);
    }

    @Override
    public Byte getByte(String attr) {
        return Convert.toByte(get(attr), null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public Boolean getBool(String attr) {
        return Convert.toBool(get(attr), null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public BigDecimal getBigDecimal(String attr) {
        return Convert.toBigDecimal(get(attr));
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    @Override
    public BigInteger getBigInteger(String attr) {
        return Convert.toBigInteger(get(attr));
    }

    @Override
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) {
        return Convert.toEnum(clazz, get(key));
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    public byte[] getBytes(String attr) {
        return get(attr, null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    public Date getDate(String attr) {
        return get(attr, null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    public Time getTime(String attr) {
        return get(attr, null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    public Timestamp getTimestamp(String attr) {
        return get(attr, null);
    }

    /**
     * @param attr 字段名
     * @return 字段值
     */
    public Number getNumber(String attr) {
        return get(attr, null);
    }

    @Override
    public Dict clone() {
        return (Dict) super.clone();
    }

}
