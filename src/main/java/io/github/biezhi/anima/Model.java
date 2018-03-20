package io.github.biezhi.anima;

import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.functions.TypeFunction;

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

    public <T extends Model> AnimaQuery<T> set(String column, Object value) {
        return new AnimaQuery(this.getClass()).set(column, value);
    }

    public <T extends Model, R> AnimaQuery<T> set(TypeFunction<T, R> function, Object value) {
        return new AnimaQuery(this.getClass()).set(function, value);
    }

    public <T extends Model> AnimaQuery<T> where(String statement, Object value) {
        return new AnimaQuery(this.getClass()).where(statement, value);
    }

    public <T extends Model, R> AnimaQuery<T> where(TypeFunction<T, R> function, Object value) {
        return new AnimaQuery(this.getClass()).where(function, value);
    }

}
