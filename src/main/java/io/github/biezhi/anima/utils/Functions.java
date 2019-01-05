package io.github.biezhi.anima.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * Functions
 *
 * @author biezhi
 * @date 2018-12-20
 */
@UtilityClass
public class Functions {

    public static <K, T extends Exception> void ifNullThrow(K k, T e) throws T {
        ifThrow(k == null, e);
    }

    public static <T extends Exception> void ifThrow(boolean flag, T e) throws T {
        if (flag) {
            throw e;
        }
    }


    public static void ifNullThen(Object value, Runnable runnable) {
        ifThen(value == null, runnable);
    }

    public static void ifNotNullThen(Object value, Runnable runnable) {
        ifThen(value != null, runnable);
    }

    public static <T extends Runnable> void ifThen(boolean flag, T t) {
        if (flag) {
            t.run();
        }
    }

    public static <T extends Runnable> void ifThen(boolean flag, T t1, T t2) {
        if (flag) {
            t1.run();
        } else {
            t2.run();
        }
    }

    public static <K, T> T ifNotNullReturn(K value, T t1, T t2) {
        return ifReturn(value != null, t1, t2);
    }

    public static <T> T ifNotNullReturn(T value, Supplier<T> supplier) {
        if (null != value) {
            return value;
        }
        return supplier.get();
    }

    public static <T> T ifReturn(boolean flag, T t1, T t2) {
        return (flag ? t1 : t2);
    }

    public static <T> T ifReturn(boolean flag, Supplier<T> s1, Supplier<T> s2) {
        if (flag) {
            return s1.get();
        }
        return s2.get();
    }

    public static <T> T ifReturnOrThrow(boolean flag, T t1, RuntimeException e) {
        if (flag) return t1;
        throw e;
    }

}
