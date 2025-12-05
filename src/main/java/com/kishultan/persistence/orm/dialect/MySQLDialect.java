package com.kishultan.persistence.orm.dialect;

/**
 * MySQL数据库方言实现
 * 
 * @author Portal Team
 */
public class MySQLDialect implements DatabaseDialect {
    
    @Override
    public String getDatabaseType() {
        return "mysql";
    }
    
    @Override
    public String buildLimitClause(int maxRows, int offset) {
        StringBuilder clause = new StringBuilder();
        
        if (maxRows != Integer.MAX_VALUE) {
            clause.append(" LIMIT ").append(maxRows);
        }
        
        if (offset > 0) {
            if (maxRows != Integer.MAX_VALUE) {
                clause.append(" OFFSET ").append(offset);
            } else {
                clause.append(" LIMIT ").append(offset).append(", ").append(Integer.MAX_VALUE);
            }
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
            if (maxRows != Integer.MAX_VALUE) {
                sql.append(" OFFSET ").append(offset);
            } else {
                sql.append(" LIMIT ").append(offset).append(", ").append(Integer.MAX_VALUE);
            }
        }
        
        return sql.toString();
    }
    
    @Override
    public String getParameterPlaceholder() {
        return "?";
    }
    
    @Override
    public String getTableNameQuote() {
        return "`";
    }
    
    @Override
    public String getColumnNameQuote() {
        return "`";
    }
} 