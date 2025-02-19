package org.aoju.bus.core.map;

import java.util.LinkedHashMap;

/**
 * 固定大小的{@link LinkedHashMap} 实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class FixedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    /**
     * 容量,超过此容量自动删除末尾元素
     */
    private int capacity;

    /**
     * 构造
     *
     * @param capacity 容量,实际初始容量比容量大1
     */
    public FixedLinkedHashMap(int capacity) {
        super(capacity + 1, 1.0f, true);
        this.capacity = capacity;
    }

    /**
     * 获取容量
     *
     * @return 容量
     */
    public int getCapacity() {
        return this.capacity;
    }

    /**
     * 设置容量
     *
     * @param capacity 容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        //当链表元素大于容量时,移除最老（最久未被使用）的元素
        return size() > this.capacity;
    }

}
