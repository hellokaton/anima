/**
 * Copyright (c) 2018, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.biezhi.anima.core;

import lombok.NoArgsConstructor;

import java.util.function.Consumer;

/**
 * Atomic
 * <p>
 * Used to save the exception information after the end of a transaction.
 * There is currently no other storage.
 * You can catch and handle them after an exception occurs.
 *
 * @author biezhi
 * @date 2018/3/15
 */
@NoArgsConstructor
public class Atomic {

    private Exception e;
    private boolean   isRollback;

    public Atomic(Exception e) {
        this.e = e;
    }

    public static Atomic ok() {
        return new Atomic();
    }

    public static Atomic error(Exception e) {
        return new Atomic(e);
    }

    public Atomic rollback(boolean isRollback) {
        this.isRollback = isRollback;
        return this;
    }

    public boolean isRollback() {
        return isRollback;
    }
    
    public Atomic catchException(Consumer<Exception> consumer) {
        if (null != e) {
            consumer.accept(e);
        }
        return this;
    }
}
