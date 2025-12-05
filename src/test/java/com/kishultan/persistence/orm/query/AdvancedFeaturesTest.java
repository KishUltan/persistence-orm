package com.kishultan.persistence.orm.query;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;

/**
 * 高级功能测试类
 * 测试聚合函数、窗口函数、表达式函数等新功能
 */
public class AdvancedFeaturesTest {
    
    private DataSource dataSource;
    private QueryBuilder<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 这里需要设置测试数据源
        // dataSource = createTestDataSource();
        // queryBuilder = new StandardQueryBuilder<>(TestEntity.class, dataSource);
    }
    
    @Test
    void testAggregateFunctions() {
        // 测试聚合函数
        AggregateClause<TestEntity> aggregateClause = queryBuilder.aggregate();
        
        // 验证方法存在且可以调用
        assertNotNull(aggregateClause);
        
        // 测试计数函数
        AggregateClause<TestEntity> countResult = aggregateClause.count(TestEntity::getId, "total_count");
        assertNotNull(countResult);
        
        // 测试求和函数
        AggregateClause<TestEntity> sumResult = aggregateClause.sum(TestEntity::getAmount, "total_amount");
        assertNotNull(sumResult);
        
        // 测试平均值函数
        AggregateClause<TestEntity> avgResult = aggregateClause.avg(TestEntity::getAmount, "avg_amount");
        assertNotNull(avgResult);
        
        // 测试最大值函数
        AggregateClause<TestEntity> maxResult = aggregateClause.max(TestEntity::getAmount, "max_amount");
        assertNotNull(maxResult);
        
        // 测试最小值函数
        AggregateClause<TestEntity> minResult = aggregateClause.min(TestEntity::getAmount, "min_amount");
        assertNotNull(minResult);
    }
    
    @Test
    void testWindowFunctions() {
        // 测试窗口函数
        WindowClause<TestEntity> windowClause = queryBuilder.window();
        
        // 验证方法存在且可以调用
        assertNotNull(windowClause);
        
        // 测试行号函数
        WindowClause<TestEntity> rowNumberResult = windowClause.rowNumber("row_num");
        assertNotNull(rowNumberResult);
        
        // 测试排名函数
        WindowClause<TestEntity> rankResult = windowClause.rank("rank_num");
        assertNotNull(rankResult);
        
        // 测试密集排名函数
        WindowClause<TestEntity> denseRankResult = windowClause.denseRank("dense_rank_num");
        assertNotNull(denseRankResult);
        
        // 测试滞后值函数
        WindowClause<TestEntity> lagResult = windowClause.lag(TestEntity::getAmount, 1, "prev_amount");
        assertNotNull(lagResult);
        
        // 测试领先值函数
        WindowClause<TestEntity> leadResult = windowClause.lead(TestEntity::getAmount, 1, "next_amount");
        assertNotNull(leadResult);
    }
    
    @Test
    void testExpressionFunctions() {
        // 测试表达式函数
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        // 验证方法存在且可以调用
        assertNotNull(expressionClause);
        
        // 测试时间函数
        ExpressionClause<TestEntity> yearResult = expressionClause.year(TestEntity::getCreateTime, "year");
        assertNotNull(yearResult);
        
        ExpressionClause<TestEntity> monthResult = expressionClause.month(TestEntity::getCreateTime, "month");
        assertNotNull(monthResult);
        
        // 测试字符串函数
        ExpressionClause<TestEntity> upperResult = expressionClause.upper(TestEntity::getName, "upper_name");
        assertNotNull(upperResult);
        
        ExpressionClause<TestEntity> lowerResult = expressionClause.lower(TestEntity::getName, "lower_name");
        assertNotNull(lowerResult);
        
        ExpressionClause<TestEntity> lengthResult = expressionClause.length(TestEntity::getName, "name_length");
        assertNotNull(lengthResult);
        
        // 测试数学函数
        ExpressionClause<TestEntity> absResult = expressionClause.abs(TestEntity::getAmount, "abs_amount");
        assertNotNull(absResult);
        
        ExpressionClause<TestEntity> roundResult = expressionClause.round(TestEntity::getAmount, "round_amount");
        assertNotNull(roundResult);
        
        ExpressionClause<TestEntity> addResult = expressionClause.add(TestEntity::getAmount, TestEntity::getAmount, "double_amount");
        assertNotNull(addResult);
    }
    
    @Test
    void testFunctionChaining() {
        // 测试函数链式调用
        AggregateClause<TestEntity> aggregateResult = queryBuilder.aggregate()
            .count(TestEntity::getId, "total_count")
            .sum(TestEntity::getAmount, "total_amount")
            .avg(TestEntity::getAmount, "avg_amount")
            .max(TestEntity::getAmount, "max_amount")
            .min(TestEntity::getAmount, "min_amount");
        
        assertNotNull(aggregateResult);
        
        // 测试与FROM子句的组合
        FromClause<TestEntity> fromClause = aggregateResult.from("test_entity", "te");
        assertNotNull(fromClause);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private Double amount;
        private java.util.Date createTime;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public java.util.Date getCreateTime() { return createTime; }
        public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }
    }
}

