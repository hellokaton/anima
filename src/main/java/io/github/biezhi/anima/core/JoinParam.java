package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.core.functions.TypeFunction;
import io.github.biezhi.anima.enums.OrderBy;
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
    private String                 orderBy;

    public JoinParam(Class<? extends Model> joinModel) {
        this.joinModel = joinModel;
    }

    public <T, R> JoinParam as(TypeFunction<T, R> function) {
        String fieldName = AnimaUtils.getLambdaColumnName(function);
        this.setFieldName(fieldName);
        return this;
    }

    public <T, S extends Model, R> JoinParam on(TypeFunction<T, R> left, TypeFunction<S, R> right) {
        String onLeft  = AnimaUtils.getLambdaFieldName(left);
        String onRight = AnimaUtils.getLambdaColumnName(right);
        this.setOnLeft(onLeft);
        this.setOnRight(onRight);
        return this;
    }

    public <S extends Model, R> JoinParam order(TypeFunction<S, R> rightField, OrderBy orderBy) {
        String columnName = AnimaUtils.getLambdaColumnName(rightField);
        this.orderBy = columnName + " " + orderBy.name();
        return this;
    }

    public JoinParam order(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }
}
