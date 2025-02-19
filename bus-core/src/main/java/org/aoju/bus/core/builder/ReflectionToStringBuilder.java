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

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.ArrayUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Assists in implementing {@link Object#toString()} methods using reflection.
 * </p>
 * <p>
 * This class uses reflection to determine the fields to append. Because these fields are usually private, the class
 * uses {@link AccessibleObject#setAccessible(AccessibleObject[], boolean)} to
 * change the visibility of the fields. This will fail under a security manager, unless the appropriate permissions are
 * set up correctly.
 * </p>
 * <p>
 * Using reflection to access (private) fields circumvents any synchronization protection guarding access to these
 * fields. If a toString method cannot safely read a field, you should exclude it from the toString method, or use
 * synchronization consistent with the class' lock management around the invocation of the method. Take special care to
 * exclude non-thread-safe collection classes, because these classes may throw ConcurrentModificationException if
 * modified while the toString method is executing.
 * </p>
 * <p>
 * A typical invocation for this method would look like:
 * </p>
 * <pre>
 * public String toString() {
 *     return ReflectionToStringBuilder.toString(this);
 * }
 * You can also use the builder to debug 3rd party objects:
 * </pre>
 * <p>
 * A subclass can control field output by overriding the methods:
 * </p>
 * <ul>
 * <li>{@link #accept(Field)}</li>
 * <li>{@link #getValue(Field)}</li>
 * </ul>
 * <p>
 * For example, this method does <i>not</i> include the <code>password</code> field in the returned <code>String</code>:
 * </p>
 * <pre>
 * public String toString() {
 * return (new ReflectionToStringBuilder(this) {
 * protected boolean accept(Field f) {
 * return super.accept(f) &amp;&amp; !f.getName().equals(&quot;password&quot;);
 * }
 * }).toString();
 * }
 * </pre>
 * <p>
 * Alternatively the {@link ToStringExclude} annotation can be used to exclude fields from being incorporated in the
 * result.
 * </p>
 * <p>
 * It is also possible to use the {@link ToStringSummary} annotation to output the summary information instead of the
 * detailed information of a field.
 * </p>
 * <p>
 * The exact format of the <code>toString</code> is determined by the {@link ToStringStyle} passed into the constructor.
 * </p>
 *
 * <p>
 * <b>Note:</b> the default {@link ToStringStyle} will only do a "shallow" formatting, i.e. composed objects are not
 * further traversed. To get "deep" formatting, use an instance of {@link RecursiveToStringStyle}.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class ReflectionToStringBuilder extends ToStringBuilder {

    /**
     * Which field names to exclude from output. Intended for fields like <code>"password"</code>.
     *
     * @since 3.0.0
     */
    protected String[] excludeFieldNames;
    /**
     * Whether or not to append static fields.
     */
    private boolean appendStatics = false;
    /**
     * Whether or not to append transient fields.
     */
    private boolean appendTransients = false;
    /**
     * Whether or not to append fields that are null.
     */
    private boolean excludeNullValues;
    /**
     * The last super class to stop appending fields for.
     */
    private Class<?> upToClass = null;

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * <p>
     * This constructor outputs using the default style set with <code>setDefaultStyle</code>.
     * </p>
     *
     * @param object the Object to build a <code>toString</code> for, must not be <code>null</code>
     * @throws IllegalArgumentException if the Object passed in is <code>null</code>
     */
    public ReflectionToStringBuilder(final Object object) {
        super(checkNotNull(object));
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default style is used.
     * </p>
     *
     * @param object the Object to build a <code>toString</code> for, must not be <code>null</code>
     * @param style  the style of the <code>toString</code> to create, may be <code>null</code>
     * @throws IllegalArgumentException if the Object passed in is <code>null</code>
     */
    public ReflectionToStringBuilder(final Object object, final ToStringStyle style) {
        super(checkNotNull(object), style);
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default style is used.
     * </p>
     *
     * <p>
     * If the buffer is <code>null</code>, a new one is created.
     * </p>
     *
     * @param object the Object to build a <code>toString</code> for
     * @param style  the style of the <code>toString</code> to create, may be <code>null</code>
     * @param buffer the <code>StringBuffer</code> to populate, may be <code>null</code>
     * @throws IllegalArgumentException if the Object passed in is <code>null</code>
     */
    public ReflectionToStringBuilder(final Object object, final ToStringStyle style, final StringBuffer buffer) {
        super(checkNotNull(object), style, buffer);
    }

    /**
     * Constructor.
     *
     * @param <T>              the type of the object
     * @param object           the Object to build a <code>toString</code> for
     * @param style            the style of the <code>toString</code> to create, may be <code>null</code>
     * @param buffer           the <code>StringBuffer</code> to populate, may be <code>null</code>
     * @param reflectUpToClass the superclass to reflect up to (inclusive), may be <code>null</code>
     * @param outputTransients whether to include transient fields
     * @param outputStatics    whether to include static fields
     * @since 2.1.0
     */
    public <T> ReflectionToStringBuilder(
            final T object, final ToStringStyle style, final StringBuffer buffer,
            final Class<? super T> reflectUpToClass, final boolean outputTransients, final boolean outputStatics) {
        super(checkNotNull(object), style, buffer);
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
        this.setAppendStatics(outputStatics);
    }


    /**
     * Constructor.
     *
     * @param <T>               the type of the object
     * @param object            the Object to build a <code>toString</code> for
     * @param style             the style of the <code>toString</code> to create, may be <code>null</code>
     * @param buffer            the <code>StringBuffer</code> to populate, may be <code>null</code>
     * @param reflectUpToClass  the superclass to reflect up to (inclusive), may be <code>null</code>
     * @param outputTransients  whether to include transient fields
     * @param outputStatics     whether to include static fields
     * @param excludeNullValues whether to exclude fields who value is null
     */
    public <T> ReflectionToStringBuilder(
            final T object, final ToStringStyle style, final StringBuffer buffer,
            final Class<? super T> reflectUpToClass, final boolean outputTransients, final boolean outputStatics,
            final boolean excludeNullValues) {
        super(checkNotNull(object), style, buffer);
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
        this.setAppendStatics(outputStatics);
        this.setExcludeNullValues(excludeNullValues);
    }

    /**
     * <p>
     * Builds a <code>toString</code> value using the default <code>ToStringStyle</code> through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * Transient members will be not be included, as they are likely derived. Static fields will not be included.
     * Superclass fields will be appended.
     * </p>
     *
     * @param object the Object to be output
     * @return the String result
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     */
    public static String toString(final Object object) {
        return toString(object, null, false, false, null);
    }

    /**
     * <p>
     * Builds a <code>toString</code> value through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * Transient members will be not be included, as they are likely derived. Static fields will not be included.
     * Superclass fields will be appended.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default <code>ToStringStyle</code> is used.
     * </p>
     *
     * @param object the Object to be output
     * @param style  the style of the <code>toString</code> to create, may be <code>null</code>
     * @return the String result
     * @throws IllegalArgumentException if the Object or <code>ToStringStyle</code> is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     */
    public static String toString(final Object object, final ToStringStyle style) {
        return toString(object, style, false, false, null);
    }

    /**
     * <p>
     * Builds a <code>toString</code> value through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the <code>outputTransients</code> is <code>true</code>, transient members will be output, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the Object.
     * </p>
     *
     * <p>
     * Static fields will not be included. Superclass fields will be appended.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default <code>ToStringStyle</code> is used.
     * </p>
     *
     * @param object           the Object to be output
     * @param style            the style of the <code>toString</code> to create, may be <code>null</code>
     * @param outputTransients whether to include transient fields
     * @return the String result
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     */
    public static String toString(final Object object, final ToStringStyle style, final boolean outputTransients) {
        return toString(object, style, outputTransients, false, null);
    }

    /**
     * <p>
     * Builds a <code>toString</code> value through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the <code>outputTransients</code> is <code>true</code>, transient fields will be output, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the Object.
     * </p>
     *
     * <p>
     * If the <code>outputStatics</code> is <code>true</code>, static fields will be output, otherwise they are
     * ignored.
     * </p>
     *
     * <p>
     * Static fields will not be included. Superclass fields will be appended.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default <code>ToStringStyle</code> is used.
     * </p>
     *
     * @param object           the Object to be output
     * @param style            the style of the <code>toString</code> to create, may be <code>null</code>
     * @param outputTransients whether to include transient fields
     * @param outputStatics    whether to include static fields
     * @return the String result
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     * @since 2.1.0
     */
    public static String toString(final Object object, final ToStringStyle style, final boolean outputTransients, final boolean outputStatics) {
        return toString(object, style, outputTransients, outputStatics, null);
    }

    /**
     * <p>
     * Builds a <code>toString</code> value through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the <code>outputTransients</code> is <code>true</code>, transient fields will be output, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the Object.
     * </p>
     *
     * <p>
     * If the <code>outputStatics</code> is <code>true</code>, static fields will be output, otherwise they are
     * ignored.
     * </p>
     *
     * <p>
     * Superclass fields will be appended up to and including the specified superclass. A null superclass is treated as
     * <code>java.lang.Object</code>.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default <code>ToStringStyle</code> is used.
     * </p>
     *
     * @param <T>              the type of the object
     * @param object           the Object to be output
     * @param style            the style of the <code>toString</code> to create, may be <code>null</code>
     * @param outputTransients whether to include transient fields
     * @param outputStatics    whether to include static fields
     * @param reflectUpToClass the superclass to reflect up to (inclusive), may be <code>null</code>
     * @return the String result
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     * @since 2.1.0
     */
    public static <T> String toString(
            final T object, final ToStringStyle style, final boolean outputTransients,
            final boolean outputStatics, final Class<? super T> reflectUpToClass) {
        return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics)
                .toString();
    }

    /**
     * <p>
     * Builds a <code>toString</code> value through reflection.
     * </p>
     *
     * <p>
     * It uses <code>AccessibleObject.setAccessible</code> to gain access to private fields. This means that it will
     * throw a security exception if run under a security manager, if the permissions are not set up correctly. It is
     * also not as efficient as testing explicitly.
     * </p>
     *
     * <p>
     * If the <code>outputTransients</code> is <code>true</code>, transient fields will be output, otherwise they
     * are ignored, as they are likely derived fields, and not part of the value of the Object.
     * </p>
     *
     * <p>
     * If the <code>outputStatics</code> is <code>true</code>, static fields will be output, otherwise they are
     * ignored.
     * </p>
     *
     * <p>
     * Superclass fields will be appended up to and including the specified superclass. A null superclass is treated as
     * <code>java.lang.Object</code>.
     * </p>
     *
     * <p>
     * If the style is <code>null</code>, the default <code>ToStringStyle</code> is used.
     * </p>
     *
     * @param <T>               the type of the object
     * @param object            the Object to be output
     * @param style             the style of the <code>toString</code> to create, may be <code>null</code>
     * @param outputTransients  whether to include transient fields
     * @param outputStatics     whether to include static fields
     * @param excludeNullValues whether to exclude fields whose values are null
     * @param reflectUpToClass  the superclass to reflect up to (inclusive), may be <code>null</code>
     * @return the String result
     * @throws IllegalArgumentException if the Object is <code>null</code>
     * @see ToStringExclude
     * @see ToStringSummary
     */
    public static <T> String toString(
            final T object, final ToStringStyle style, final boolean outputTransients,
            final boolean outputStatics, final boolean excludeNullValues, final Class<? super T> reflectUpToClass) {
        return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics, excludeNullValues)
                .toString();
    }

    /**
     * Builds a String for a toString method excluding the given field names.
     *
     * @param object            The object to "toString".
     * @param excludeFieldNames The field names to exclude. Null excludes nothing.
     * @return The toString value.
     */
    public static String toStringExclude(final Object object, final Collection<String> excludeFieldNames) {
        return toStringExclude(object, toNoNullStringArray(excludeFieldNames));
    }

    /**
     * Converts the given Collection into an array of Strings. The returned array does not contain <code>null</code>
     * entries. Note that {@link Arrays#sort(Object[])} will throw an {@link NullPointerException} if an array element
     * is <code>null</code>.
     *
     * @param collection The collection to convert
     * @return A new array of Strings.
     */
    static String[] toNoNullStringArray(final Collection<String> collection) {
        if (collection == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return toNoNullStringArray(collection.toArray());
    }

    /**
     * Returns a new array of Strings without null elements. Internal method used to normalize exclude lists
     * (arrays and collections). Note that {@link Arrays#sort(Object[])} will throw an {@link NullPointerException}
     * if an array element is <code>null</code>.
     *
     * @param array The array to check
     * @return The given array or a new array without null.
     */
    static String[] toNoNullStringArray(final Object[] array) {
        final List<String> list = new ArrayList<>(array.length);
        for (final Object e : array) {
            if (e != null) {
                list.add(e.toString());
            }
        }
        return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * Builds a String for a toString method excluding the given field names.
     *
     * @param object            The object to "toString".
     * @param excludeFieldNames The field names to exclude
     * @return The toString value.
     */
    public static String toStringExclude(final Object object, final String... excludeFieldNames) {
        return new ReflectionToStringBuilder(object).setExcludeFieldNames(excludeFieldNames).toString();
    }

    private static Object checkNotNull(final Object obj) {
        Assert.isTrue(obj != null, "The Object passed in should not be null.");
        return obj;
    }

    /**
     * Returns whether or not to append the given <code>Field</code>.
     * <ul>
     * <li>Transient fields are appended only if {@link #isAppendTransients()} returns <code>true</code>.
     * <li>Static fields are appended only if {@link #isAppendStatics()} returns <code>true</code>.
     * <li>Inner class fields are not appended.</li>
     * </ul>
     *
     * @param field The Field to test.
     * @return Whether or not to append the given <code>Field</code>.
     */
    protected boolean accept(final Field field) {
        if (field.getName().indexOf(Symbol.C_DOLLAR) != -1) {
            // Reject field from inner class.
            return false;
        }
        if (Modifier.isTransient(field.getModifiers()) && !this.isAppendTransients()) {
            // Reject transient fields.
            return false;
        }
        if (Modifier.isStatic(field.getModifiers()) && !this.isAppendStatics()) {
            // Reject static fields.
            return false;
        }
        if (this.excludeFieldNames != null
                && Arrays.binarySearch(this.excludeFieldNames, field.getName()) >= 0) {
            // Reject fields from the getExcludeFieldNames list.
            return false;
        }
        return !field.isAnnotationPresent(ToStringExclude.class);
    }

    /**
     * <p>
     * Appends the fields and values defined by the given object of the given Class.
     * </p>
     *
     * <p>
     * If a cycle is detected as an object is &quot;toString()'ed&quot;, such an object is rendered as if
     * <code>Object.toString()</code> had been called and not implemented by the object.
     * </p>
     *
     * @param clazz The class of object parameter
     */
    protected void appendFieldsIn(final Class<?> clazz) {
        if (clazz.isArray()) {
            this.reflectionAppendArray(this.getObject());
            return;
        }
        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (final Field field : fields) {
            final String fieldName = field.getName();
            if (this.accept(field)) {
                try {
                    // Warning: Field.get(Object) creates wrappers objects
                    // for primitive types.
                    final Object fieldValue = this.getValue(field);
                    if (!excludeNullValues || fieldValue != null) {
                        this.append(fieldName, fieldValue, !field.isAnnotationPresent(ToStringSummary.class));
                    }
                } catch (final IllegalAccessException ex) {
                    //this can't happen. Would get a Security exception
                    // instead
                    //throw a runtime exception in case the impossible
                    // happens.
                    throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * @return Returns the excludeFieldNames.
     */
    public String[] getExcludeFieldNames() {
        return this.excludeFieldNames.clone();
    }

    /**
     * Sets the field names to exclude.
     *
     * @param excludeFieldNamesParam The excludeFieldNames to excluding from toString or <code>null</code>.
     * @return <code>this</code>
     */
    public ReflectionToStringBuilder setExcludeFieldNames(final String... excludeFieldNamesParam) {
        if (excludeFieldNamesParam == null) {
            this.excludeFieldNames = null;
        } else {
            //clone and remove nulls
            this.excludeFieldNames = toNoNullStringArray(excludeFieldNamesParam);
            Arrays.sort(this.excludeFieldNames);
        }
        return this;
    }

    /**
     * <p>
     * Gets the last super class to stop appending fields for.
     * </p>
     *
     * @return The last super class to stop appending fields for.
     */
    public Class<?> getUpToClass() {
        return this.upToClass;
    }

    /**
     * <p>
     * Sets the last super class to stop appending fields for.
     * </p>
     *
     * @param clazz The last super class to stop appending fields for.
     */
    public void setUpToClass(final Class<?> clazz) {
        if (clazz != null) {
            final Object object = getObject();
            if (object != null && !clazz.isInstance(object)) {
                throw new IllegalArgumentException("Specified class is not a superclass of the object");
            }
        }
        this.upToClass = clazz;
    }

    /**
     * <p>
     * Calls <code>java.lang.reflect.Field.get(Object)</code>.
     * </p>
     *
     * @param field The Field to query.
     * @return The Object from the given Field.
     * @throws IllegalArgumentException see {@link Field#get(Object)}
     * @throws IllegalAccessException   see {@link Field#get(Object)}
     * @see Field#get(Object)
     */
    protected Object getValue(final Field field) throws IllegalAccessException {
        return field.get(this.getObject());
    }

    /**
     * <p>
     * Gets whether or not to append static fields.
     * </p>
     *
     * @return Whether or not to append static fields.
     * @since 2.1.0
     */
    public boolean isAppendStatics() {
        return this.appendStatics;
    }

    /**
     * <p>
     * Sets whether or not to append static fields.
     * </p>
     *
     * @param appendStatics Whether or not to append static fields.
     * @since 2.1.0
     */
    public void setAppendStatics(final boolean appendStatics) {
        this.appendStatics = appendStatics;
    }

    /**
     * <p>
     * Gets whether or not to append transient fields.
     * </p>
     *
     * @return Whether or not to append transient fields.
     */
    public boolean isAppendTransients() {
        return this.appendTransients;
    }

    /**
     * <p>
     * Sets whether or not to append transient fields.
     * </p>
     *
     * @param appendTransients Whether or not to append transient fields.
     */
    public void setAppendTransients(final boolean appendTransients) {
        this.appendTransients = appendTransients;
    }

    /**
     * <p>
     * Gets whether or not to append fields whose values are null.
     * </p>
     *
     * @return Whether or not to append fields whose values are null.
     */
    public boolean isExcludeNullValues() {
        return this.excludeNullValues;
    }

    /**
     * <p>
     * Sets whether or not to append fields whose values are null.
     * </p>
     *
     * @param excludeNullValues Whether or not to append fields whose values are null.
     */
    public void setExcludeNullValues(final boolean excludeNullValues) {
        this.excludeNullValues = excludeNullValues;
    }

    /**
     * <p>
     * Append to the <code>toString</code> an <code>Object</code> array.
     * </p>
     *
     * @param array the array to add to the <code>toString</code>
     * @return this
     */
    public ReflectionToStringBuilder reflectionAppendArray(final Object array) {
        this.getStyle().reflectionAppendArrayDetail(this.getStringBuffer(), null, array);
        return this;
    }

    /**
     * <p>
     * Gets the String built by this builder.
     * </p>
     *
     * @return the built string
     */
    @Override
    public String toString() {
        if (this.getObject() == null) {
            return this.getStyle().getNullText();
        }
        Class<?> clazz = this.getObject().getClass();
        this.appendFieldsIn(clazz);
        while (clazz.getSuperclass() != null && clazz != this.getUpToClass()) {
            clazz = clazz.getSuperclass();
            this.appendFieldsIn(clazz);
        }
        return super.toString();
    }

}
