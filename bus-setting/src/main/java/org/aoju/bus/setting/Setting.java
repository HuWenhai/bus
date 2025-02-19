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
package org.aoju.bus.setting;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.io.resource.ClassPathResource;
import org.aoju.bus.core.io.resource.FileResource;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.io.resource.UriResource;
import org.aoju.bus.core.io.watchers.SimpleWatcher;
import org.aoju.bus.core.io.watchers.WatchMonitor;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.ResourceUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.setting.dialect.Props;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.*;

/**
 * 设置工具类  用于支持设置（配置）文件
 * BasicSetting用于替换Properties类,提供功能更加强大的配置文件,同时对Properties文件向下兼容
 *
 * <pre>
 *  1、支持变量,默认变量命名为 ${变量名},变量只能识别读入行的变量,例如第6行的变量在第三行无法读取
 *  2、支持分组,分组为中括号括起来的内容,中括号以下的行都为此分组的内容,无分组相当于空字符分组,若某个key是name,加上分组后的键相当于group.name
 *  3、注释以#开头,但是空行和不带“=”的行也会被跳过,但是建议加#
 *  4、store方法不会保存注释内容,慎重使用
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Setting extends AbsSetting implements Map<String, String> {

    /**
     * 附带分组的键值对存储
     */
    private final GroupedMap groupedMap = new GroupedMap();

    /**
     * 本设置对象的字符集
     */
    protected Charset charset;
    /**
     * 是否使用变量
     */
    protected boolean isUseVariable;
    /**
     * 设定文件的URL
     */
    protected URL settingUrl;

    private SettingLoader settingLoader;
    private WatchMonitor watchMonitor;

    /**
     * 空构造
     */
    public Setting() {
    }

    /**
     * 构造
     *
     * @param path 相对路径或绝对路径
     */
    public Setting(String path) {
        this(path, false);
    }

    /**
     * 构造
     *
     * @param path          相对路径或绝对路径
     * @param isUseVariable 是否使用变量
     */
    public Setting(String path, boolean isUseVariable) {
        this(path, org.aoju.bus.core.consts.Charset.UTF_8, isUseVariable);
    }

    /**
     * 构造,使用相对于Class文件根目录的相对路径
     *
     * @param path          相对路径或绝对路径
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(String path, Charset charset, boolean isUseVariable) {
        Assert.notBlank(path, "Blank setting path !");
        this.init(ResourceUtils.getResourceObj(path), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param configFile    配置文件对象
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(File configFile, Charset charset, boolean isUseVariable) {
        Assert.notNull(configFile, "Null setting file define!");
        this.init(new FileResource(configFile), charset, isUseVariable);
    }

    /**
     * 构造,相对于classes读取文件
     *
     * @param path          相对ClassPath路径或绝对路径
     * @param clazz         基准类
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(String path, Class<?> clazz, Charset charset, boolean isUseVariable) {
        Assert.notBlank(path, "Blank setting path !");
        this.init(new ClassPathResource(path, clazz), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param url           设定文件的URL
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(URL url, Charset charset, boolean isUseVariable) {
        Assert.notNull(url, "Null setting url define!");
        this.init(new UriResource(url), charset, isUseVariable);
    }

    /**
     * 初始化设定文件
     *
     * @param resource      {@link Resource}
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     * @return 成功初始化与否
     */
    public boolean init(Resource resource, Charset charset, boolean isUseVariable) {
        if (resource == null) {
            throw new NullPointerException("Null setting url define!");
        }
        this.settingUrl = resource.getUrl();
        this.charset = charset;
        this.isUseVariable = isUseVariable;

        return load();
    }

    /**
     * 重新加载配置文件
     *
     * @return 是否加载成功
     */
    synchronized public boolean load() {
        if (null == this.settingLoader) {
            settingLoader = new SettingLoader(this.groupedMap, this.charset, this.isUseVariable);
        }
        return settingLoader.load(new UriResource(this.settingUrl));
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param autoReload 是否自动加载
     */
    public void autoLoad(boolean autoReload) {
        if (autoReload) {
            if (null != this.watchMonitor) {
                this.watchMonitor.close();
            }
            try {
                watchMonitor = WatchMonitor.create(this.settingUrl, StandardWatchEventKinds.ENTRY_MODIFY);
                watchMonitor.setWatcher(new SimpleWatcher() {
                    @Override
                    public void onModify(WatchEvent<?> event, Path currentPath) {
                        load();
                    }
                }).start();
            } catch (Exception e) {
                throw new InstrumentException("Setting auto load not support url: [{}]", this.settingUrl);
            }
        } else {
            this.watchMonitor = null;
        }
    }

    /**
     * @return 获得设定文件的路径
     */
    public String getSettingPath() {
        return (null == this.settingUrl) ? null : this.settingUrl.getPath();
    }

    /**
     * 键值总数
     *
     * @return 键值总数
     */
    public int size() {
        return this.groupedMap.size();
    }

    @Override
    public String getByGroup(String key, String group) {
        return this.groupedMap.get(group, key);
    }

    /**
     * 获取并删除键值对,当指定键对应值非空时,返回并删除这个值,后边的键对应的值不再查找
     *
     * @param keys 键列表,常用于别名
     * @return 值
     * @since 3.1.9
     */
    public Object getAndRemove(String... keys) {
        Object value = null;
        for (String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return value;
    }

    /**
     * 获取并删除键值对,当指定键对应值非空时,返回并删除这个值,后边的键对应的值不再查找
     *
     * @param keys 键列表,常用于别名
     * @return 字符串值
     * @since 3.1.9
     */
    public String getAndRemoveStr(String... keys) {
        Object value = null;
        for (String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return (String) value;
    }

    /**
     * 获得指定分组的所有键值对,此方法获取的是原始键值对,获取的键值对可以被修改
     *
     * @param group 分组
     * @return map
     */
    public Map<String, String> getMap(String group) {
        return this.groupedMap.get(group);
    }

    /**
     * 获得group对应的子Setting
     *
     * @param group 分组
     * @return {@link Setting}
     */
    public Setting getSetting(String group) {
        final Setting setting = new Setting();
        setting.putAll(this.getMap(group));
        return setting;
    }

    /**
     * 获得group对应的子Properties
     *
     * @param group 分组
     * @return Properties对象
     */
    public Properties getProperties(String group) {
        final Properties properties = new Properties();
        properties.putAll(getMap(group));
        return properties;
    }

    /**
     * 获得group对应的子Props
     *
     * @param group 分组
     * @return Props对象
     */
    public Props getProps(String group) {
        final Props props = new Props();
        props.putAll(getMap(group));
        return props;
    }

    /**
     * 持久化当前设置,会覆盖掉之前的设置
     * 持久化不会保留之前的分组
     *
     * @param absolutePath 设置文件的绝对路径
     */
    public void store(String absolutePath) {
        if (null == this.settingLoader) {
            settingLoader = new SettingLoader(this.groupedMap, this.charset, this.isUseVariable);
        }
        settingLoader.store(absolutePath);
    }

    /**
     * 转换为Properties对象,原分组变为前缀
     *
     * @return Properties对象
     */
    public Properties toProperties() {
        final Properties properties = new Properties();
        String group;
        for (Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupedMap.entrySet()) {
            group = groupEntry.getKey();
            for (Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                properties.setProperty(StringUtils.isEmpty(group) ? entry.getKey() : group + Symbol.DOT + entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }

    /**
     * 获取GroupedMap
     *
     * @return GroupedMap
     */
    public GroupedMap getGroupedMap() {
        return this.groupedMap;
    }

    /**
     * 获取所有分组
     *
     * @return 获得所有分组名
     */
    public List<String> getGroups() {
        return CollUtils.newArrayList(this.groupedMap.keySet());
    }

    /**
     * 设置变量的正则
     * 正则只能有一个group表示变量本身,剩余为字符 例如 \$\{(name)\}表示${name}变量名为name的一个变量表示
     *
     * @param regex 正则
     */
    public void setVarRegex(String regex) {
        if (null == this.settingLoader) {
            throw new NullPointerException("SettingLoader is null !");
        }
        this.settingLoader.setVarRegex(regex);
    }

    /**
     * 某个分组对应的键值对是否为空
     *
     * @param group 分组
     * @return 是否为空
     */
    public boolean isEmpty(String group) {
        return this.groupedMap.isEmpty(group);
    }

    /**
     * 指定分组中是否包含指定key
     *
     * @param group 分组
     * @param key   键
     * @return 是否包含key
     */
    public boolean containsKey(String group, String key) {
        return this.groupedMap.containsKey(group, key);
    }

    /**
     * 指定分组中是否包含指定值
     *
     * @param group 分组
     * @param value 值
     * @return 是否包含值
     */
    public boolean containsValue(String group, String value) {
        return this.groupedMap.containsValue(group, value);
    }

    /**
     * 获取分组对应的值,如果分组不存在或者值不存在则返回null
     *
     * @param group 分组
     * @param key   键
     * @return 值, 如果分组不存在或者值不存在则返回null
     */
    public String get(String group, String key) {
        return this.groupedMap.get(group, key);
    }

    /**
     * 将键值对加入到对应分组中
     *
     * @param group 分组
     * @param key   键
     * @param value 值
     * @return 此key之前存在的值, 如果没有返回null
     */
    public String put(String group, String key, String value) {
        return this.groupedMap.put(group, key, value);
    }

    /**
     * 从指定分组中删除指定值
     *
     * @param group 分组
     * @param key   键
     * @return 被删除的值, 如果值不存在, 返回null
     */
    public String remove(String group, Object key) {
        return this.groupedMap.remove(group, Convert.toString(key));
    }

    /**
     * 加入多个键值对到某个分组下
     *
     * @param group 分组
     * @param m     键值对
     * @return this
     */
    public Setting putAll(String group, Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(group, m);
        return this;
    }

    /**
     * 清除指定分组下的所有键值对
     *
     * @param group 分组
     * @return this
     */
    public Setting clear(String group) {
        this.groupedMap.clear(group);
        return this;
    }

    /**
     * 指定分组所有键的Set
     *
     * @param group 分组
     * @return 键Set
     */
    public Set<String> keySet(String group) {
        return this.groupedMap.keySet(group);
    }

    /**
     * 指定分组下所有值
     *
     * @param group 分组
     * @return 值
     */
    public Collection<String> values(String group) {
        return this.groupedMap.values(group);
    }

    /**
     * 指定分组下所有键值对
     *
     * @param group 分组
     * @return 键值对
     */
    public Set<Entry<String, String>> entrySet(String group) {
        return this.groupedMap.entrySet(group);
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     * @return this
     * @since 3.3.1
     */
    public Setting set(String key, String value) {
        this.groupedMap.put(Normal.EMPTY, key, value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return this.groupedMap.isEmpty();
    }

    /**
     * 默认分组（空分组）中是否包含指定key对应的值
     *
     * @param key 键
     * @return 默认分组中是否包含指定key对应的值
     */
    @Override
    public boolean containsKey(Object key) {
        return this.groupedMap.containsKey(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 默认分组（空分组）中是否包含指定值
     *
     * @param value 值
     * @return 默认分组中是否包含指定值
     */
    @Override
    public boolean containsValue(Object value) {
        return this.groupedMap.containsValue(Normal.EMPTY, Convert.toString(value));
    }

    /**
     * 获取默认分组（空分组）中指定key对应的值
     *
     * @param key 键
     * @return 默认分组（空分组）中指定key对应的值
     */
    @Override
    public String get(Object key) {
        return this.groupedMap.get(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 将指定键值对加入到默认分组（空分组）中
     *
     * @param key   键
     * @param value 值
     * @return 加入的值
     */
    @Override
    public String put(String key, String value) {
        return this.groupedMap.put(Normal.EMPTY, key, value);
    }

    /**
     * 移除默认分组（空分组）中指定值
     *
     * @param key 键
     * @return 移除的值
     */
    @Override
    public String remove(Object key) {
        return this.groupedMap.remove(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 将键值对Map加入默认分组（空分组）中
     *
     * @param m Map
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(Normal.EMPTY, m);
    }

    /**
     * 清空默认分组（空分组）中的所有键值对
     */
    @Override
    public void clear() {
        this.groupedMap.clear(Normal.EMPTY);
    }

    /**
     * 获取默认分组（空分组）中的所有键列表
     *
     * @return 默认分组（空分组）中的所有键列表
     */
    @Override
    public Set<String> keySet() {
        return this.groupedMap.keySet(Normal.EMPTY);
    }

    /**
     * 获取默认分组（空分组）中的所有值列表
     *
     * @return 默认分组（空分组）中的所有值列表
     */
    @Override
    public Collection<String> values() {
        return this.groupedMap.values(Normal.EMPTY);
    }

    /**
     * 获取默认分组（空分组）中的所有键值对列表
     *
     * @return 默认分组（空分组）中的所有键值对列表
     */
    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.groupedMap.entrySet(Normal.EMPTY);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + ((groupedMap == null) ? 0 : groupedMap.hashCode());
        result = prime * result + (isUseVariable ? 1231 : 1237);
        result = prime * result + ((settingUrl == null) ? 0 : settingUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Setting other = (Setting) obj;
        if (charset == null) {
            if (other.charset != null) {
                return false;
            }
        } else if (!charset.equals(other.charset)) {
            return false;
        }
        if (groupedMap == null) {
            if (other.groupedMap != null) {
                return false;
            }
        } else if (!groupedMap.equals(other.groupedMap)) {
            return false;
        }
        if (isUseVariable != other.isUseVariable) {
            return false;
        }
        if (settingUrl == null) {
            if (other.settingUrl != null) {
                return false;
            }
        } else if (!settingUrl.equals(other.settingUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return groupedMap.toString();
    }

}
