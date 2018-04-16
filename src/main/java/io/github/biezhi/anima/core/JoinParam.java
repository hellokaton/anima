package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.core.functions.TypeFunction;
import io.github.biezhi.anima.utils.AnimaUtils;
import lombok.Data;

/**
 * @author biezhi
 * @date 2018/4/16
 */
@Data
public class JoinParam {

    private Class<? extends Model> joinModel;
    private String                 onLeft;
    private String                 onRight;
    private String                 fieldName;

    public JoinParam(Class<? extends Model> joinModel) {
        this.joinModel = joinModel;
    }

    public <T, R> JoinParam as(TypeFunction<T, R> function) {
        String fieldName = AnimaUtils.getLambdaColumnName(function);
        this.setFieldName(fieldName);
        return this;
    }

    public <T, S extends Model, R> JoinParam on(TypeFunction<T, R> left, TypeFunction<S, R> right) {
        String onLeft  = AnimaUtils.getLambdaColumnName(left);
        String onRight = AnimaUtils.getLambdaColumnName(right);
        this.setOnLeft(onLeft);
        this.setOnRight(onRight);
        return this;
    }

}
