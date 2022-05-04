package com.hellokaton.anima.dialect;

import com.hellokaton.anima.core.SQLParams;
import com.hellokaton.anima.page.PageRow;

/**
 * Oracle dialect
 *
 * @author biezhi
 * @date 2018/3/17
 */
public class OracleDialect implements Dialect {

    @Override
    public String paginate(SQLParams sqlParams) {
        PageRow pageRow = sqlParams.getPageRow();
        int     limit   = pageRow.getPageSize();
        int     pageNum = pageRow.getPageNum();

        int           start = (pageNum - 1) * limit + 1;
        int           end   = pageNum * limit;
        StringBuilder sql   = new StringBuilder();
        sql.append("SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM (  ");
        sql.append(select(sqlParams));
        sql.append(" ) row_ where rownum <= ").append(end).append(") table_alias");
        sql.append(" WHERE table_alias.rownum_ >= ").append(start);
        return sql.toString();
    }

}
