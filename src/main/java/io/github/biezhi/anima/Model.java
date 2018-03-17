package io.github.biezhi.anima;

import io.github.biezhi.anima.core.AnimaDB;
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
        return new AnimaDB(this.getClass()).save(this);
    }

    public int update() {
        return new AnimaDB(this.getClass()).updateByModel(this);
    }

    public int updateById(Serializable id) {
        return new AnimaDB(this.getClass()).updateById(this, id);
    }

    public int delete() {
        return new AnimaDB(this.getClass()).deleteByModel(this);
    }

    public AnimaDB set(String column, Object value) {
        return new AnimaDB(this.getClass()).set(column, value);
    }

    public AnimaDB where(String statement, Object value) {
        return new AnimaDB(this.getClass()).where(statement, value);
    }

}
