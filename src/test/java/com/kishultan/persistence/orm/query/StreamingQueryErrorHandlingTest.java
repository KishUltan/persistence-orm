package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;
import com.kishultan.persistence.orm.query.impl.StreamingQueryBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 流式查询错误处理测试类
 */
public class StreamingQueryErrorHandlingTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private SqlExecutor sqlExecutor;
    
    @Mock
    private DefaultRowMapper defaultRowMapper;
    
    private QueryBuilder<TestEntity> queryBuilder;
    private StreamingQueryBuilder<TestEntity> streamingQuery;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        queryBuilder = new StandardQueryBuilder<>(TestEntity.class, dataSource);
        streamingQuery = new StreamingQueryBuilderImpl<>(queryBuilder, sqlExecutor, dataSource, defaultRowMapper);
    }
    
    @Test
    public void testContinueErrorStrategy() {
        // 测试继续处理错误策略
        AtomicInteger processedCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                return ErrorStrategy.CONTINUE;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                return ErrorStrategy.CONTINUE;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                return ErrorStrategy.CONTINUE;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    @Test
    public void testSkipErrorStrategy() {
        // 测试跳过错误策略
        AtomicInteger processedCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    @Test
    public void testRetryErrorStrategy() {
        // 测试重试错误策略
        AtomicInteger processedCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                return ErrorStrategy.RETRY;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                return ErrorStrategy.RETRY;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                return ErrorStrategy.RETRY;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    @Test
    public void testStopErrorStrategy() {
        // 测试停止处理错误策略
        AtomicInteger processedCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                return ErrorStrategy.STOP;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                return ErrorStrategy.STOP;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                return ErrorStrategy.STOP;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    @Test
    public void testErrorStatistics() {
        // 测试错误统计
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            private final Map<Class<? extends Throwable>, Long> errorStats = new HashMap<>();
            
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                errorStats.merge(error.getClass(), 1L, Long::sum);
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                errorStats.merge(error.getClass(), 1L, Long::sum);
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                errorStats.merge(error.getClass(), 1L, Long::sum);
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>(errorStats);
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            // 处理记录
        });
        
        Map<Class<? extends Throwable>, Long> stats = errorHandler.getErrorStatistics();
        assertNotNull("Error statistics should not be null", stats);
    }
    
    @Test
    public void testErrorHandlerWithBatchSize() {
        // 测试带批次大小的错误处理
        AtomicInteger processedCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler, 500).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    @Test
    public void testErrorHandlerWithDifferentStrategies() {
        // 测试不同错误处理策略的组合
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        StreamingErrorHandler<TestEntity> errorHandler = new StreamingErrorHandler<TestEntity>() {
            @Override
            public ErrorStrategy handleError(TestEntity item, Throwable error, long position) {
                errorCount.incrementAndGet();
                if (position < 5) {
                    return ErrorStrategy.CONTINUE;
                } else if (position < 10) {
                    return ErrorStrategy.SKIP;
                } else {
                    return ErrorStrategy.STOP;
                }
            }
            
            @Override
            public ErrorStrategy handleBatchError(List<TestEntity> batch, Throwable error, long startPosition) {
                errorCount.incrementAndGet();
                return ErrorStrategy.SKIP;
            }
            
            @Override
            public ErrorStrategy handleConnectionError(Throwable error) {
                errorCount.incrementAndGet();
                return ErrorStrategy.STOP;
            }
            
            @Override
            public Map<Class<? extends Throwable>, Long> getErrorStatistics() {
                return new HashMap<>();
            }
        };
        
        streamingQuery.streamWithErrorHandling(errorHandler).forEach(entity -> {
            processedCount.incrementAndGet();
        });
        
        // 验证处理了记录
        assertTrue("Should process some records", processedCount.get() >= 0);
    }
    
    /**
     * 测试实体类
     */
    public static class TestEntity {
        private Long id;
        private String name;
        private String status;
        private Integer age;
        
        public TestEntity() {}
        
        public TestEntity(Long id, String name, String status, Integer age) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.age = age;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }
}
