package io.github.biezhi.anima.core.relation;

import lombok.Builder;
import lombok.Getter;

/**
 * @author biezhi
 * @date 2018/3/19
 */
@Getter
@Builder
public class RelationParams {

    /**
     * Field Type
     */
    private Class<?> type;

    /**
     * Field Type Primary Key Column
     */
    private String pk;

    /**
     * Current Table Foreign Key
     */
    private String fk;

    /**
     * Field Name
     */
    private String fieldName;

    private String relateSQL;

    /**
     * Field Type Table Name
     */
    private String tableName;

}
