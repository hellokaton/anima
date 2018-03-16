package io.github.biezhi.anima.core;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/3/16
 */
public class ResultList<T> {

    private List<T> list;

    public ResultList(List<T> list) {
        this.list = list;
    }

    public T one() {
        if (null == list || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<T> all() {
        return list;
    }
}
