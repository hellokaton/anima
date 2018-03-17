package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.page.PageRow;
import lombok.Data;

import java.util.Map;

/**
 * @author biezhi
 * @date 2018/3/17
 */
@Data
public class SQLParams {

    private Class<? extends Model> modelClass;
    private Object                 model;
    private String                 selectColumns;
    private String                 tableName;
    private String                 pkName;
    private StringBuilder          conditionSQL;
    private Map<String, Object>    updateColumns;
    private PageRow                pageRow;
    private String                 orderBy;
}
