package io.github.biezhi.anima.dialect;

import io.github.biezhi.anima.core.SQLParams;
import io.github.biezhi.anima.page.PageRow;

/**
 * SqlServer dialect
 *
 * @author biezhi
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
        int begin = (pageNum - 1) * limit;
        if (begin < 0)
            begin = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( SELECT row_number() over (ORDER BY tempcolumn) temprownumber, * FROM ( SELECT top ")
                .append(end)
                .append(" tempcolumn=0,")
                .append(select(sqlParams))
                .append(")vip)mvp where temprownumber>")
                .append(begin);
        return sql.toString();
    }

}
