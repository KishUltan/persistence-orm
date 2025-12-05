package com.kishultan.persistence.orm.dialect;

/**
 * 数据库方言接口
 * 用于处理不同数据库的SQL语法差异
 * 
 * @author Portal Team
 */
public interface DatabaseDialect {
    
    /**
     * 获取数据库类型
     */
    String getDatabaseType();
    
    /**
     * 生成LIMIT子句
     * @param maxRows 最大行数
     * @param offset 偏移量
     * @return LIMIT子句
     */
    String buildLimitClause(int maxRows, int offset);
    
    /**
     * 生成分页查询的完整SQL
     * @param baseSql 基础SQL
     * @param maxRows 最大行数
     * @param offset 偏移量
     * @return 完整的分页SQL
     */
    String buildPaginationSql(String baseSql, int maxRows, int offset);
    
    /**
     * 获取参数占位符
     * @return 参数占位符字符串
     */
    String getParameterPlaceholder();
    
    /**
     * 获取表名引号
     * @return 表名引号字符串
     */
    String getTableNameQuote();
    
    /**
     * 获取列名引号
     * @return 列名引号字符串
     */
    String getColumnNameQuote();
} 