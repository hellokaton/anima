package io.github.biezhi.anima.dialect;

import io.github.biezhi.anima.core.SQLParams;
import io.github.biezhi.anima.page.PageRow;

/**
 * SqlServer dialect
 *
 * @author biezhi,darren
 * @date 2018/3/18
 */
public class SqlServerDialect implements Dialect {

    @Override
    public String paginate(SQLParams sqlParams) {
        PageRow pageRow = sqlParams.getPageRow();
        int     limit   = pageRow.getPageSize();
        int     pageNum = pageRow.getPageNum();

        int end = pageNum * limit;
        if (end <= 0)
            end = limit;
        int begin = (pageNum - 1) * limit + 1;
        if (begin < 1)
            begin = 1;

        StringBuilder sql = new StringBuilder();

        sql.append("with query as ( select inner_query.*, row_number() over (order by current_timestamp) as temprownumber from ( ");
        sql.append(select(sqlParams).replaceFirst("(?i)select(\\s+distinct\\s+)?", "$0 top("+end+")"));
        sql.append(" ) inner_query ) select * from query where temprownumber between ");
        sql.append(begin).append(" and ").append(end);
        return sql.toString();
    }

}
