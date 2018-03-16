package io.github.biezhi.anima;

import io.github.biezhi.anima.core.JavaRecord;
import io.github.biezhi.anima.core.ResultKey;

import java.io.Serializable;

/**
 * Base Model
 *
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

    public int update(Serializable id) {
        return new JavaRecord(this.getClass()).updateById(id);
    }

    public int delete() {
        return new JavaRecord(this.getClass()).deleteByModel(this);
    }

    public int deleteById(Serializable id) {
        return new JavaRecord(this.getClass()).deleteById(id);
    }

    public JavaRecord set(String column, Object value) {
        return new JavaRecord(this.getClass()).set(column, value);
    }

    public JavaRecord where(String statement, Object value) {
        return new JavaRecord(this.getClass()).where(statement, value);
    }

}
