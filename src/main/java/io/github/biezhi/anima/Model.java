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

    private AnimaQuery<? extends Model> animaQuery = new AnimaQuery<>(this.getClass());

    public ResultKey save() {
        return animaQuery.save(this);
    }

    public int update() {
        return animaQuery.updateByModel(this);
    }

    public int updateById(Serializable id) {
        return new AnimaQuery<>(this.getClass()).updateById(this, id);
    }

    public int delete() {
        return animaQuery.deleteByModel(this);
    }

    public AnimaQuery<? extends Model> set(String column, Object value) {
        return animaQuery.set(column, value);
    }

    public <T extends Model, R> AnimaQuery<? extends Model> set(TypeFunction<T, R> function, Object value) {
        return animaQuery.set(function, value);
    }

    public AnimaQuery<? extends Model> where(String statement, Object value) {
        return animaQuery.where(statement, value);
    }

    public <T extends Model, R> AnimaQuery<? extends Model> where(TypeFunction<T, R> function, Object value) {
        return animaQuery.where(function, value);
    }

}
