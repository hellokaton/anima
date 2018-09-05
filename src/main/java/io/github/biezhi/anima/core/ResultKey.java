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

import java.math.BigInteger;

/**
 * Result Key
 * <p>
 * Stores the return value of the primary key when the data is saved
 *
 * @author biezhi
 * @date 2018/3/16
 */
public class ResultKey {

    private Object key;

    public ResultKey(Object key) {
        this.key = key;
    }

    public Integer asInt() {
        if (key instanceof Long) {
            return asLong().intValue();
        }
        if (key instanceof BigInteger) {
            return asBigInteger().intValue();
        }
        return (Integer) key;
    }


    public Long asLong() {
        return (Long) key;
    }

    public BigInteger asBigInteger() {
        return (BigInteger) key;
    }

    public String asString() {
        return key.toString();
    }

}
