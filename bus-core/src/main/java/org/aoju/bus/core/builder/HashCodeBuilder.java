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
package org.aoju.bus.core.builder;


import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.ArrayUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Assists in implementing {@link Object#hashCode()} methods.
 * </p>
 *
 * <p>
 * This class enables a good <code>hashCode</code> method to be built for any class. It follows the rules laid out in
 * the book <a href="http://www.oracle.com/technetwork/java/effectivejava-136174.html">Effective Java</a> by Joshua Bloch. Writing a
 * good <code>hashCode</code> method is actually quite difficult. This class aims to simplify the process.
 * </p>
 *
 * <p>
 * The following is the approach taken. When appending a data field, the current total is multiplied by the
 * multiplier then a relevant value
 * for that data type is added. For example, if the current hashCode is 17, and the multiplier is 37, then
 * appending the integer 45 will create a hash code of 674, namely 17 * 37 + 45.
 * </p>
 *
 * <p>
 * All relevant fields from the object should be included in the <code>hashCode</code> method. Derived fields may be
 * excluded. In general, any field used in the <code>equals</code> method must be used in the <code>hashCode</code>
 * method.
 * </p>
 *
 * <p>
 * To use this class write code as follows:
 * </p>
 *
 * <pre>
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   ...
 *
 *   public int hashCode() {
 *     // you pick a hard-coded, randomly chosen, non-zero, odd number
 *     // ideally different for each class
 *     return new HashCodeBuilder(17, 37).
 *       append(name).
 *       append(age).
 *       append(smoker).
 *       toHashCode();
 *   }
 * }
 * </pre>
 *
 * <p>
 * If required, the superclass <code>hashCode()</code> can be added using {@link #appendSuper}.
 * </p>
 *
 * <p>
 * Alternatively, there is a method that uses reflection to determine the fields to test. Because these fields are
 * usually private, the method, <code>reflectionHashCode</code>, uses <code>AccessibleObject.setAccessible</code>
 * to change the visibility of the fields. This will fail under a security manager, unless the appropriate permissions
 * are set up correctly. It is also slower than testing explicitly.
 * </p>
 *
 * <p>
 * A typical invocation for this method would look like:
 * </p>
 *
 * <pre>
 * public int hashCode() {
 *   return HashCodeBuilder.reflectionHashCode(this);
 * }
 * </pre>
 *
 * <p>The {@link HashCodeExclude} annotation can be used to exclude fields from being
 * used by the <code>reflectionHashCode</code> methods.</p>
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class HashCodeBuilder implements Builder<Integer> {

    /**
     * The default initial value to use in reflection hash code building.
     */
    private static final int DEFAULT_INITIAL_VALUE = 17;

    /**
     * The default multiplier value to use in reflection hash code building.
     */
    private static final int DEFAULT_MULTIPLIER_VALUE = 37;

    /**
     * <p>
     * A registry of objects used by reflection methods to detect cyclical object references and avoid infinite loops.
     * </p>
     *
     * @since 2.3.0
     */
    private static final ThreadLocal<Set<IDKey>> REGISTRY = new ThreadLocal<>();

    /**
     * Constant to use in building the hashCode.
     */
    private final int iConstant;
    /**
     * Running total of the hashCode.
     */
    private int iTotal = 0;

    /**
     * <p>
     * Uses two hard coded choices for the constants needed to build a <code>hashCode</code>.
     * </p>
     */
    public HashCodeBuilder() {
        iConstant = 37;
        iTotal = 17;
    }

    /**
     * <p>
     * Two randomly chosen, odd numbers must be passed in. Ideally these should be different for each class,
     * however this is not vital.
     * </p>
     *
     * <p>
     * Prime numbers are preferred, especially for the multiplier.
     * </p>
     *
     * @param initialOddNumber    an odd number used as the initial value
     * @param multiplierOddNumber an odd number used as the multiplier
     * @throws IllegalArgumentException if the number is even
     */
    public HashCodeBuilder(final int initialOddNumber, final int multiplierOddNumber) {
        Assert.isTrue(initialOddNumber % 2 != 0, "HashCodeBuilder requires an odd initial value");
        Assert.isTrue(multiplierOddNumber % 2 != 0, "HashCodeBuilder requires an odd multiplier");
        iConstant = multiplierOddNumber;
        iTotal = initialOddNumber;
    }

    /**
     * <p>
     * Returns the registry of objects being traversed by the reflection methods in the current thread.
     * </p>
     *
     * @return Set the registry of objects being traversed
     * @since 2.3.0
     */
    static Set<IDKey> getRegistry() {
        return REGISTRY.get();
    }

    /**
     * <p>
     * Returns <code>true</code> if the registry contains the given object. Used by the reflection methods to avoid
     * infinite loops.
     * </p>
     *
     * @param value The object to lookup in the registry.
     * @return boolean <code>true</code> if the registry contains the given object.
     * @since 2.3.0
     */
    static boolean isRegistered(final Object value) {
        final Set<IDKey> registry = getRegistry();
        return registry != null && registry.contains(new IDKey(value));
    }

    /**
     * <p>
     * Appends the fields and values defined by the given object of the given <code>Class</code>.
     * </p>
     *
     * @param object        the object to append details of
     * @param clazz         the class to append details of
     * @param builder       the builder to append to
     * @param useTransients whether to use transient fields
     * @param excludeFields Collection of String field names to exclude from use in calculation of hash code
     */
    private static void reflectionAppend(final Object object, final Class<?> clazz, final HashCodeBuilder builder, final boolean useTransients,
                                         final String[] excludeFields) {
        if (isRegistered(object)) {
            return;
        }
        try {
            register(object);
            final Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (final Field field : fields) {
                if (!ArrayUtils.contains(excludeFields, field.getName())
                        && !field.getName().contains("$")
                        && (useTransients || !Modifier.isTransient(field.getModifiers()))
                        && !Modifier.isStatic(field.getModifiers())
                        && !field.isAnnotationPresent(HashCodeExclude.class)) {
                    try {
                        final Object fieldValue = field.get(object);
                        builder.append(fieldValue);
                    } catch (final IllegalAccessException e) {
                        // this can't happen. Would get a Security exception instead
                        // throw a runtime exception in case the impossible happens.
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        } finally {
            unregister(object);
        }
    }

    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * Transient members will be not be used, as they are likely derived fields, and not part of the value of the
     * <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be tested. Superclass fields will be included.
     * </p>
     *
     * <p>
     * Two randomly chosen, non-zero, odd numbers must be passed in. Ideally these should be different for each class,
     * however this is not vital. Prime numbers are preferred, especially for the multiplier.
     * </p>
     *
     * @param initialNonZeroOddNumber    a non-zero, odd number used as the initial value. This will be the returned
     *                                   value if no fields are found to include in the hash code
     * @param multiplierNonZeroOddNumber a non-zero, odd number used as the multiplier
     * @param object                     the Object to create a <code>hashCode</code> for
     * @return int hash code
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @throws IllegalArgumentException if the number is zero or even
     * @see HashCodeExclude
     */
    public static int reflectionHashCode(final int initialNonZeroOddNumber, final int multiplierNonZeroOddNumber, final Object object) {
        return reflectionHashCode(initialNonZeroOddNumber, multiplierNonZeroOddNumber, object, false, null);
    }


    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the TestTransients parameter is set to <code>true</code>, transient members will be tested, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be tested. Superclass fields will be included.
     * </p>
     *
     * <p>
     * Two randomly chosen, non-zero, odd numbers must be passed in. Ideally these should be different for each class,
     * however this is not vital. Prime numbers are preferred, especially for the multiplier.
     * </p>
     *
     * @param initialNonZeroOddNumber    a non-zero, odd number used as the initial value. This will be the returned
     *                                   value if no fields are found to include in the hash code
     * @param multiplierNonZeroOddNumber a non-zero, odd number used as the multiplier
     * @param object                     the Object to create a <code>hashCode</code> for
     * @param testTransients             whether to include transient fields
     * @return int hash code
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @throws IllegalArgumentException if the number is zero or even
     * @see HashCodeExclude
     */
    public static int reflectionHashCode(final int initialNonZeroOddNumber, final int multiplierNonZeroOddNumber, final Object object,
                                         final boolean testTransients) {
        return reflectionHashCode(initialNonZeroOddNumber, multiplierNonZeroOddNumber, object, testTransients, null);
    }

    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the TestTransients parameter is set to <code>true</code>, transient members will be tested, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be included. Superclass fields will be included up to and including the specified
     * superclass. A null superclass is treated as java.lang.Object.
     * </p>
     *
     * <p>
     * Two randomly chosen, non-zero, odd numbers must be passed in. Ideally these should be different for each class,
     * however this is not vital. Prime numbers are preferred, especially for the multiplier.
     * </p>
     *
     * @param <T>                        the type of the object involved
     * @param initialNonZeroOddNumber    a non-zero, odd number used as the initial value. This will be the returned
     *                                   value if no fields are found to include in the hash code
     * @param multiplierNonZeroOddNumber a non-zero, odd number used as the multiplier
     * @param object                     the Object to create a <code>hashCode</code> for
     * @param testTransients             whether to include transient fields
     * @param reflectUpToClass           the superclass to reflect up to (inclusive), may be <code>null</code>
     * @param excludeFields              array of field names to exclude from use in calculation of hash code
     * @return int hash code
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @throws IllegalArgumentException if the number is zero or even
     * @see HashCodeExclude
     * @since 2.0.0
     */
    public static <T> int reflectionHashCode(final int initialNonZeroOddNumber, final int multiplierNonZeroOddNumber, final T object,
                                             final boolean testTransients, final Class<? super T> reflectUpToClass, final String... excludeFields) {
        Assert.isTrue(object != null, "The object to build a hash code for must not be null");
        final HashCodeBuilder builder = new HashCodeBuilder(initialNonZeroOddNumber, multiplierNonZeroOddNumber);
        Class<?> clazz = object.getClass();
        reflectionAppend(object, clazz, builder, testTransients, excludeFields);
        while (clazz.getSuperclass() != null && clazz != reflectUpToClass) {
            clazz = clazz.getSuperclass();
            reflectionAppend(object, clazz, builder, testTransients, excludeFields);
        }
        return builder.toHashCode();
    }

    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * This constructor uses two hard coded choices for the constants needed to build a hash code.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <P>
     * If the TestTransients parameter is set to <code>true</code>, transient members will be tested, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be tested. Superclass fields will be included. If no fields are found to include
     * in the hash code, the result of this method will be constant.
     * </p>
     *
     * @param object         the Object to create a <code>hashCode</code> for
     * @param testTransients whether to include transient fields
     * @return int hash code
     * @throws IllegalArgumentException if the object is <code>null</code>
     * @see HashCodeExclude
     */
    public static int reflectionHashCode(final Object object, final boolean testTransients) {
        return reflectionHashCode(DEFAULT_INITIAL_VALUE, DEFAULT_MULTIPLIER_VALUE, object,
                testTransients, null);
    }

    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * This constructor uses two hard coded choices for the constants needed to build a hash code.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * Transient members will be not be used, as they are likely derived fields, and not part of the value of the
     * <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be tested. Superclass fields will be included. If no fields are found to include
     * in the hash code, the result of this method will be constant.
     * </p>
     *
     * @param object        the Object to create a <code>hashCode</code> for
     * @param excludeFields Collection of String field names to exclude from use in calculation of hash code
     * @return int hash code
     * @throws IllegalArgumentException if the object is <code>null</code>
     * @see HashCodeExclude
     */
    public static int reflectionHashCode(final Object object, final Collection<String> excludeFields) {
        return reflectionHashCode(object, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    /**
     * <p>
     * Uses reflection to build a valid hash code from the fields of {@code object}.
     * </p>
     *
     * <p>
     * This constructor uses two hard coded choices for the constants needed to build a hash code.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * Transient members will be not be used, as they are likely derived fields, and not part of the value of the
     * <code>Object</code>.
     * </p>
     *
     * <p>
     * Static fields will not be tested. Superclass fields will be included. If no fields are found to include
     * in the hash code, the result of this method will be constant.
     * </p>
     *
     * @param object        the Object to create a <code>hashCode</code> for
     * @param excludeFields array of field names to exclude from use in calculation of hash code
     * @return int hash code
     * @throws IllegalArgumentException if the object is <code>null</code>
     * @see HashCodeExclude
     */
    public static int reflectionHashCode(final Object object, final String... excludeFields) {
        return reflectionHashCode(DEFAULT_INITIAL_VALUE, DEFAULT_MULTIPLIER_VALUE, object, false,
                null, excludeFields);
    }

    /**
     * <p>
     * Registers the given object. Used by the reflection methods to avoid infinite loops.
     * </p>
     *
     * @param value The object to register.
     */
    private static void register(final Object value) {
        Set<IDKey> registry = getRegistry();
        if (registry == null) {
            registry = new HashSet<>();
            REGISTRY.set(registry);
        }
        registry.add(new IDKey(value));
    }

    /**
     * <p>
     * Unregisters the given object.
     * </p>
     *
     * <p>
     * Used by the reflection methods to avoid infinite loops.
     *
     * @param value The object to unregister.
     * @since 2.3.0
     */
    private static void unregister(final Object value) {
        final Set<IDKey> registry = getRegistry();
        if (registry != null) {
            registry.remove(new IDKey(value));
            if (registry.isEmpty()) {
                REGISTRY.remove();
            }
        }
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>boolean</code>.
     * </p>
     * <p>
     * This adds <code>1</code> when true, and <code>0</code> when false to the <code>hashCode</code>.
     * </p>
     * <p>
     * This is in contrast to the standard <code>java.lang.Boolean.hashCode</code> handling, which computes
     * a <code>hashCode</code> value of <code>1231</code> for <code>java.lang.Boolean</code> instances
     * that represent <code>true</code> or <code>1237</code> for <code>java.lang.Boolean</code> instances
     * that represent <code>false</code>.
     * </p>
     * <p>
     * This is in accordance with the <i>Effective Java</i> design.
     * </p>
     *
     * @param value the boolean to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final boolean value) {
        iTotal = iTotal * iConstant + (value ? 0 : 1);
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>boolean</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final boolean[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final boolean element : array) {
                append(element);
            }
        }
        return this;
    }


    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>byte</code>.
     * </p>
     *
     * @param value the byte to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final byte value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>byte</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final byte[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final byte element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>char</code>.
     * </p>
     *
     * @param value the char to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final char value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>char</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final char[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final char element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>double</code>.
     * </p>
     *
     * @param value the double to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final double value) {
        return append(Double.doubleToLongBits(value));
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>double</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final double[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final double element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>float</code>.
     * </p>
     *
     * @param value the float to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final float value) {
        iTotal = iTotal * iConstant + Float.floatToIntBits(value);
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>float</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final float[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final float element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for an <code>int</code>.
     * </p>
     *
     * @param value the int to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final int value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for an <code>int</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final int[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final int element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>long</code>.
     * </p>
     *
     * @param value the long to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final long value) {
        iTotal = iTotal * iConstant + ((int) (value ^ (value >> 32)));
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>long</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final long[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final long element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for an <code>Object</code>.
     * </p>
     *
     * @param object the Object to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final Object object) {
        if (object == null) {
            iTotal = iTotal * iConstant;

        } else {
            if (object.getClass().isArray()) {
                // factor out array case in order to keep method small enough
                // to be inlined
                appendArray(object);
            } else {
                iTotal = iTotal * iConstant + object.hashCode();
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for an array.
     * </p>
     *
     * @param object the array to add to the <code>hashCode</code>
     */
    private void appendArray(final Object object) {
        // 'Switch' on type of array, to dispatch to the correct handler
        // This handles multi dimensional arrays
        if (object instanceof long[]) {
            append((long[]) object);
        } else if (object instanceof int[]) {
            append((int[]) object);
        } else if (object instanceof short[]) {
            append((short[]) object);
        } else if (object instanceof char[]) {
            append((char[]) object);
        } else if (object instanceof byte[]) {
            append((byte[]) object);
        } else if (object instanceof double[]) {
            append((double[]) object);
        } else if (object instanceof float[]) {
            append((float[]) object);
        } else if (object instanceof boolean[]) {
            append((boolean[]) object);
        } else {
            // Not an array of primitives
            append((Object[]) object);
        }
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for an <code>Object</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final Object[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final Object element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>short</code>.
     * </p>
     *
     * @param value the short to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final short value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    /**
     * <p>
     * Append a <code>hashCode</code> for a <code>short</code> array.
     * </p>
     *
     * @param array the array to add to the <code>hashCode</code>
     * @return this
     */
    public HashCodeBuilder append(final short[] array) {
        if (array == null) {
            iTotal = iTotal * iConstant;
        } else {
            for (final short element : array) {
                append(element);
            }
        }
        return this;
    }

    /**
     * <p>
     * Adds the result of super.hashCode() to this builder.
     * </p>
     *
     * @param superHashCode the result of calling <code>super.hashCode()</code>
     * @return this HashCodeBuilder, used to chain calls.
     * @since 2.0.0
     */
    public HashCodeBuilder appendSuper(final int superHashCode) {
        iTotal = iTotal * iConstant + superHashCode;
        return this;
    }

    /**
     * <p>
     * Return the computed <code>hashCode</code>.
     * </p>
     *
     * @return <code>hashCode</code> based on the fields appended
     */
    public int toHashCode() {
        return iTotal;
    }

    /**
     * Returns the computed <code>hashCode</code>.
     *
     * @return <code>hashCode</code> based on the fields appended
     * @since 3.0.0
     */
    @Override
    public Integer build() {
        return Integer.valueOf(toHashCode());
    }

    /**
     * <p>
     * The computed <code>hashCode</code> from toHashCode() is returned due to the likelihood
     * of bugs in mis-calling toHashCode() and the unlikeliness of it mattering what the hashCode for
     * HashCodeBuilder itself is.</p>
     *
     * @return <code>hashCode</code> based on the fields appended
     * @since 2.5.0
     */
    @Override
    public int hashCode() {
        return toHashCode();
    }

}
