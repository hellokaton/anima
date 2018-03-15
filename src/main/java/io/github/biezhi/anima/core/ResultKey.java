package io.github.biezhi.anima.core;

/**
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
        return (Integer) key;
    }

    public Long asLong() {
        return (Long) key;
    }

}
