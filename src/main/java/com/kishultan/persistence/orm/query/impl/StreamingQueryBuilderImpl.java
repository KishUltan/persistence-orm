package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.query.config.StreamingQueryConfig;
import com.kishultan.persistence.orm.query.DefaultRowMapper;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 流式查询构建器实现类
 * 基于QueryBuilder提供流式查询功能
 * 
 * @param <T> 实体类型
 */
public class StreamingQueryBuilderImpl<T> implements StreamingQueryBuilder<T> {
    
    private final QueryBuilder<T> queryBuilder;
    private final SqlExecutor sqlExecutor;
    private final DataSource dataSource;
    private final DefaultRowMapper defaultRowMapper;
    
    /**
     * 构造函数
     * @param queryBuilder 查询构建器
     * @param sqlExecutor SQL执行器
     * @param dataSource 数据源
     * @param defaultRowMapper 结果集映射器
     */
    public StreamingQueryBuilderImpl(QueryBuilder<T> queryBuilder, 
                                   SqlExecutor sqlExecutor, 
                                   DataSource dataSource, 
                                   DefaultRowMapper defaultRowMapper) {
        this.queryBuilder = queryBuilder;
        this.sqlExecutor = sqlExecutor;
        this.dataSource = dataSource;
        this.defaultRowMapper = defaultRowMapper;
    }
    
    // ==================== 基础流式查询 ====================
    
    @Override
    public Stream<T> stream() {
        return stream(StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public Stream<T> stream(int batchSize) {
        return StreamSupport.stream(
            new StreamingQuerySpliterator<>(queryBuilder, sqlExecutor, defaultRowMapper, dataSource, batchSize),
            false
        );
    }
    
    // ==================== 分页流式查询 ====================
    
    @Override
    public Stream<T> streamWithPagination(int pageSize) {
        return streamWithPagination(pageSize, 0);
    }
    
    @Override
    public Stream<T> streamWithPagination(int pageSize, int offset) {
        return StreamSupport.stream(
            new PaginatedStreamingQuerySpliterator<>(queryBuilder, sqlExecutor, defaultRowMapper, pageSize, offset),
            false
        );
    }
    
    // ==================== 流式处理 ====================
    
    @Override
    public void streamForEach(Consumer<T> processor) {
        streamForEach(processor, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public void streamForEach(Consumer<T> processor, int batchSize) {
        try (Stream<T> stream = stream(batchSize)) {
            stream.forEach(processor);
        }
    }
    
    @Override
    public void streamForEachWithPagination(Consumer<T> processor, int pageSize) {
        try (Stream<T> stream = streamWithPagination(pageSize)) {
            stream.forEach(processor);
        }
    }
    
    // ==================== 流式转换 ====================
    
    @Override
    public <R> Stream<R> streamMap(Function<T, R> mapper) {
        return streamMap(mapper, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public <R> Stream<R> streamMap(Function<T, R> mapper, int batchSize) {
        return stream(batchSize).map(mapper);
    }
    
    // ==================== 流式过滤 ====================
    
    @Override
    public Stream<T> streamFilter(Predicate<T> filter) {
        return streamFilter(filter, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public Stream<T> streamFilter(Predicate<T> filter, int batchSize) {
        return stream(batchSize).filter(filter);
    }
    
    // ==================== 流式统计 ====================
    
    @Override
    public CompletableFuture<Long> streamCount() {
        return streamCount(StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public CompletableFuture<Long> streamCount(int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                return stream.count();
            }
        });
    }
    
    // ==================== 流式聚合 ====================
    
    @Override
    public <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator) {
        return streamReduce(identity, accumulator, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                R result = identity;
                for (T item : stream.collect(java.util.stream.Collectors.toList())) {
                    result = accumulator.apply(result, item);
                }
                return result;
            }
        });
    }
    
    // ==================== 流式收集 ====================
    
    @Override
    public <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector) {
        return streamCollect(collector, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                return stream.collect(collector);
            }
        });
    }
    
    // ==================== 监控和指标 ====================
    
    @Override
    public Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics) {
        return streamWithMonitoring(metrics, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics, int batchSize) {
        return stream(batchSize)
            .peek(item -> {
                ((StreamingQueryMetricsImpl) metrics).incrementProcessedCount();
                ((StreamingQueryMetricsImpl) metrics).updateProcessingTime(System.currentTimeMillis());
            })
            .onClose(() -> ((StreamingQueryMetricsImpl) metrics).setCompleted(true));
    }
    
    // ==================== 错误处理 ====================
    
    @Override
    public Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler) {
        return streamWithErrorHandling(errorHandler, StreamingQueryConfig.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler, int batchSize) {
        return stream(batchSize)
            .filter(item -> {
                try {
                    return true;
                } catch (Exception e) {
                    StreamingErrorHandler.ErrorStrategy strategy = errorHandler.handleError(item, e, 0);
                    return strategy == StreamingErrorHandler.ErrorStrategy.CONTINUE || 
                           strategy == StreamingErrorHandler.ErrorStrategy.SKIP;
                }
            });
    }
    
    // ==================== 并行处理 ====================
    
    @Override
    public Stream<T> streamParallel() {
        return streamParallel(StreamingQueryConfig.DEFAULT_BATCH_SIZE, StreamingQueryConfig.DEFAULT_PARALLELISM);
    }
    
    @Override
    public Stream<T> streamParallel(int batchSize) {
        return streamParallel(batchSize, StreamingQueryConfig.DEFAULT_PARALLELISM);
    }
    
    @Override
    public Stream<T> streamParallel(int batchSize, int parallelism) {
        return stream(batchSize)
            .parallel()
            .onClose(() -> {
                // 清理并行处理资源
            });
    }
}
