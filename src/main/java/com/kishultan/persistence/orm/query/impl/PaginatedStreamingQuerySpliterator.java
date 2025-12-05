package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.DefaultRowMapper;
import com.kishultan.persistence.orm.query.QueryBuilder;
import com.kishultan.persistence.orm.query.SqlExecutor;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 分页流式查询分割器
 * 实现基于分页的流式数据读取
 * 
 * @param <T> 实体类型
 */
public class PaginatedStreamingQuerySpliterator<T> implements Spliterator<T> {
    
    private final QueryBuilder<T> queryBuilder;
    private final SqlExecutor sqlExecutor;
    private final DefaultRowMapper defaultRowMapper;
    private final int pageSize;
    
    private int currentOffset;
    private List<T> currentPage;
    private int currentIndex;
    private boolean hasMorePages;
    private boolean closed = false;
    
    /**
     * 构造函数
     * @param queryBuilder 查询构建器
     * @param sqlExecutor SQL执行器
     * @param defaultRowMapper 结果集映射器
     * @param pageSize 页大小
     * @param offset 初始偏移量
     */
    public PaginatedStreamingQuerySpliterator(QueryBuilder<T> queryBuilder, 
                                            SqlExecutor sqlExecutor, 
                                            DefaultRowMapper defaultRowMapper,
                                            int pageSize, 
                                            int offset) {
        this.queryBuilder = queryBuilder;
        this.sqlExecutor = sqlExecutor;
        this.defaultRowMapper = defaultRowMapper;
        this.pageSize = pageSize;
        this.currentOffset = offset;
        this.currentIndex = 0;
        this.hasMorePages = true;
        
        loadNextPage();
    }
    
    /**
     * 加载下一页数据
     */
    private void loadNextPage() {
        if (closed) {
            return;
        }
        
        try {
            // 创建分页查询
            QueryBuilder<T> paginatedQuery = queryBuilder
                .limit(currentOffset, pageSize);
            
            // 执行查询
            List<T> results = sqlExecutor.executeQuery(
                ((StandardQueryBuilder<T>) paginatedQuery).buildQuery().getSql(),
                ((StandardQueryBuilder<T>) paginatedQuery).buildQuery().getParameters(),
                (Class<T>) ((StandardQueryBuilder<T>) paginatedQuery).getEntityClass(),
                    defaultRowMapper
            );
            
            currentPage = results;
            hasMorePages = results.size() == pageSize;
            currentOffset += pageSize;
            currentIndex = 0;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load page data", e);
        }
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed) {
            return false;
        }
        
        if (currentIndex >= currentPage.size()) {
            if (hasMorePages) {
                loadNextPage();
                if (currentPage.isEmpty()) {
                    close();
                    return false;
                }
            } else {
                close();
                return false;
            }
        }
        
        action.accept(currentPage.get(currentIndex++));
        return true;
    }
    
    @Override
    public Spliterator<T> trySplit() {
        // 分页流式查询不支持分割，返回null
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
        // 分页查询不需要关闭数据库连接，因为每次都是新的查询
    }
}
