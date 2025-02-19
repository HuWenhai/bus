package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.io.FastByteArray;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统运行时工具类
 * 用于执行系统命令的工具
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class RuntimeUtils {

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param cmds 命令列表,每个元素代表一条命令
     * @return 执行结果
     * @throws InstrumentException IO异常
     */
    public static String execForStr(String... cmds) throws InstrumentException {
        return execForStr(CharsetUtils.systemCharset(), cmds);
    }

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param charset 编码
     * @param cmds    命令列表,每个元素代表一条命令
     * @return 执行结果
     * @throws InstrumentException 内部处理异常
     * @since 3.1.2
     */
    public static String execForStr(Charset charset, String... cmds) throws InstrumentException {
        return getResult(exec(cmds), charset);
    }

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param cmds 命令列表,每个元素代表一条命令
     * @return 执行结果, 按行区分
     * @throws InstrumentException 内部处理异常
     */
    public static List<String> execForLines(String... cmds) throws InstrumentException {
        return execForLines(CharsetUtils.systemCharset(), cmds);
    }

    /**
     * 执行系统命令,使用系统默认编码
     *
     * @param charset 编码
     * @param cmds    命令列表,每个元素代表一条命令
     * @return 执行结果, 按行区分
     * @throws InstrumentException 内部处理异常
     * @since 3.1.2
     */
    public static List<String> execForLines(Charset charset, String... cmds) throws InstrumentException {
        return getResultLines(exec(cmds), charset);
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String... cmds) {
        if (ArrayUtils.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringUtils.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringUtils.splitToArray(cmd, Symbol.C_SPACE);
        }

        Process process;
        try {
            process = new ProcessBuilder(cmds).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return process;
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数,传入形式为key=value,null表示继承系统环境变量
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, String... cmds) {
        return exec(envp, null, cmds);
    }

    /**
     * 执行命令
     * 命令带参数时参数可作为其中一个参数,也可以将命令和参数组合为一个字符串传入
     *
     * @param envp 环境变量参数,传入形式为key=value,null表示继承系统环境变量
     * @param dir  执行命令所在目录（用于相对路径命令执行）,null表示使用当前进程执行的目录
     * @param cmds 命令
     * @return {@link Process}
     */
    public static Process exec(String[] envp, File dir, String... cmds) {
        if (ArrayUtils.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StringUtils.isBlank(cmd)) {
                throw new NullPointerException("Command is empty !");
            }
            cmds = StringUtils.splitToArray(cmd, Symbol.C_SPACE);
        }
        try {
            return Runtime.getRuntime().exec(cmds, envp, dir);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取命令执行结果,使用系统默认编码,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static List<String> getResultLines(Process process) {
        return getResultLines(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行结果,使用系统默认编码,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static List<String> getResultLines(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoUtils.readLines(in, charset, new ArrayList<String>());
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行结果,使用系统默认编码,,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static String getResult(Process process) {
        return getResult(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行结果,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     * @since 3.1.2
     */
    public static String getResult(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoUtils.read(in, charset);
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 获取命令执行异常结果,使用系统默认编码,,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process) {
        return getErrorResult(process, CharsetUtils.systemCharset());
    }

    /**
     * 获取命令执行异常结果,获取后销毁进程
     *
     * @param process {@link Process} 进程
     * @param charset 编码
     * @return 命令执行结果列表
     */
    public static String getErrorResult(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getErrorStream();
            return IoUtils.read(in, charset);
        } finally {
            IoUtils.close(in);
            destroy(process);
        }
    }

    /**
     * 销毁进程
     *
     * @param process 进程
     * @since 3.1.2
     */
    public static void destroy(Process process) {
        if (null != process) {
            process.destroy();
        }
    }

    /**
     * 增加一个JVM关闭后的钩子,用于在JVM关闭时执行某些操作
     *
     * @param hook 钩子
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     * 获得完整消息,包括异常名,消息格式为：{SimpleClassName}: {ThrowableMessage}
     *
     * @param e 异常
     * @return 完整消息
     */
    public static String getMessage(Throwable e) {
        if (null == e) {
            return Normal.NULL;
        }
        return StringUtils.format("{}: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    /**
     * 获得消息,调用异常类的getMessage方法
     *
     * @param e 异常
     * @return 消息
     */
    public static String getSimpleMessage(Throwable e) {
        return (null == e) ? Normal.NULL : e.getMessage();
    }

    /**
     * 使用运行时异常包装编译异常
     * <p>
     * 如果
     *
     * @param throwable 异常
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntime(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

    /**
     * 包装一个异常
     *
     * @param <T>           对象
     * @param throwable     异常
     * @param wrapThrowable 包装后的异常类
     * @return 包装后的异常
     * @since 3.3.0
     */
    public static <T extends Throwable> T wrap(Throwable throwable, Class<T> wrapThrowable) {
        if (wrapThrowable.isInstance(throwable)) {
            return (T) throwable;
        }
        return ReflectUtils.newInstance(wrapThrowable, throwable);
    }

    /**
     * 包装异常并重新抛出此异常
     * {@link RuntimeException} 和{@link Error} 直接抛出,其它检查异常包装为{@link UndeclaredThrowableException} 后抛出
     *
     * @param throwable 异常
     */
    public static void wrapAndThrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new UndeclaredThrowableException(throwable);
    }

    /**
     * 剥离反射引发的InvocationTargetException、
     * UndeclaredThrowableException中间异常,返回业务本身的异常
     *
     * @param wrapped 包装的异常
     * @return 剥离后的异常
     */
    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * 获取当前栈信息
     *
     * @return 当前栈信息
     */
    public static StackTraceElement[] getStackElements() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * 获取指定层的堆栈信息
     *
     * @param layers 堆栈层级
     * @return 指定层的堆栈信息
     */
    public static StackTraceElement getStackElement(int layers) {
        return getStackElements()[layers];
    }

    /**
     * 获取入口堆栈信息
     *
     * @return 入口堆栈信息
     */
    public static StackTraceElement getRootStackElement() {
        final StackTraceElement[] stackElements = getStackElements();
        return stackElements[stackElements.length - 1];
    }

    /**
     * 堆栈转为单行完整字符串
     *
     * @param throwable 异常对象
     * @return 堆栈转为的字符串
     */
    public static String getStackTraceOneLine(Throwable throwable) {
        return getStackTraceOneLine(throwable, 3000);
    }

    /**
     * 堆栈转为单行完整字符串
     *
     * @param throwable 异常对象
     * @param limit     限制最大长度
     * @return 堆栈转为的字符串
     */
    public static String getStackTraceOneLine(Throwable throwable, int limit) {
        Map<Character, String> replaceCharToStrMap = new HashMap<>();
        replaceCharToStrMap.put(Symbol.C_CR, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_LF, Symbol.SPACE);
        replaceCharToStrMap.put(Symbol.C_TAB, Symbol.SPACE);

        return getStackTrace(throwable, limit, replaceCharToStrMap);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable 异常对象
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable) {
        return getStackTrace(throwable, 3000);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable 异常对象
     * @param limit     限制最大长度
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable, int limit) {
        return getStackTrace(throwable, limit, null);
    }

    /**
     * 堆栈转为完整字符串
     *
     * @param throwable           异常对象
     * @param limit               限制最大长度
     * @param replaceCharToStrMap 替换字符为指定字符串
     * @return 堆栈转为的字符串
     */
    public static String getStackTrace(Throwable throwable, int limit, Map<Character, String> replaceCharToStrMap) {
        final FastByteArray baos = new FastByteArray();
        throwable.printStackTrace(new PrintStream(baos));
        String exceptionStr = baos.toString();
        int length = exceptionStr.length();
        if (limit > 0 && limit < length) {
            length = limit;
        }

        if (CollUtils.isNotEmpty(replaceCharToStrMap)) {
            final StringBuilder sb = StringUtils.builder();
            char c;
            String value;
            for (int i = 0; i < length; i++) {
                c = exceptionStr.charAt(i);
                value = replaceCharToStrMap.get(c);
                if (null != value) {
                    sb.append(value);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return StringUtils.subPre(exceptionStr, limit);
        }
    }

    /**
     * 判断是否由指定异常类引起
     *
     * @param throwable    异常
     * @param causeClasses 定义的引起异常的类
     * @return 是否由指定异常类引起
     */
    public static boolean isCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        return null != getCausedBy(throwable, causeClasses);
    }

    /**
     * 获取由指定异常类引起的异常
     *
     * @param throwable    异常
     * @param causeClasses 定义的引起异常的类
     * @return 是否由指定异常类引起
     */
    public static Throwable getCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        Throwable cause = throwable;
        while (cause != null) {
            for (Class<? extends Exception> causeClass : causeClasses) {
                if (causeClass.isInstance(cause)) {
                    return cause;
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * 判断指定异常是否来自或者包含指定异常
     *
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @return true 来自或者包含
     */
    public static boolean isFromOrSuppressedThrowable(Throwable throwable, Class<? extends Throwable> exceptionClass) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, true) != null;
    }

    /**
     * 判断指定异常是否来自或者包含指定异常
     *
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @param checkCause     判断cause
     * @return true 来自或者包含
     */
    public static boolean isFromOrSuppressedThrowable(Throwable throwable, Class<? extends Throwable> exceptionClass, boolean checkCause) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, checkCause) != null;
    }

    /**
     * 转化指定异常为来自或者包含指定异常
     *
     * @param <T>            异常类型
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @return 结果为null 不是来自或者包含
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(Throwable throwable, Class<T> exceptionClass) {
        return convertFromOrSuppressedThrowable(throwable, exceptionClass, true);
    }

    /**
     * 转化指定异常为来自或者包含指定异常
     *
     * @param <T>            异常类型
     * @param throwable      异常
     * @param exceptionClass 定义的引起异常的类
     * @param checkCause     判断cause
     * @return 结果为null 不是来自或者包含
     */
    public static <T extends Throwable> T convertFromOrSuppressedThrowable(Throwable throwable, Class<T> exceptionClass, boolean checkCause) {
        if (throwable == null || exceptionClass == null) {
            return null;
        }
        if (exceptionClass.isAssignableFrom(throwable.getClass())) {
            return (T) throwable;
        }
        if (checkCause) {
            Throwable cause = throwable.getCause();
            if (cause != null && exceptionClass.isAssignableFrom(cause.getClass())) {
                return (T) cause;
            }
        }
        Throwable[] throwables = throwable.getSuppressed();
        if (ArrayUtils.isNotEmpty(throwables)) {
            for (Throwable throwable1 : throwables) {
                if (exceptionClass.isAssignableFrom(throwable1.getClass())) {
                    return (T) throwable1;
                }
            }
        }
        return null;
    }

    /**
     * 获取异常链上所有异常的集合,如果{@link Throwable} 对象没有cause,返回只有一个节点的List
     * 如果传入null,返回空集合
     *
     * @param throwable 异常对象,可以为null
     * @return 异常链中所有异常集合
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && false == list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * 获取异常链中最尾端的异常,即异常最早发生的异常对象
     * 此方法通过调用{@link Throwable#getCause()} 直到没有cause为止,如果异常本身没有cause,返回异常本身
     * 传入null返回也为null
     *
     * @param throwable 异常对象,可能为null
     * @return 最尾端异常, 传入null参数返回也为null
     */
    public static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.size() < 1 ? null : list.get(list.size() - 1);
    }

    /**
     * 获取异常链中最尾端的异常的消息,
     * 消息格式为：{SimpleClassName}: {ThrowableMessage}
     *
     * @param th 异常
     * @return 消息
     */
    public static String getRootCauseMessage(final Throwable th) {
        return getMessage(getRootCause(th));
    }

}
