package org.aoju.bus.core.compare;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.CompareUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Bean属性排序器<br>
 * 支持读取Bean多层次下的属性
 *
 * @param <T> 被比较的Bean
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class PropertyCompare<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String property;
    private final boolean isNullGreater;

    /**
     * 构造
     *
     * @param property 属性名
     */
    public PropertyCompare(String property) {
        this(property, true);
    }

    /**
     * 构造
     *
     * @param property      属性名
     * @param isNullGreater null值是否排在后（从小到大排序）
     */
    public PropertyCompare(String property, boolean isNullGreater) {
        this.property = property;
        this.isNullGreater = isNullGreater;
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        java.lang.Comparable<?> v1;
        java.lang.Comparable<?> v2;
        try {
            v1 = (java.lang.Comparable<?>) BeanUtils.getProperty(o1, property);
            v2 = (java.lang.Comparable<?>) BeanUtils.getProperty(o2, property);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }

        return compare(o1, o2, v1, v2);
    }

    private int compare(T o1, T o2, java.lang.Comparable fieldValue1, java.lang.Comparable fieldValue2) {
        int result = ObjectUtils.compare(fieldValue1, fieldValue2, isNullGreater);
        if (0 == result) {
            result = CompareUtils.compare(o1, o2, this.isNullGreater);
        }
        return result;
    }

}
