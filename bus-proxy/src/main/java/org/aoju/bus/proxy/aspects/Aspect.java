package org.aoju.bus.proxy.aspects;

import java.lang.reflect.Method;

/**
 * 切面接口
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public interface Aspect {

    /**
     * 目标方法执行前的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @return 是否继续执行接下来的操作
     */
    boolean before(Object target, Method method, Object[] args);

    /**
     * 目标方法执行后的操作
     * 如果 target.method 抛出异常且
     *
     * @param target    目标对象
     * @param method    目标方法
     * @param args      参数
     * @param returnVal 目标方法执行返回值
     * @return 是否允许返回值（接下来的操作）
     * @see Aspect#afterException 返回true,则不会执行此操作
     * 如果
     * @see Aspect#afterException 返回false,则无论target.method是否抛出异常,均会执行此操作
     */
    boolean after(Object target, Method method, Object[] args, Object returnVal);

    /**
     * 目标方法抛出异常时的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @param e      异常
     * @return 是否允许抛出异常
     */
    boolean afterException(Object target, Method method, Object[] args, Throwable e);
}
