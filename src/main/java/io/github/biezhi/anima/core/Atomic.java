package io.github.biezhi.anima.core;

import lombok.NoArgsConstructor;

import java.util.function.Consumer;

/**
 * Transaction
 *
 * @author biezhi
 * @date 2018/3/15
 */
@NoArgsConstructor
public class Atomic {

    private Exception e;

    public Atomic(Exception e) {
        this.e = e;
    }

    public static Atomic ok() {
        return new Atomic();
    }

    public static Atomic error(Exception e) {
        return new Atomic(e);
    }

    public <T extends Exception> void catchException(Consumer<T> consumer) {
        if (null != e) {
            consumer.accept((T) e);
        }
    }
}
