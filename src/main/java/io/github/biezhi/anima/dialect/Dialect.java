package io.github.biezhi.anima.dialect;

import io.github.biezhi.anima.core.AnimaCache;
import io.github.biezhi.anima.core.SQLParams;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.utils.AnimaUtils;

import java.lang.reflect.Field;
import java.util.List;

import static io.github.biezhi.anima.core.AnimaCache.getGetterName;

/**
 * Database Dialect
 *
 * @author biezhi
 * @date 2018/3/17
 */
public interface Dialect {

    default String select(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        if (AnimaUtils.isNotEmpty(sqlParams.getCustomSQL())) {
            sql.append(sqlParams.getCustomSQL());
        } else {
            sql.append("SELECT");
            if (AnimaUtils.isNotEmpty(sqlParams.getSelectColumns())) {
                sql.append(' ').append(sqlParams.getSelectColumns()).append(' ');
            } else if (AnimaUtils.isNotEmpty(sqlParams.getExcludedColumns())) {
                sql.append(' ').append(AnimaUtils.buildColumns(sqlParams.getExcludedColumns(), sqlParams.getModelClass())).append(' ');
            } else {
                sql.append(" * ");
            }
            sql.append("FROM ").append(sqlParams.getTableName());
            if (sqlParams.getConditionSQL().length() > 0) {
                sql.append(" WHERE ").append(sqlParams.getConditionSQL().substring(5));
            }
        }

        if (AnimaUtils.isNotEmpty(sqlParams.getOrderBy())) {
            sql.append(" ORDER BY").append(sqlParams.getOrderBy());
        }
        if (sqlParams.isSQLLimit()) {
            sql.append(" LIMIT ?");
        }
        return sql.toString();
    }

    default String count(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(sqlParams.getTableName());
        if (sqlParams.getConditionSQL().length() > 0) {
            sql.append(" WHERE ").append(sqlParams.getConditionSQL().substring(5));
        }
        return sql.toString();
    }

    default String insert(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(sqlParams.getTableName());

        StringBuilder columnNames = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();

        List<Field> fields = AnimaCache.computeModelFields(sqlParams.getModelClass());

        for (int i = 0; i < fields.size(); i++) {
            if(null != sqlParams.getColumnValues().get(i)){
                Field field = fields.get(i);
                columnNames.append(",").append(" ").append(AnimaCache.getColumnName(field));
                placeholder.append(", ?");
            }
        }

        sql.append("(").append(columnNames.substring(2)).append(")").append(" VALUES (")
                .append(placeholder.substring(2)).append(")");
        return sql.toString();
    }

    default String update(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(sqlParams.getTableName()).append(" SET ");

        StringBuilder setSQL = new StringBuilder();

        if (null != sqlParams.getUpdateColumns() && !sqlParams.getUpdateColumns().isEmpty()) {
            sqlParams.getUpdateColumns().forEach((key, value) -> setSQL.append(key).append(" = ?, "));
        } else {
            if (null != sqlParams.getModel()) {
                for (Field field : AnimaCache.computeModelFields(sqlParams.getModelClass())) {
                    try {
                        Object value = AnimaUtils.invokeMethod(sqlParams.getModel(), getGetterName(field.getName()), AnimaUtils.EMPTY_ARG);
                        if (null == value) {
                            continue;
                        }
                        setSQL.append(AnimaCache.getColumnName(field)).append(" = ?, ");
                    } catch (IllegalArgumentException e) {
                        throw new AnimaException("illegal argument or Access:", e);
                    }
                }
            }
        }
        sql.append(setSQL.substring(0, setSQL.length() - 2));
        if (sqlParams.getConditionSQL().length() > 0) {
            sql.append(" WHERE ").append(sqlParams.getConditionSQL().substring(5));
        }
        return sql.toString();
    }

    default String delete(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(sqlParams.getTableName());

        if (sqlParams.getConditionSQL().length() > 0) {
            sql.append(" WHERE ").append(sqlParams.getConditionSQL().substring(5));
        } else {
            if (null != sqlParams.getModel()) {
                StringBuilder columnNames = new StringBuilder();
                for (Field field : AnimaCache.computeModelFields(sqlParams.getModelClass())) {
                    try {
                        Object value = AnimaUtils.invokeMethod(sqlParams.getModel(), getGetterName(field.getName()), AnimaUtils.EMPTY_ARG);
                        if (null == value) {
                            continue;
                        }
                        columnNames.append(AnimaCache.getColumnName(field)).append(" = ? and ");
                    } catch (IllegalArgumentException e) {
                        throw new AnimaException("illegal argument or Access:", e);
                    }
                }
                if (columnNames.length() > 0) {
                    sql.append(" WHERE ").append(columnNames.substring(0, columnNames.length() - 5));
                }
            }
        }
        return sql.toString();
    }

    String paginate(SQLParams sqlParams);

}
