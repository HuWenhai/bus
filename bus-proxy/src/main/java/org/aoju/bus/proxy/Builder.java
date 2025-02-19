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
package org.aoju.bus.proxy;

import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.proxy.aspects.Aspect;
import org.aoju.bus.proxy.factory.AbstractFactory;
import org.aoju.bus.proxy.invoker.NullInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Builder {

    public static final Object[] EMPTY_ARGUMENTS = new Object[0];
    public static final Class[] EMPTY_ARGUMENT_TYPES = new Class[0];
    private static final Map wrapperClassMap = new HashMap();

    static {
        wrapperClassMap.put(Integer.TYPE, Integer.class);
        wrapperClassMap.put(Character.TYPE, Character.class);
        wrapperClassMap.put(Boolean.TYPE, Boolean.class);
        wrapperClassMap.put(Short.TYPE, Short.class);
        wrapperClassMap.put(Long.TYPE, Long.class);
        wrapperClassMap.put(Float.TYPE, Float.class);
        wrapperClassMap.put(Double.TYPE, Double.class);
        wrapperClassMap.put(Byte.TYPE, Byte.class);
    }

    public static Object createNullObject(Factory factory, Class[] proxyClasses) {
        return factory.createInvokerProxy(new NullInvoker(), proxyClasses);
    }

    public static Object createNullObject(Factory factory, ClassLoader classLoader, Class[] proxyClasses) {
        return factory.createInvokerProxy(classLoader, new NullInvoker(), proxyClasses);
    }

    public static Class[] getAllInterfaces(Class cls) {
        final List interfaces = getAllInterfacesImpl(cls, new LinkedList());
        return interfaces == null ? null : (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static List getAllInterfacesImpl(Class cls, List list) {
        if (cls == null) {
            return null;
        }
        while (cls != null) {
            Class[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (!list.contains(interfaces[i])) {
                    list.add(interfaces[i]);
                }
                getAllInterfacesImpl(interfaces[i], list);
            }
            cls = cls.getSuperclass();
        }
        return list;
    }

    public static String getJavaClassName(Class clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    public static Class getWrapperClass(Class primitiveType) {
        return (Class) wrapperClassMap.get(primitiveType);
    }


    /**
     * 使用切面代理对象
     *
     * @param <T>         切面对象类型
     * @param target      目标对象
     * @param aspectClass 切面对象类
     * @return 代理对象
     */
    public static <T> T proxy(T target, Class<? extends Aspect> aspectClass) {
        return AbstractFactory.createProxy(target, aspectClass);
    }

    /**
     * 使用切面代理对象
     *
     * @param <T>    被代理对象类型
     * @param target 被代理对象
     * @param aspect 切面对象
     * @return 代理对象
     */
    public static <T> T proxy(T target, Aspect aspect) {
        return AbstractFactory.createProxy(target, aspect);
    }

    /**
     * 创建动态代理对象<br>
     * 动态代理对象的创建原理是：<br>
     * 假设创建的代理对象名为 $Proxy0<br>
     * 1、根据传入的interfaces动态生成一个类,实现interfaces中的接口<br>
     * 2、通过传入的classloder将刚生成的类加载到jvm中 即将$Proxy0类load<br>
     * 3、调用$Proxy0的$Proxy0(InvocationHandler)构造函数 创建$Proxy0的对象,并且用interfaces参数遍历其所有接口的方法,这些实现方法的实现本质上是通过反射调用被代理对象的方法<br>
     * 4、将$Proxy0的实例返回给客户端  <br>
     * 5、当调用代理类的相应方法时,相当于调用 {@link InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])} 方法
     *
     * @param <T>               被代理对象类型
     * @param classloader       被代理类对应的ClassLoader
     * @param invocationHandler {@link InvocationHandler} ,被代理类通过实现此接口提供动态代理功能
     * @param interfaces        代理类中需要实现的被代理类的接口方法
     * @return 代理类
     */
    public static <T> T newProxyInstance(ClassLoader classloader, InvocationHandler invocationHandler, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(classloader, interfaces, invocationHandler);
    }

    /**
     * 创建动态代理对象
     *
     * @param <T>               被代理对象类型
     * @param invocationHandler {@link InvocationHandler} ,被代理类通过实现此接口提供动态代理功能
     * @param interfaces        代理类中需要实现的被代理类的接口方法
     * @return 代理类
     */
    public static <T> T newProxyInstance(InvocationHandler invocationHandler, Class<?>... interfaces) {
        return newProxyInstance(ClassUtils.getClassLoader(), invocationHandler, interfaces);
    }

}

