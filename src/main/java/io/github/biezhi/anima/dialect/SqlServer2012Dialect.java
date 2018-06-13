package io.github.biezhi.anima.dialect;

import io.github.biezhi.anima.core.SQLParams;
import io.github.biezhi.anima.page.PageRow;

/**
 * SqlServer 2012+ dialect
 *
 * @author darren
 * @date 2018-06-13
 */
public class SqlServer2012Dialect implements Dialect {

    @Override
    public String paginate(SQLParams sqlParams) {
        PageRow pageRow  = sqlParams.getPageRow();
        int     limit    = pageRow.getPageSize();
        int     offset   = limit * (pageRow.getPageNum() - 1);
        String  limitSQL = " OFFSET " + offset + " ROWS FETCH NEXT " + limit+ " ROWS ONLY ";

        StringBuilder sql = new StringBuilder();
        sql.append(select(sqlParams));
        //offset-fetch 必须在order-by语句之后，因此如果用户的sql没有order-by则为其添加一个不影响顺序的按时间戳的order-by
        if(!sql.toString().matches("(?i).* ORDER BY[^)]+$")) {
            sql.append(" ORDER BY CURRENT_TIMESTAMP");
        }
        sql.append(limitSQL);
        return sql.toString();
    }

}
