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

import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.ClassUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <p>{@link Object#equals(Object)} 方法的构建器</p>
 *
 * <p>两个对象equals必须保证hashCode值相等,hashCode值相等不能保证一定equals</p>
 *
 * <p>使用方法如下：</p>
 * <pre>
 * public boolean equals(Object obj) {
 *   if (obj == null) { return false; }
 *   if (obj == this) { return true; }
 *   if (obj.getClass() != getClass()) {
 *     return false;
 *   }
 *   MyClass rhs = (MyClass) obj;
 *   return new EqualsBuilder()
 *                 .appendSuper(super.equals(obj))
 *                 .append(field1, rhs.field1)
 *                 .append(field2, rhs.field2)
 *                 .append(field3, rhs.field3)
 *                 .isEquals();
 *  }
 * </pre>
 *
 * <p> 我们也可以通过反射判断所有字段是否equals：</p>
 * <pre>
 * public boolean equals(Object obj) {
 *   return EqualsBuilder.reflectionEquals(this, obj);
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class EqualsBuilder implements Builder<Boolean> {

    /**
     * <p>
     * A registry of objects used by reflection methods to detect cyclical object references and avoid infinite loops.
     * </p>
     *
     * @since 3.0.0
     */
    private static final ThreadLocal<Set<Pair<IDKey, IDKey>>> REGISTRY = new ThreadLocal<>();

    /**
     * 是否equals,此值随着构建会变更,默认true
     */
    private boolean isEquals = true;
    private boolean testTransients = false;
    private boolean testRecursive = false;
    private List<Class<?>> bypassReflectionClasses;
    private Class<?> reflectUpToClass = null;
    private String[] excludeFields = null;

    /**
     * <p>Constructor for EqualsBuilder.</p>
     *
     * <p>Starts off assuming that equals is <code>true</code>.</p>
     *
     * @see Object#equals(Object)
     */
    public EqualsBuilder() {
        // set up default classes to bypass reflection for
        bypassReflectionClasses = new ArrayList<>();
        bypassReflectionClasses.add(String.class); //hashCode field being lazy but not transient
    }

    /**
     * <p>
     * Returns the registry of object pairs being traversed by the reflection
     * methods in the current thread.
     * </p>
     *
     * @return Set the registry of objects being traversed
     * @since 3.0.0
     */
    static Set<Pair<IDKey, IDKey>> getRegistry() {
        return REGISTRY.get();
    }

    /**
     * <p>
     * Converters value pair into a register pair.
     * </p>
     *
     * @param lhs <code>this</code> object
     * @param rhs the other object
     * @return the pair
     */
    static Pair<IDKey, IDKey> getRegisterPair(final Object lhs, final Object rhs) {
        final IDKey left = new IDKey(lhs);
        final IDKey right = new IDKey(rhs);
        return Pair.of(left, right);
    }

    /**
     * <p>
     * Returns <code>true</code> if the registry contains the given object pair.
     * Used by the reflection methods to avoid infinite loops.
     * Objects might be swapped therefore a check is needed if the object pair
     * is registered in given or swapped order.
     * </p>
     *
     * @param lhs <code>this</code> object to lookup in registry
     * @param rhs the other object to lookup on registry
     * @return boolean <code>true</code> if the registry contains the given object.
     * @since 3.0.0
     */
    static boolean isRegistered(final Object lhs, final Object rhs) {
        final Set<Pair<IDKey, IDKey>> registry = getRegistry();
        final Pair<IDKey, IDKey> pair = getRegisterPair(lhs, rhs);
        final Pair<IDKey, IDKey> swappedPair = Pair.of(pair.getRight(), pair.getLeft());

        return registry != null
                && (registry.contains(pair) || registry.contains(swappedPair));
    }

    /**
     * <p>
     * Registers the given object pair.
     * Used by the reflection methods to avoid infinite loops.
     * </p>
     *
     * @param lhs <code>this</code> object to register
     * @param rhs the other object to register
     */
    private static void register(final Object lhs, final Object rhs) {
        Set<Pair<IDKey, IDKey>> registry = getRegistry();
        if (registry == null) {
            registry = new HashSet<>();
            REGISTRY.set(registry);
        }
        final Pair<IDKey, IDKey> pair = getRegisterPair(lhs, rhs);
        registry.add(pair);
    }

    /**
     * <p>
     * Unregisters the given object pair.
     * </p>
     *
     * <p>
     * Used by the reflection methods to avoid infinite loops.
     *
     * @param lhs <code>this</code> object to unregister
     * @param rhs the other object to unregister
     * @since 3.0.0
     */
    private static void unregister(final Object lhs, final Object rhs) {
        final Set<Pair<IDKey, IDKey>> registry = getRegistry();
        if (registry != null) {
            final Pair<IDKey, IDKey> pair = getRegisterPair(lhs, rhs);
            registry.remove(pair);
            if (registry.isEmpty()) {
                REGISTRY.remove();
            }
        }
    }

    /**
     * <p>反射检查两个对象是否equals,此方法检查对象及其父对象的属性（包括私有属性）是否equals</p>
     *
     * @param lhs           此对象
     * @param rhs           另一个对象
     * @param excludeFields 排除的字段集合,如果有不参与计算equals的字段加入此集合即可
     * @return 两个对象是否equals, 是返回<code>true</code>
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final Collection<String> excludeFields) {
        return reflectionEquals(lhs, rhs, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    /**
     * <p>反射检查两个对象是否equals,此方法检查对象及其父对象的属性（包括私有属性）是否equals</p>
     *
     * @param lhs           此对象
     * @param rhs           另一个对象
     * @param excludeFields 排除的字段集合,如果有不参与计算equals的字段加入此集合即可
     * @return 两个对象是否equals, 是返回<code>true</code>
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final String... excludeFields) {
        return reflectionEquals(lhs, rhs, false, null, excludeFields);
    }

    /**
     * <p>This method uses reflection to determine if the two <code>Object</code>s
     * are equal.</p>
     *
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     *
     * <p>If the TestTransients parameter is set to <code>true</code>, transient
     * members will be tested, otherwise they are ignored, as they are likely
     * derived fields, and not part of the value of the <code>Object</code>.</p>
     *
     * <p>Static fields will not be tested. Superclass fields will be included.</p>
     *
     * @param lhs            <code>this</code> object
     * @param rhs            the other object
     * @param testTransients whether to include transient fields
     * @return <code>true</code> if the two Objects have tested equals.
     * @see EqualsExclude
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients) {
        return reflectionEquals(lhs, rhs, testTransients, null);
    }

    /**
     * <p>This method uses reflection to determine if the two <code>Object</code>s
     * are equal.</p>
     *
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     *
     * <p>If the testTransients parameter is set to <code>true</code>, transient
     * members will be tested, otherwise they are ignored, as they are likely
     * derived fields, and not part of the value of the <code>Object</code>.</p>
     *
     * <p>Static fields will not be included. Superclass fields will be appended
     * up to and including the specified superclass. A null superclass is treated
     * as java.lang.Object.</p>
     *
     * @param lhs              <code>this</code> object
     * @param rhs              the other object
     * @param testTransients   whether to include transient fields
     * @param reflectUpToClass the superclass to reflect up to (inclusive),
     *                         may be <code>null</code>
     * @param excludeFields    array of field names to exclude from testing
     * @return <code>true</code> if the two Objects have tested equals.
     * @see EqualsExclude
     * @since 2.0.0
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients, final Class<?> reflectUpToClass,
                                           final String... excludeFields) {
        return reflectionEquals(lhs, rhs, testTransients, reflectUpToClass, false, excludeFields);
    }

    /**
     * <p>This method uses reflection to determine if the two <code>Object</code>s
     * are equal.</p>
     *
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     *
     * <p>If the testTransients parameter is set to <code>true</code>, transient
     * members will be tested, otherwise they are ignored, as they are likely
     * derived fields, and not part of the value of the <code>Object</code>.</p>
     *
     * <p>Static fields will not be included. Superclass fields will be appended
     * up to and including the specified superclass. A null superclass is treated
     * as java.lang.Object.</p>
     *
     * <p>If the testRecursive parameter is set to <code>true</code>, non primitive
     * (and non primitive wrapper) field types will be compared by
     * <code>EqualsBuilder</code> recursively instead of invoking their
     * <code>equals()</code> method. Leading to a deep reflection equals test.
     *
     * @param lhs              <code>this</code> object
     * @param rhs              the other object
     * @param testTransients   whether to include transient fields
     * @param reflectUpToClass the superclass to reflect up to (inclusive),
     *                         may be <code>null</code>
     * @param testRecursive    whether to call reflection equals on non primitive
     *                         fields recursively.
     * @param excludeFields    array of field names to exclude from testing
     * @return <code>true</code> if the two Objects have tested equals.
     * @see EqualsExclude
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients, final Class<?> reflectUpToClass,
                                           final boolean testRecursive, final String... excludeFields) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        return new EqualsBuilder()
                .setExcludeFields(excludeFields)
                .setReflectUpToClass(reflectUpToClass)
                .setTestTransients(testTransients)
                .setTestRecursive(testRecursive)
                .reflectionAppend(lhs, rhs)
                .isEquals();
    }

    /**
     * Set whether to include transient fields when reflectively comparing objects.
     *
     * @param testTransients whether to test transient fields
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder setTestTransients(final boolean testTransients) {
        this.testTransients = testTransients;
        return this;
    }

    /**
     * Set whether to include transient fields when reflectively comparing objects.
     *
     * @param testRecursive whether to do a recursive test
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder setTestRecursive(final boolean testRecursive) {
        this.testRecursive = testRecursive;
        return this;
    }

    /**
     * <p>Set <code>Class</code>es whose instances should be compared by calling their <code>equals</code>
     * although being in recursive mode. So the fields of theses classes will not be compared recursively by reflection.</p>
     *
     * <p>Here you should name classes having non-transient fields which are cache fields being set lazily.
     * Prominent example being {@link String} class with its hash code cache field. Due to the importance
     * of the <code>String</code> class, it is included in the default bypasses classes. Usually, if you use
     * your own set of classes here, remember to include <code>String</code> class, too.</p>
     *
     * @param bypassReflectionClasses classes to bypass reflection test
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder setBypassReflectionClasses(List<Class<?>> bypassReflectionClasses) {
        this.bypassReflectionClasses = bypassReflectionClasses;
        return this;
    }

    /**
     * Set the superclass to reflect up to at reflective tests.
     *
     * @param reflectUpToClass the super class to reflect up to
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder setReflectUpToClass(final Class<?> reflectUpToClass) {
        this.reflectUpToClass = reflectUpToClass;
        return this;
    }

    /**
     * Set field names to be excluded by reflection tests.
     *
     * @param excludeFields the fields to exclude
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder setExcludeFields(final String... excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    /**
     * <p>Tests if two <code>objects</code> by using reflection.</p>
     *
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     *
     * <p>If the testTransients field is set to <code>true</code>, transient
     * members will be tested, otherwise they are ignored, as they are likely
     * derived fields, and not part of the value of the <code>Object</code>.</p>
     *
     * <p>Static fields will not be included. Superclass fields will be appended
     * up to and including the specified superclass in field <code>reflectUpToClass</code>.
     * A null superclass is treated as java.lang.Object.</p>
     *
     * <p>Field names listed in field <code>excludeFields</code> will be ignored.</p>
     *
     * <p>If either class of the compared objects is contained in
     * <code>bypassReflectionClasses</code>, both objects are compared by calling
     * the equals method of the left hand object with the right hand object as an argument.</p>
     *
     * @param lhs the left hand object
     * @param rhs the left hand object
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder reflectionAppend(final Object lhs, final Object rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            isEquals = false;
            return this;
        }

        // Find the leaf class since there may be transients in the leaf
        // class or in classes between the leaf and root.
        // If we are not testing transients or a subclass has no ivars,
        // then a subclass can test equals to a superclass.
        final Class<?> lhsClass = lhs.getClass();
        final Class<?> rhsClass = rhs.getClass();
        Class<?> testClass;
        if (lhsClass.isInstance(rhs)) {
            testClass = lhsClass;
            if (!rhsClass.isInstance(lhs)) {
                // rhsClass is a subclass of lhsClass
                testClass = rhsClass;
            }
        } else if (rhsClass.isInstance(lhs)) {
            testClass = rhsClass;
            if (!lhsClass.isInstance(rhs)) {
                // lhsClass is a subclass of rhsClass
                testClass = lhsClass;
            }
        } else {
            // The two classes are not related.
            isEquals = false;
            return this;
        }

        try {
            if (testClass.isArray()) {
                append(lhs, rhs);
            } else {
                //If either class is being excluded, call normal object equals method on lhsClass.
                if (bypassReflectionClasses != null
                        && (bypassReflectionClasses.contains(lhsClass) || bypassReflectionClasses.contains(rhsClass))) {
                    isEquals = lhs.equals(rhs);
                } else {
                    reflectionAppend(lhs, rhs, testClass);
                    while (testClass.getSuperclass() != null && testClass != reflectUpToClass) {
                        testClass = testClass.getSuperclass();
                        reflectionAppend(lhs, rhs, testClass);
                    }
                }
            }
        } catch (final IllegalArgumentException e) {
            // In this case, we tried to test a subclass vs. a superclass and
            // the subclass has ivars or the ivars are transient and
            // we are testing transients.
            // If a subclass has ivars that we are trying to test them, we get an
            // exception and we know that the objects are not equal.
            isEquals = false;
            return this;
        }
        return this;
    }

    /**
     * <p>Appends the fields and values defined by the given object of the
     * given Class.</p>
     *
     * @param lhs   the left hand object
     * @param rhs   the right hand object
     * @param clazz the class to append details of
     */
    private void reflectionAppend(
            final Object lhs,
            final Object rhs,
            final Class<?> clazz) {

        if (isRegistered(lhs, rhs)) {
            return;
        }

        try {
            register(lhs, rhs);
            final Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < fields.length && isEquals; i++) {
                final Field f = fields[i];
                if (!ArrayUtils.contains(excludeFields, f.getName())
                        && !f.getName().contains("$")
                        && (testTransients || !Modifier.isTransient(f.getModifiers()))
                        && !Modifier.isStatic(f.getModifiers())
                        && !f.isAnnotationPresent(EqualsExclude.class)) {
                    try {
                        append(f.get(lhs), f.get(rhs));
                    } catch (final IllegalAccessException e) {
                        //this can't happen. Would get a Security exception instead
                        //throw a runtime exception in case the impossible happens.
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        } finally {
            unregister(lhs, rhs);
        }
    }

    /**
     * <p>Adds the result of <code>super.equals()</code> to this builder.</p>
     *
     * @param superEquals the result of calling <code>super.equals()</code>
     * @return EqualsBuilder - used to chain calls.
     * @since 2.0.0
     */
    public EqualsBuilder appendSuper(final boolean superEquals) {
        if (!isEquals) {
            return this;
        }
        isEquals = superEquals;
        return this;
    }

    /**
     * <p>Test if two <code>Object</code>s are equal using either
     * #{@link #reflectionAppend(Object, Object)}, if object are non
     * primitives (or wrapper of primitives) or if field <code>testRecursive</code>
     * is set to <code>false</code>. Otherwise, using their
     * <code>equals</code> method.</p>
     *
     * @param lhs the left hand object
     * @param rhs the right hand object
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final Object lhs, final Object rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        final Class<?> lhsClass = lhs.getClass();
        if (lhsClass.isArray()) {
            // factor out array case in order to keep method small enough
            // to be inlined
            appendArray(lhs, rhs);
        } else {
            // The simple case, not an array, just test the element
            if (testRecursive && !ClassUtils.isPrimitiveOrWrapper(lhsClass)) {
                reflectionAppend(lhs, rhs);
            } else {
                isEquals = lhs.equals(rhs);
            }
        }
        return this;
    }

    /**
     * <p>Test if an <code>Object</code> is equal to an array.</p>
     *
     * @param lhs the left hand object, an array
     * @param rhs the right hand object
     */
    private void appendArray(final Object lhs, final Object rhs) {
        // First we compare different dimensions, for example: a boolean[][] to a boolean[]
        // then we 'Switch' on type of array, to dispatch to the correct handler
        // This handles multi dimensional arrays of the same depth
        if (lhs.getClass() != rhs.getClass()) {
            this.setEquals(false);
        } else if (lhs instanceof long[]) {
            append((long[]) lhs, (long[]) rhs);
        } else if (lhs instanceof int[]) {
            append((int[]) lhs, (int[]) rhs);
        } else if (lhs instanceof short[]) {
            append((short[]) lhs, (short[]) rhs);
        } else if (lhs instanceof char[]) {
            append((char[]) lhs, (char[]) rhs);
        } else if (lhs instanceof byte[]) {
            append((byte[]) lhs, (byte[]) rhs);
        } else if (lhs instanceof double[]) {
            append((double[]) lhs, (double[]) rhs);
        } else if (lhs instanceof float[]) {
            append((float[]) lhs, (float[]) rhs);
        } else if (lhs instanceof boolean[]) {
            append((boolean[]) lhs, (boolean[]) rhs);
        } else {
            // Not an array of primitives
            append((Object[]) lhs, (Object[]) rhs);
        }
    }

    /**
     * <p>
     * Test if two <code>long</code> s are equal.
     * </p>
     *
     * @param lhs the left hand <code>long</code>
     * @param rhs the right hand <code>long</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final long lhs, final long rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>int</code>s are equal.</p>
     *
     * @param lhs the left hand <code>int</code>
     * @param rhs the right hand <code>int</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final int lhs, final int rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>short</code>s are equal.</p>
     *
     * @param lhs the left hand <code>short</code>
     * @param rhs the right hand <code>short</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final short lhs, final short rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>char</code>s are equal.</p>
     *
     * @param lhs the left hand <code>char</code>
     * @param rhs the right hand <code>char</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final char lhs, final char rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>byte</code>s are equal.</p>
     *
     * @param lhs the left hand <code>byte</code>
     * @param rhs the right hand <code>byte</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final byte lhs, final byte rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>double</code>s are equal by testing that the
     * pattern of bits returned by <code>doubleToLong</code> are equal.</p>
     *
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     *
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs the left hand <code>double</code>
     * @param rhs the right hand <code>double</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final double lhs, final double rhs) {
        if (!isEquals) {
            return this;
        }
        return append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
    }

    /**
     * <p>Test if two <code>float</code>s are equal byt testing that the
     * pattern of bits returned by doubleToLong are equal.</p>
     *
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     *
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs the left hand <code>float</code>
     * @param rhs the right hand <code>float</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final float lhs, final float rhs) {
        if (!isEquals) {
            return this;
        }
        return append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
    }

    /**
     * <p>Test if two <code>booleans</code>s are equal.</p>
     *
     * @param lhs the left hand <code>boolean</code>
     * @param rhs the right hand <code>boolean</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final boolean lhs, final boolean rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    /**
     * <p>Performs a deep comparison of two <code>Object</code> arrays.</p>
     *
     * <p>This also will be called for the top level of
     * multi-dimensional, ragged, and multi-typed arrays.</p>
     *
     * <p>Note that this method does not compare the type of the arrays; it only
     * compares the contents.</p>
     *
     * @param lhs the left hand <code>Object[]</code>
     * @param rhs the right hand <code>Object[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final Object[] lhs, final Object[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>long</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(long, long)} is used.</p>
     *
     * @param lhs the left hand <code>long[]</code>
     * @param rhs the right hand <code>long[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final long[] lhs, final long[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>int</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(int, int)} is used.</p>
     *
     * @param lhs the left hand <code>int[]</code>
     * @param rhs the right hand <code>int[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final int[] lhs, final int[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>short</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(short, short)} is used.</p>
     *
     * @param lhs the left hand <code>short[]</code>
     * @param rhs the right hand <code>short[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final short[] lhs, final short[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>char</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(char, char)} is used.</p>
     *
     * @param lhs the left hand <code>char[]</code>
     * @param rhs the right hand <code>char[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final char[] lhs, final char[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>byte</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(byte, byte)} is used.</p>
     *
     * @param lhs the left hand <code>byte[]</code>
     * @param rhs the right hand <code>byte[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final byte[] lhs, final byte[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>double</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(double, double)} is used.</p>
     *
     * @param lhs the left hand <code>double[]</code>
     * @param rhs the right hand <code>double[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final double[] lhs, final double[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>float</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(float, float)} is used.</p>
     *
     * @param lhs the left hand <code>float[]</code>
     * @param rhs the right hand <code>float[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final float[] lhs, final float[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Deep comparison of array of <code>boolean</code>. Length and all
     * values are compared.</p>
     *
     * <p>The method {@link #append(boolean, boolean)} is used.</p>
     *
     * @param lhs the left hand <code>boolean[]</code>
     * @param rhs the right hand <code>boolean[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final boolean[] lhs, final boolean[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Returns <code>true</code> if the fields that have been checked
     * are all equal.</p>
     *
     * @return boolean
     */
    public boolean isEquals() {
        return this.isEquals;
    }

    /**
     * Sets the <code>isEquals</code> value.
     *
     * @param isEquals The value to set.
     * @since 2.1.0
     */
    protected void setEquals(final boolean isEquals) {
        this.isEquals = isEquals;
    }

    /**
     * <p>Returns <code>true</code> if the fields that have been checked
     * are all equal.</p>
     *
     * @return <code>true</code> if all of the fields that have been checked
     * are equal, <code>false</code> otherwise.
     * @since 3.0.0
     */
    @Override
    public Boolean build() {
        return Boolean.valueOf(isEquals());
    }

    /**
     * Reset the EqualsBuilder so you can use the same object again
     *
     * @since 2.5.0
     */
    public void reset() {
        this.isEquals = true;
    }

}
