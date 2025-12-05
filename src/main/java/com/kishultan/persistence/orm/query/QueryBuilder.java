package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.query.monitor.QueryPerformanceMonitor;
import com.kishultan.persistence.orm.query.monitor.QueryMetrics;
import com.kishultan.persistence.orm.query.cache.QueryCache;
import java.util.List;
import java.util.function.Consumer;

/**
 * 查询构建器基础接口
 * 提供查询构建和执行的核心功能
 * 支持Lambda表达式和类型安全查询
 */
public interface QueryBuilder<T> {
    
    // 子查询构建
    QueryBuilder<T> subquery();
    
    // 主查询构建
    SelectClause<T> select();
    SelectClause<T> select(String... columns);
    SelectClause<T> select(Columnable<T, ?>... fields);
    SelectClause<T> selectAll();
    
    // 聚合函数构建
    AggregateClause<T> aggregate();
    
    // 窗口函数构建
    WindowClause<T> window();
    
    // 表达式函数构建
    ExpressionClause<T> expression();
    
    // CASE WHEN表达式构建
    CaseWhenClause<T> caseWhen();
    CaseWhenClause<T> caseWhen(Columnable<T, ?> field);
    CaseWhenClause<T> caseWhen(Columnable<T, ?> field, String alias);
    CaseWhenClause<T> caseWhen(String alias);
    
    // 执行方法
    List<T> findList();
    T findFirst();
    long count();
    String getGeneratedSql();
    
    // 子查询相关
    boolean isSubquery();
    String getSubquerySql();
    
    // 分页支持
    QueryBuilder<T> limit(int offset, int size);
    
    // 当前查询字段引用
    String selfField(Columnable<T, ?> fieldSelector);
    
    // 子查询字段引用
    String subqueryField(Columnable<T, ?> fieldSelector);
    
    // 条件构建器模式 - 支持 Consumer 的 where 方法
    QueryBuilder<T> where(Consumer<WhereClause<T>> whereBuilder);
    
    // 性能监控支持
    QueryMetrics getPerformanceMetrics();
    QueryPerformanceMonitor getPerformanceMonitor();
    
    // 缓存支持
    QueryCache getQueryCache();
    
    // 自定义映射器支持
    QueryBuilder setRowMapper(RowMapper rowMapper);
    RowMapper<?> getRowMapper();
    
    // 子句创建方法（用于解耦query包和query.impl包）
    /**
     * 创建OrderClause实例
     * 
     * @return OrderClause实例
     */
    OrderClause<T> createOrderClause();
}

