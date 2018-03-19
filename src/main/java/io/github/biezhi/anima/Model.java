package io.github.biezhi.anima;

import io.github.biezhi.anima.core.AnimaQuery;
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
        return new AnimaQuery(this.getClass()).save(this);
    }

    public int update() {
        return new AnimaQuery(this.getClass()).updateByModel(this);
    }

    public int updateById(Serializable id) {
        return new AnimaQuery(this.getClass()).updateById(this, id);
    }

    public int delete() {
        return new AnimaQuery(this.getClass()).deleteByModel(this);
    }

    public AnimaQuery set(String column, Object value) {
        return new AnimaQuery(this.getClass()).set(column, value);
    }

    public AnimaQuery where(String statement, Object value) {
        return new AnimaQuery(this.getClass()).where(statement, value);
    }

}
