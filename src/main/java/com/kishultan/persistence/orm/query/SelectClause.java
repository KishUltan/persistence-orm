package com.kishultan.persistence.orm.query;

/**
 * SELECT子句接口
 * 提供SELECT子句功能，支持子查询和lambda表达式，遵循SQL书写习惯
 */
public interface SelectClause<T> extends CommonClause<T> {
    
    /**
     * 指定FROM子句
     */
    FromClause<T> from();
    
    /**
     * 指定FROM子句（实体类）
     */
    FromClause<T> from(Class<T> entityClass);
    
    /**
     * 指定FROM子句（表名）
     */
    FromClause<T> from(String tableName);
    
    /**
     * 指定FROM子句（表名+别名）
     */
    FromClause<T> from(String tableName, String alias);
    
    /**
     * 基于子查询的FROM子句
     */
    FromClause<T> fromSubquery(String subquerySql);
    
    /**
     * 基于子查询的FROM子句（QueryBuilder）
     */
    FromClause<T> fromSubquery(QueryBuilder<T> subquery);
}




