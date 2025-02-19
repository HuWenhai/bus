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
package org.aoju.bus.http.accord.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Duck-typing for methods: Represents a method that may or may not be present on an object.
 *
 * @param <T> the type of the object the method might be on, typically an interface or base class
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
class OptionalMethod<T> {

    /**
     * The return type of the method. null means "don't care".
     */
    private final Class<?> returnType;

    private final String methodName;

    private final Class[] methodParams;

    /**
     * Creates an optional method.
     *
     * @param returnType   the return type to required, null if it does not matter
     * @param methodName   the name of the method
     * @param methodParams the method parameter types
     */
    OptionalMethod(Class<?> returnType, String methodName, Class... methodParams) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

    private static Method getPublicMethod(Class<?> clazz, String methodName, Class[] parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                method = null;
            }
        } catch (NoSuchMethodException e) {
            // None.
        }
        return method;
    }

    public boolean isSupported(T target) {
        return getMethod(target.getClass()) != null;
    }

    public Object invokeOptional(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public Object invokeOptionalWithoutCheckedException(T target, Object... args) {
        try {
            return invokeOptional(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    public Object invoke(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m == null) {
            throw new AssertionError("Method " + methodName + " not supported for object " + target);
        }
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            // Method should be public: we checked.
            AssertionError error = new AssertionError("Unexpectedly could not call: " + m);
            error.initCause(e);
            throw error;
        }
    }

    public Object invokeWithoutCheckedException(T target, Object... args) {
        try {
            return invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    private Method getMethod(Class<?> clazz) {
        Method method = null;
        if (methodName != null) {
            method = getPublicMethod(clazz, methodName, methodParams);
            if (method != null
                    && returnType != null
                    && !returnType.isAssignableFrom(method.getReturnType())) {

                method = null;
            }
        }
        return method;
    }

}

