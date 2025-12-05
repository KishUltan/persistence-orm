package com.kishultan.persistence.orm.dialect;

/**
 * H2数据库方言实现
 * 
 * @author Portal Team
 */
public class H2Dialect implements DatabaseDialect {
    
    @Override
    public String getDatabaseType() {
        return "h2";
    }
    
    @Override
    public String buildLimitClause(int maxRows, int offset) {
        StringBuilder clause = new StringBuilder();
        
        if (maxRows != Integer.MAX_VALUE) {
            clause.append(" LIMIT ").append(maxRows);
        }
        
        if (offset > 0) {
            clause.append(" OFFSET ").append(offset);
        }
        
        return clause.toString();
    }
    
    @Override
    public String buildPaginationSql(String baseSql, int maxRows, int offset) {
        StringBuilder sql = new StringBuilder(baseSql);
        
        if (maxRows != Integer.MAX_VALUE) {
            sql.append(" LIMIT ").append(maxRows);
        }
        
        if (offset > 0) {
            sql.append(" OFFSET ").append(offset);
        }
        
        return sql.toString();
    }
    
    @Override
    public String getParameterPlaceholder() {
        return "?";
    }
    
    @Override
    public String getTableNameQuote() {
        return "\"";
    }
    
    @Override
    public String getColumnNameQuote() {
        return "\"";
    }
} 