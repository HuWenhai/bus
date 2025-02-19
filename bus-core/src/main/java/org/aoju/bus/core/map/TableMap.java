package org.aoju.bus.core.map;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.CollUtils;

import java.io.Serializable;
import java.util.*;


/**
 * 无重复键的Map
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class TableMap<K, V> implements Map<K, V>, Serializable {

    private List<K> keys;
    private List<V> values;

    /**
     * 构造
     *
     * @param size 初始容量
     */
    public TableMap(int size) {
        this.keys = new ArrayList<>(size);
        this.values = new ArrayList<>(size);
    }

    /**
     * 构造
     *
     * @param keys   键列表
     * @param values 值列表
     */
    public TableMap(K[] keys, V[] values) {
        this.keys = CollUtils.toList(keys);
        this.values = CollUtils.toList(values);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(keys);
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        final int index = keys.indexOf(key);
        if (index > -1 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        keys.add(key);
        values.add(value);
        return null;
    }

    @Override
    public V remove(Object key) {
        int index = keys.indexOf(key);
        if (index > -1) {
            keys.remove(index);
            if (index < values.size()) {
                values.remove(index);
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return new HashSet<>(values);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        HashSet<Map.Entry<K, V>> hashSet = new HashSet<>();
        for (int i = 0; i < size(); i++) {
            hashSet.add(new Entry<K, V>(keys.get(i), values.get(i)));
        }
        return hashSet;
    }

    private static class Entry<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("setValue not supported.");
        }

    }

}
