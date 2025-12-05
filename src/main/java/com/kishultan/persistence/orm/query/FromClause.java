package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.Columnable;
import java.util.function.Consumer;

/**
 * FROM子句接口
 * 提供FROM子句功能，支持JOIN操作，遵循SQL书写习惯
 */
public interface FromClause<T> extends CommonClause<T> {

    /**
     * 内连接
     */
    JoinClause<T> innerJoin(Class<?> entityClass);

    /**
     * 内连接
     */
    JoinClause<T> innerJoin(Class<?> entityClass, String alias);
    
    /**
     * 内连接（自动别名）
     */
    JoinClause<T> join(Class<?> entityClass);
    
    /**
     * 内连接（字符串表名）
     */
    JoinClause<T> innerJoin(String tableName, String alias);
    
    /**
     * 左连接
     */
    JoinClause<T> leftJoin(Class<?> entityClass, String alias);
    
    /**
     * 左连接（自动别名）
     */
    JoinClause<T> leftJoin(Class<?> entityClass);
    
    /**
     * 左连接（字符串表名）
     */
    JoinClause<T> leftJoin(String tableName, String alias);
    
    /**
     * 右连接
     */
    JoinClause<T> rightJoin(Class<?> entityClass, String alias);
    
    /**
     * 全连接
     */
    JoinClause<T> fullJoin(Class<?> entityClass, String alias);
    
    /**
     * 交叉连接
     */
    JoinClause<T> crossJoin(Class<?> entityClass, String alias);
    
    /**
     * WHERE子句
     */
    WhereClause<T> where();
    
    /**
     * WHERE子句 - 支持Consumer模式
     * 提供更简洁的条件构建方式
     * 
     * @param whereClause WHERE条件构建器
     * @return WhereClause实例，支持链式调用
     */    WhereClause<T> where(Consumer<WhereClause<T>> whereClause);


    /**
     * GROUP BY子句（直接指定分组字段）
     */
    GroupClause<T> groupBy(String... columns);
    
    /**
     * GROUP BY子句（Lambda表达式）
     */
    <R> GroupClause<T> groupBy(Columnable<T, R>... columns);
    
    /**
     * ORDER BY子句
     */
    OrderClause<T> orderBy();
    
    // end方法已移除，FromClause现在直接继承CommonClause，可以直接调用执行方法
}




