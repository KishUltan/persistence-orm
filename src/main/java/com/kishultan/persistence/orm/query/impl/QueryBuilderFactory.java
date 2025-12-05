package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.QueryBuilder;
import javax.sql.DataSource;

/**
 * 查询构建器工厂
 * 
 * 位于impl包，可以创建StandardQueryBuilder实例
 */
public class QueryBuilderFactory {
    
    private final DataSource dataSource;
    
    public QueryBuilderFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public <T> QueryBuilder<T> createQueryBuilder(Class<T> entityClass) {
        return new StandardQueryBuilder<>(entityClass, dataSource);
    }
    
    public <T> QueryBuilder<T> createQueryBuilder(Class<T> entityClass, DataSource dataSource) {
        return new StandardQueryBuilder<>(entityClass, dataSource);
    }
}

