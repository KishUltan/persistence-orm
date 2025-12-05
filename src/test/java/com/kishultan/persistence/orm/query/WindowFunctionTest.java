package com.kishultan.persistence.orm.query;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;
import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;

/**
 * 窗口函数测试类
 */
public class WindowFunctionTest {
    
    private DataSource dataSource;
    private QueryBuilder<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的QueryBuilder
        queryBuilder = new StandardQueryBuilder<>(TestEntity.class, null);
    }
    
    @Test
    public void testBasicWindowFunctions() {
        // 测试基本窗口函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 测试ROW_NUMBER
        WindowClause<TestEntity> rowNumberResult = windowClause.rowNumber("row_num");
        assertNotNull(rowNumberResult);
        
        // 测试RANK
        WindowClause<TestEntity> rankResult = windowClause.rank("rank_num");
        assertNotNull(rankResult);
        
        // 测试DENSE_RANK
        WindowClause<TestEntity> denseRankResult = windowClause.denseRank("dense_rank_num");
        assertNotNull(denseRankResult);
        
        // 测试PERCENT_RANK
        WindowClause<TestEntity> percentRankResult = windowClause.percentRank("percent_rank");
        assertNotNull(percentRankResult);
        
        // 测试CUME_DIST
        WindowClause<TestEntity> cumeDistResult = windowClause.cumeDist("cume_dist");
        assertNotNull(cumeDistResult);
    }
    
    @Test
    public void testOffsetWindowFunctions() {
        // 测试偏移窗口函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 测试LAG
        WindowClause<TestEntity> lagResult = windowClause.lag(TestEntity::getAmount, 1, "prev_amount");
        assertNotNull(lagResult);
        
        // 测试LEAD
        WindowClause<TestEntity> leadResult = windowClause.lead(TestEntity::getAmount, 1, "next_amount");
        assertNotNull(leadResult);
    }
    
    @Test
    public void testNtileFunction() {
        // 测试NTILE函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        WindowClause<TestEntity> ntileResult = windowClause.ntile(4, "quartile");
        assertNotNull(ntileResult);
        
        WindowClause<TestEntity> ntile10Result = windowClause.ntile(10, "decile");
        assertNotNull(ntile10Result);
    }
    
    @Test
    public void testValueWindowFunctions() {
        // 测试值窗口函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 测试FIRST_VALUE
        WindowClause<TestEntity> firstValueResult = windowClause.firstValue(TestEntity::getName, "first_name");
        assertNotNull(firstValueResult);
        
        // 测试LAST_VALUE
        WindowClause<TestEntity> lastValueResult = windowClause.lastValue(TestEntity::getName, "last_name");
        assertNotNull(lastValueResult);
        
        // 测试NTH_VALUE
        WindowClause<TestEntity> nthValueResult = windowClause.nthValue(TestEntity::getName, 3, "third_name");
        assertNotNull(nthValueResult);
    }
    
    @Test
    public void testWindowWithPartitionAndOrder() {
        // 测试带分区和排序的窗口函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 测试带分区的ROW_NUMBER
        WindowClause<TestEntity> partitionedRowNumber = windowClause.rowNumber(
            java.util.Arrays.asList(TestEntity::getCategory), 
            java.util.Arrays.asList(TestEntity::getCreateTime), 
            "row_num_by_category"
        );
        assertNotNull(partitionedRowNumber);
        
        // 测试带分区的RANK
        WindowClause<TestEntity> partitionedRank = windowClause.rank(
            java.util.Arrays.asList(TestEntity::getCategory), 
            java.util.Arrays.asList(TestEntity::getScore), 
            "rank_by_category"
        );
        assertNotNull(partitionedRank);
    }
    
    @Test
    public void testWindowWithFrom() {
        // 测试窗口函数与FROM子句的组合
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        FromClause<TestEntity> fromClause = windowClause
            .rowNumber("row_num")
            .rank("rank_num")
            .denseRank("dense_rank_num")
            .lag(TestEntity::getAmount, 1, "prev_amount")
            .lead(TestEntity::getAmount, 1, "next_amount")
            .ntile(4, "quartile")
            .firstValue(TestEntity::getName, "first_name")
            .lastValue(TestEntity::getName, "last_name")
            .from("test_entity", "te");
        
        assertNotNull(fromClause);
    }
    
    @Test
    public void testWindowChain() {
        // 测试窗口函数链式调用
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        WindowClause<TestEntity> result = windowClause
            .rowNumber("row_num")
            .rank("rank_num")
            .denseRank("dense_rank_num")
            .percentRank("percent_rank")
            .cumeDist("cume_dist")
            .lag(TestEntity::getAmount, 1, "prev_amount")
            .lead(TestEntity::getAmount, 1, "next_amount")
            .ntile(4, "quartile")
            .firstValue(TestEntity::getName, "first_name")
            .lastValue(TestEntity::getName, "last_name")
            .nthValue(TestEntity::getName, 3, "third_name");
        
        assertNotNull(result);
    }
    
    @Test
    public void testComplexWindowFunctions() {
        // 测试复杂窗口函数组合
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 测试多字段分区
        WindowClause<TestEntity> multiPartitionResult = windowClause.rowNumber(
            java.util.Arrays.asList(TestEntity::getCategory, TestEntity::getStatus), 
            java.util.Arrays.asList(TestEntity::getCreateTime), 
            "row_num_multi_partition"
        );
        assertNotNull(multiPartitionResult);
        
        // 测试多字段排序
        WindowClause<TestEntity> multiOrderResult = windowClause.rank(
            java.util.Arrays.asList(TestEntity::getCategory), 
            java.util.Arrays.asList(TestEntity::getScore, TestEntity::getCreateTime), 
            "rank_multi_order"
        );
        assertNotNull(multiOrderResult);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private String category;
        private String status;
        private Double amount;
        private Double score;
        private java.util.Date createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public java.util.Date getCreateTime() { return createTime; }
        public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }
    }
}
