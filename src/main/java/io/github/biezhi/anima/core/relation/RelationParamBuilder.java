package io.github.biezhi.anima.core.relation;

import io.github.biezhi.anima.annotation.BelongsTo;
import io.github.biezhi.anima.annotation.HasMany;
import io.github.biezhi.anima.annotation.HasOne;
import io.github.biezhi.anima.core.AnimaCache;
import io.github.biezhi.anima.exception.AnimaException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author biezhi
 * @date 2018/3/19
 */
public class RelationParamBuilder {

    public static RelationParams buildBelongsTo(Field field) {
        Class<?>  type       = field.getType();
        BelongsTo belongsTo  = field.getAnnotation(BelongsTo.class);
        String    foreignKey = belongsTo.fk();
        String    fieldName  = field.getName();
        String    tableName  = AnimaCache.getTableName(type);
        String    pkName     = AnimaCache.getPKColumn(type);
        String    belongSQL  = "SELECT * FROM " + tableName + " WHERE " + pkName + " = ?";

        return RelationParams.builder().fk(foreignKey).pk(pkName).type(type)
                .relateSQL(belongSQL)
                .fieldName(fieldName)
                .tableName(tableName)
                .build();
    }

    public static RelationParams buildHasMany(Field field){
        Class<?> type = field.getType();
        if (type == List.class) {
            Type genericType = field.getGenericType();
            if (genericType == null) {
                throw new AnimaException("Not genericType");
            }
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                type = (Class<?>) pt.getActualTypeArguments()[0];
            }
        }
        HasMany hasMany    = field.getAnnotation(HasMany.class);
        String  foreignKey = hasMany.fk();
        String  fieldName  = field.getName();
        String  tableName  = AnimaCache.getTableName(type);
        String  hasManySQL = "SELECT * FROM " + tableName + " WHERE " + foreignKey + " = ?";

        return RelationParams.builder().fk(foreignKey).type(type)
                .relateSQL(hasManySQL)
                .fieldName(fieldName)
                .tableName(tableName)
                .build();
    }

    public static RelationParams buildHasOne(Field field){
        Class<?>           type       = field.getType();
        HasOne hasOne     = field.getAnnotation(HasOne.class);
        String foreignKey = hasOne.fk();
        String fieldName  = field.getName();
        String tableName  = AnimaCache.getTableName(type);
        String hasOneSQL  = "SELECT * FROM " + tableName + " WHERE " + foreignKey + " = ?";

        return RelationParams.builder().fk(foreignKey).type(type)
                .relateSQL(hasOneSQL)
                .fieldName(fieldName)
                .tableName(tableName)
                .build();

    }

}
