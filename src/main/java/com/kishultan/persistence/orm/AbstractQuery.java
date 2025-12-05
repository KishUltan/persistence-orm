package com.kishultan.persistence.orm;

import com.kishultan.persistence.orm.query.QueryBuilder;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.ColumnabledLambda;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 查询抽象基类
 * 实现CommonQuery接口的所有公共方法，委托给QueryBuilder执行
 * 
 * @author Portal Team
 */
public abstract class AbstractQuery<T> implements CommonQuery<T> {
    
    protected final QueryBuilder<T> queryBuilder;
    protected final Class<T> entityClass;
    
    protected AbstractQuery(Class<T> entityClass, QueryBuilder<T> queryBuilder) {
        this.entityClass = entityClass;
        this.queryBuilder = queryBuilder;
    }
    
    /**
     * 获取查询构建器
     */
    protected QueryBuilder<T> getQueryBuilder() {
        return queryBuilder;
    }
    
    /**
     * 获取实体类
     */
    protected Class<T> getEntityClass() {
        return entityClass;
    }
    
    // ==================== 执行方法实现 ====================
    
    @Override
    public List<T> findList() {
        if (queryBuilder == null) {
            throw new UnsupportedOperationException("QueryBuilder is not available");
        }
        return queryBuilder.findList();
    }
    
    @Override
    public T findOne() {
        if (queryBuilder == null) {
            throw new UnsupportedOperationException("QueryBuilder is not available");
        }
        return queryBuilder.findFirst();
    }
    
    @Override
    public Optional<T> findOneOptional() {
        T result = findOne();
        return Optional.ofNullable(result);
    }
    
    @Override
    public long count() {
        if (queryBuilder == null) {
            throw new UnsupportedOperationException("QueryBuilder is not available");
        }
        return queryBuilder.count();
    }
    
    @Override
    public Stream<T> stream() {
        if (queryBuilder == null) {
            throw new UnsupportedOperationException("QueryBuilder is not available");
        }
        List<T> results = queryBuilder.findList();
        return results.stream();
    }
    
    // ==================== 排序方法实现 ====================
    
    @Override
    public CommonQuery<T> orderBy(String property) {
        if (queryBuilder != null) {
            queryBuilder.select().orderBy().asc(property);
        }
        return this;
    }
    
    @Override
    public CommonQuery<T> orderByAsc(String property) {
        if (queryBuilder != null) {
            queryBuilder.select().orderBy().asc(property);
        }
        return this;
    }
    
    @Override
    public CommonQuery<T> orderByDesc(String property) {
        if (queryBuilder != null) {
            queryBuilder.select().orderBy().desc(property);
        }
        return this;
    }
    
    @Override
    public <R> CommonQuery<T> orderBy(Columnable<T, R> property) {
        if (queryBuilder != null) {
            String fieldName = ColumnabledLambda.getColumnName(property);
            queryBuilder.select().orderBy().asc(fieldName);
        }
        return this;
    }
    
    @Override
    public <R> CommonQuery<T> orderByAsc(Columnable<T, R> property) {
        if (queryBuilder != null) {
            String fieldName = ColumnabledLambda.getColumnName(property);
            queryBuilder.select().orderBy().asc(fieldName);
        }
        return this;
    }
    
    @Override
    public <R> CommonQuery<T> orderByDesc(Columnable<T, R> property) {
        if (queryBuilder != null) {
            String fieldName = ColumnabledLambda.getColumnName(property);
            queryBuilder.select().orderBy().desc(fieldName);
        }
        return this;
    }
    
    // ==================== 分页方法实现 ====================
    
    @Override
    public CommonQuery<T> limit(int offset, int size) {
        if (queryBuilder != null) {
            queryBuilder.select().limit(offset, size);
        }
        return this;
    }
    
    @Override
    public CommonQuery<T> limit(int size) {
        if (queryBuilder != null) {
            queryBuilder.select().limit(0, size);
        }
        return this;
    }
    
    // ==================== 分组方法实现 ====================
    
    @Override
    public CommonQuery<T> groupBy(String... columns) {
        if (queryBuilder != null && columns != null) {
            queryBuilder.select().from(entityClass).groupBy(columns);
        }
        return this;
    }
    
    @Override
    public <R> CommonQuery<T> groupBy(Columnable<T, R>... columns) {
        if (queryBuilder != null && columns != null) {
            queryBuilder.select().from(entityClass).groupBy(columns);
        }
        return this;
    }
}
