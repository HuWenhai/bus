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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.consts.FileType;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Scaner {

    /**
     * 包名
     */
    private String packageName;
    /**
     * 包名,最后跟一个点,表示包名,避免在检查前缀时的歧义
     */
    private String packageNameWithDot;
    /**
     * 包路径,用于文件中对路径操作
     */
    private String packageDirName;
    /**
     * 包路径,用于jar中对路径操作,在Linux下与packageDirName一致
     */
    private String packagePath;
    /**
     * 过滤器
     */
    private Filter<Class<?>> classFilter;
    /**
     * 编码
     */
    private Charset charset;
    /**
     * 是否初始化类
     */
    private boolean initialize;

    private Set<Class<?>> classes = new HashSet<Class<?>>();

    /**
     * 构造,默认UTF-8编码
     */
    public Scaner() {
        this(null);
    }

    /**
     * 构造,默认UTF-8编码
     *
     * @param packageName 包名,所有包传入""或者null
     */
    public Scaner(String packageName) {
        this(packageName, null);
    }

    /**
     * 构造,默认UTF-8编码
     *
     * @param packageName 包名,所有包传入""或者null
     * @param classFilter 过滤器,无需传入null
     */
    public Scaner(String packageName, Filter<Class<?>> classFilter) {
        this(packageName, classFilter, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param packageName 包名,所有包传入""或者null
     * @param classFilter 过滤器,无需传入null
     * @param charset     编码
     */
    public Scaner(String packageName, Filter<Class<?>> classFilter, Charset charset) {
        packageName = StringUtils.nullToEmpty(packageName);
        this.packageName = packageName;
        this.packageNameWithDot = StringUtils.addSuffixIfNot(packageName, Symbol.DOT);
        this.packageDirName = packageName.replace(Symbol.C_DOT, File.separatorChar);
        this.packagePath = packageName.replace(Symbol.C_DOT, Symbol.C_SLASH);
        this.classFilter = classFilter;
        this.charset = charset;
    }

    /**
     * 扫描指定包路径下所有包含指定注解的类
     *
     * @param packageName     包路径
     * @param annotationClass 注解类
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageByAnnotation(String packageName, final Class<? extends Annotation> annotationClass) {
        return scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> clazz) {
                return clazz.isAnnotationPresent(annotationClass);
            }
        });
    }

    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类
     *
     * @param packageName 包路径
     * @param superClass  父类或接口
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageBySuper(String packageName, final Class<?> superClass) {
        return scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> clazz) {
                return superClass.isAssignableFrom(clazz) && !superClass.equals(clazz);
            }
        });
    }

    /**
     * 扫面该包路径下所有class文件
     *
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage() {
        return scanPackage(Normal.EMPTY, null);
    }

    /**
     * 扫面该包路径下所有class文件
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(String packageName) {
        return scanPackage(packageName, null);
    }

    /**
     * 扫面包路径下满足class过滤器条件的所有class文件,
     * 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException
     * 因为className 应该为 com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器,过滤掉不需要的class
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(String packageName, Filter<Class<?>> classFilter) {
        return new Scaner(packageName, classFilter).scan();
    }

    /**
     * 扫面包路径下满足class过滤器条件的所有class文件
     *
     * @return 类集合
     */
    public Set<Class<?>> scan() {
        for (URL url : ResourceUtils.getResourceIter(this.packagePath)) {
            switch (url.getProtocol()) {
                case "file":
                    scanFile(new File(UriUtils.decode(url.getFile(), this.charset.name())), null);
                    break;
                case "jar":
                    scanJar(UriUtils.getJarFile(url));
                    break;
            }
        }

        if (CollUtils.isEmpty(this.classes)) {
            scanJavaClassPaths();
        }

        return Collections.unmodifiableSet(this.classes);
    }

    /**
     * 设置是否在扫描到类时初始化类
     *
     * @param initialize 是否初始化类
     */
    public void setInitialize(boolean initialize) {
        this.initialize = initialize;
    }

    /**
     * 扫描Java指定的ClassPath路径
     *
     * @return 扫描到的类
     */
    private void scanJavaClassPaths() {
        final String[] javaClassPaths = ClassUtils.getJavaClassPaths();
        for (String classPath : javaClassPaths) {
            // bug修复,由于路径中空格和中文导致的Jar找不到
            classPath = UriUtils.decode(classPath, CharsetUtils.systemCharsetName());

            scanFile(new File(classPath), null);
        }
    }

    /**
     * 扫描文件或目录中的类
     *
     * @param file    文件或目录
     * @param rootDir 包名对应classpath绝对路径
     */
    private void scanFile(File file, String rootDir) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileType.CLASS)) {
                final String className = fileName//
                        // 8为classes长度,fileName.length() - 6为".class"的长度
                        .substring(rootDir.length(), fileName.length() - 6)//
                        .replace(File.separatorChar, Symbol.C_DOT);//
                //加入满足条件的类
                addIfAccept(className);
            } else if (fileName.endsWith(FileType.JAR)) {
                try {
                    scanJar(new JarFile(file));
                } catch (IOException e) {
                    throw new InstrumentException(e);
                }
            }
        } else if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
            }
        }
    }


    /**
     * 扫描jar包
     *
     * @param jar jar包
     */
    private void scanJar(JarFile jar) {
        String name;
        for (JarEntry entry : new IterUtils.EnumerationIter<>(jar.entries())) {
            name = StringUtils.removePrefix(entry.getName(), Symbol.SLASH);
            if (name.startsWith(this.packagePath)) {
                if (name.endsWith(FileType.CLASS) && false == entry.isDirectory()) {
                    final String className = name//
                            .substring(0, name.length() - 6)//
                            .replace(Symbol.C_SLASH, Symbol.C_DOT);//
                    addIfAccept(loadClass(className));
                }
            }
        }
    }

    /**
     * 加载类
     *
     * @param className 类名
     * @return 加载的类
     */
    private Class<?> loadClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, this.initialize, ClassUtils.getClassLoader());
        } catch (NoClassDefFoundError e) {
            // 由于依赖库导致的类无法加载,直接跳过此类
        } catch (UnsupportedClassVersionError e) {
            // 版本导致的不兼容的类,跳过
        } catch (Exception e) {
            throw new RuntimeException(e);
            // Console.error(e);
        }
        return clazz;
    }

    /**
     * 通过过滤器,是否满足接受此类的条件
     *
     * @param className 类
     * @return 是否接受
     */
    private void addIfAccept(String className) {
        if (StringUtils.isBlank(className)) {
            return;
        }
        int classLen = className.length();
        int packageLen = this.packageName.length();
        if (classLen == packageLen) {
            //类名和包名长度一致,用户可能传入的包名是类名
            if (className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if (classLen > packageLen) {
            //检查类名是否以指定包名为前缀,包名后加.
            if (className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    /**
     * 通过过滤器,是否满足接受此类的条件
     *
     * @param clazz 类
     * @return 是否接受
     */
    private void addIfAccept(Class<?> clazz) {
        if (null != clazz) {
            Filter<Class<?>> classFilter = this.classFilter;
            if (classFilter == null || classFilter.accept(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    /**
     * 截取文件绝对路径中包名之前的部分
     *
     * @param file 文件
     * @return 包名之前的部分
     */
    private String subPathBeforePackage(File file) {
        String filePath = file.getAbsolutePath();
        if (StringUtils.isNotEmpty(this.packageDirName)) {
            filePath = StringUtils.subBefore(filePath, this.packageDirName, true);
        }
        return StringUtils.addSuffixIfNot(filePath, File.separator);
    }

}
