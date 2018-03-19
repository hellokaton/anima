package io.github.biezhi.anima.core.relation;

import lombok.Builder;
import lombok.Data;

/**
 * @author biezhi
 * @date 2018/3/19
 */
@Data
@Builder
public class RelationParams {

    private Class<?> type;

    private String pk;

    private String fk;

    private String fieldName;

    private String relateSQL;

    private String tableName;

}
