package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.page.PageRow;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQLParams
 *
 * @author biezhi
 * @date 2018/3/17
 */
@Data
@Builder
public class SQLParams {

    private Class<? extends Model> modelClass;
    private Object                 model;
    private String                 selectColumns;
    private String                 tableName;
    private String                 pkName;
    private StringBuilder          conditionSQL;
    private Map<String, Object>    updateColumns;
    private List<String>           excludedColumns;
    private PageRow                pageRow;
    private String                 orderBy;

}
