package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.DefaultRowMapper;
import com.kishultan.persistence.orm.query.QueryBuilder;
import com.kishultan.persistence.orm.query.SqlExecutor;
import com.kishultan.persistence.orm.query.context.QueryResult;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 流式查询分割器
 * 实现基于ResultSet的流式数据读取
 * 
 * @param <T> 实体类型
 */
public class StreamingQuerySpliterator<T> implements Spliterator<T> {
    
    private final QueryBuilder<T> queryBuilder;
    private final DefaultRowMapper defaultRowMapper;
    private final DataSource dataSource;
    private final int batchSize;
    
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private List<T> currentBatch;
    private int currentIndex;
    private boolean hasMoreData;
    private boolean closed = false;
    
    /**
     * 构造函数
     * @param queryBuilder 查询构建器
     * @param sqlExecutor SQL执行器
     * @param defaultRowMapper 结果集映射器
     * @param batchSize 批次大小
     */
    public StreamingQuerySpliterator(QueryBuilder<T> queryBuilder, 
                                   SqlExecutor sqlExecutor, 
                                   DefaultRowMapper defaultRowMapper,
                                   DataSource dataSource,
                                   int batchSize) {
        this.queryBuilder = queryBuilder;
        this.defaultRowMapper = defaultRowMapper;
        this.dataSource = dataSource;
        this.batchSize = batchSize;
        this.currentIndex = 0;
        this.hasMoreData = true;
        
        // 延迟初始化，避免在构造函数中执行数据库操作
        // initializeQuery();
    }
    
    /**
     * 初始化查询
     */
    private void initializeQuery() {
        try {
            // 获取数据库连接
            connection = dataSource.getConnection();
            
            // 构建查询SQL
            QueryResult queryResult = ((StandardQueryBuilder<T>) queryBuilder).buildQuery();
            String sql = queryResult.getSql();
            List<Object> parameters = queryResult.getParameters();
            
            // 创建预编译语句
            statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(batchSize);
            
            // 设置参数
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            
            // 执行查询
            resultSet = statement.executeQuery();
            
            // 加载第一批数据
            loadNextBatch();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize streaming query", e);
        }
    }
    
    /**
     * 加载下一批数据
     */
    private void loadNextBatch() {
        if (closed) {
            return;
        }
        
        try {
            currentBatch = new ArrayList<>(batchSize);
            int count = 0;
            
            while (count < batchSize && resultSet.next()) {
                try {
                    // 使用ResultSetMapper的map方法
                    T entity = (T) defaultRowMapper.mapRow(resultSet, (Class<T>) ((StandardQueryBuilder<T>) queryBuilder).getEntityClass());
                    currentBatch.add(entity);
                    count++;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to map result set row", e);
                }
            }
            
            hasMoreData = count == batchSize;
            currentIndex = 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load batch data", e);
        }
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed) {
            return false;
        }
        
        // 延迟初始化查询
        if (connection == null) {
            try {
                initializeQuery();
            } catch (Exception e) {
                close();
                return false;
            }
        }
        
        if (currentIndex >= currentBatch.size()) {
            if (hasMoreData) {
                loadNextBatch();
                if (currentBatch.isEmpty()) {
                    close();
                    return false;
                }
            } else {
                close();
                return false;
            }
        }
        
        action.accept(currentBatch.get(currentIndex++));
        return true;
    }
    
    @Override
    public Spliterator<T> trySplit() {
        // 流式查询不支持分割，返回null
        return null;
    }
    
    @Override
    public long estimateSize() {
        // 无法准确估计大小，返回未知
        return Long.MAX_VALUE;
    }
    
    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (closed) {
            return;
        }
        
        closed = true;
        
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // 记录日志，但不抛出异常
            System.err.println("Error closing streaming query resources: " + e.getMessage());
        }
    }
}
