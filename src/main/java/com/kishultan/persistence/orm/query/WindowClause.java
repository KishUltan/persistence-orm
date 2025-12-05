package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.Columnable;
import java.util.List;

/**
 * 窗口函数子句接口
 * 提供窗口函数功能，与SELECT、FROM等子句同级
 */
public interface WindowClause<T> extends SelectClause<T>, FunctionQuery {
    
    /**
     * 行号函数
     */
    WindowClause<T> rowNumber(String alias);
    
    /**
     * 行号函数（带分区和排序）
     */
    WindowClause<T> rowNumber(List<Columnable<T, ?>> partitionBy,
                              List<Columnable<T, ?>> orderBy,
                              String alias);
    
    /**
     * 排名函数
     */
    WindowClause<T> rank(String alias);
    
    /**
     * 排名函数（带分区和排序）
     */
    WindowClause<T> rank(List<Columnable<T, ?>> partitionBy,
                         List<Columnable<T, ?>> orderBy,
                         String alias);
    
    /**
     * 密集排名函数
     */
    WindowClause<T> denseRank(String alias);
    
    /**
     * 密集排名函数（带分区和排序）
     */
    WindowClause<T> denseRank(List<Columnable<T, ?>> partitionBy,
                              List<Columnable<T, ?>> orderBy,
                              String alias);
    
    /**
     * 滞后值函数
     */
    WindowClause<T> lag(Columnable<T, ?> field, int offset, String alias);
    
    /**
     * 领先值函数
     */
    WindowClause<T> lead(Columnable<T, ?> field, int offset, String alias);
    
    /**
     * 分组函数
     */
    WindowClause<T> ntile(int buckets, String alias);
    
    /**
     * 百分比排名函数
     */
    WindowClause<T> percentRank(String alias);
    
    /**
     * 累积分布函数
     */
    WindowClause<T> cumeDist(String alias);
    
    /**
     * 第一个值函数
     */
    WindowClause<T> firstValue(Columnable<T, ?> field, String alias);
    
    /**
     * 最后一个值函数
     */
    WindowClause<T> lastValue(Columnable<T, ?> field, String alias);
    
    /**
     * 第N个值函数
     */
    WindowClause<T> nthValue(Columnable<T, ?> field, int n, String alias);
    
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

