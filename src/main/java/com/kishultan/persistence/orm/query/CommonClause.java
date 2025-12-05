package com.kishultan.persistence.orm.query;

import java.util.List;

/**
 * 所有 Clause 的公共方法接口
 * 定义了 orderBy、limit、end 等公共方法
 * 以及执行方法，让子句可以直接执行查询
 */
public interface CommonClause<T> {
    
    /**
     * ORDER BY 子句
     */
    OrderClause<T> orderBy();
    
    /**
     * 设置 LIMIT
     */
    QueryBuilder<T> limit(int offset, int size);
    
    /**
     * 结束当前查询构建
     */
    QueryBuilder<T> end();
    
    // ==================== 执行方法 ====================
    // 执行方法定义到CommonClause中，可以让多个子句不必返回到builder就能调用
    
    /**
     * 执行查询并返回结果列表
     */
    List<T> findList();
    
    /**
     * 执行查询并返回单个结果
     */
    T findOne();
    
    /**
     * 执行查询并返回结果数量
     */
    long count();
}

