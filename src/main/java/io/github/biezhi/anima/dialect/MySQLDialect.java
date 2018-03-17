package io.github.biezhi.anima.dialect;

import io.github.biezhi.anima.core.SQLParams;

/**
 * @author biezhi
 * @date 2018/3/17
 */
public class MySQLDialect implements Dialect {

    @Override
    public String paginate(SQLParams sqlParams) {
        StringBuilder sql = new StringBuilder();
        String        s1  = select(sqlParams);
        sql.append(s1).append(" LIMIT ?, ?");
        return sql.toString();
    }

}
