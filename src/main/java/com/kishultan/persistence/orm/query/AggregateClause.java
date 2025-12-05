package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.Columnable;

/**
 * 聚合函数子句接口
 * 提供聚合函数功能，与SELECT、FROM等子句同级
 */
public interface AggregateClause<T> extends SelectClause<T>, FunctionQuery {
    
    /**
     * 计数函数
     */
    AggregateClause<T> count(Columnable<T, ?> field);
    
    /**
     * 计数函数（带别名）
     */
    AggregateClause<T> count(Columnable<T, ?> field, String alias);
    
    /**
     * 求和函数
     */
    AggregateClause<T> sum(Columnable<T, ?> field);
    
    /**
     * 求和函数（带别名）
     */
    AggregateClause<T> sum(Columnable<T, ?> field, String alias);
    
    /**
     * 平均值函数
     */
    AggregateClause<T> avg(Columnable<T, ?> field);
    
    /**
     * 平均值函数（带别名）
     */
    AggregateClause<T> avg(Columnable<T, ?> field, String alias);
    
    /**
     * 最大值函数
     */
    AggregateClause<T> max(Columnable<T, ?> field);
    
    /**
     * 最大值函数（带别名）
     */
    AggregateClause<T> max(Columnable<T, ?> field, String alias);
    
    /**
     * 最小值函数
     */
    AggregateClause<T> min(Columnable<T, ?> field);
    
    /**
     * 最小值函数（带别名）
     */
    AggregateClause<T> min(Columnable<T, ?> field, String alias);
    
//    /**
//     * 指定FROM子句
//     */
//    FromClause<T> from();
//
//    /**
//     * 指定FROM子句（实体类）
//     */
//    FromClause<T> from(Class<T> entityClass);
//
//    /**
//     * 指定FROM子句（表名）
//     */
//    FromClause<T> from(String tableName);
//
//    /**
//     * 指定FROM子句（表名+别名）
//     */
//    FromClause<T> from(String tableName, String alias);
}

