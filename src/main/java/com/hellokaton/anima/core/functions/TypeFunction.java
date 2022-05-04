package com.hellokaton.anima.core.functions;

import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface TypeFunction<T, R> extends Serializable, Function<T, R> {

}