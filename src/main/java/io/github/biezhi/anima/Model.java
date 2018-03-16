package io.github.biezhi.anima;

import io.github.biezhi.anima.core.JavaRecord;
import io.github.biezhi.anima.core.ResultKey;

/**
 * @author biezhi
 * @date 2018/3/16
 */
public class Model {

    public ResultKey save() {
        return new JavaRecord(this.getClass()).save(this);
    }

    public int update() {
        return new JavaRecord(this.getClass()).updateByModel(this);
    }

    public int delete() {
        return new JavaRecord(this.getClass()).deleteByModel(this);
    }

    public JavaRecord set(String column, Object value) {
        return new JavaRecord(this.getClass()).set(column, value);
    }

}
