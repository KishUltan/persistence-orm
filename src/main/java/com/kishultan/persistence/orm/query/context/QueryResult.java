package com.kishultan.persistence.orm.query.context;

import java.util.List;

/**
 * 查询结果，存储完整的SQL和COUNT查询SQL
 */
public class QueryResult {
    private final String sql;
    private final String countSql;
    private final List<Object> parameters;
    
    public QueryResult(String sql, String countSql, List<Object> parameters) {
        this.sql = sql;
        this.countSql = countSql;
        this.parameters = parameters;
    }
    
    public String getSql() { return sql; }
    public String getCountSql() { return countSql; }
    public List<Object> getParameters() { return parameters; }
}

