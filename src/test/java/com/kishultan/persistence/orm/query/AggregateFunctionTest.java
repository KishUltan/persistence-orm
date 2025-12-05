package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;

/**
 * 聚合函数测试类
 */
public class AggregateFunctionTest {
    
    private DataSource dataSource;
    private QueryBuilder<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 这里需要设置测试数据源
        // dataSource = createTestDataSource();
        // queryBuilder = new StandardQueryBuilder<>(TestEntity.class, dataSource);
        
        // 为了测试，创建一个模拟的QueryBuilder
        queryBuilder = new StandardQueryBuilder<>(TestEntity.class, null);
    }
    
    @Test
    public void testAggregateFunctionBasic() {
        // 测试基本的聚合函数调用
        AggregateClause<TestEntity> aggregateClause = queryBuilder.aggregate();
        
        // 验证方法存在且可以调用
        assertNotNull(aggregateClause);
        
        // 测试计数函数
        AggregateClause<TestEntity> countResult = aggregateClause.count(TestEntity::getId);
        assertNotNull(countResult);
        
        // 测试带别名的计数函数
        AggregateClause<TestEntity> countWithAlias = aggregateClause.count(TestEntity::getId, "total_count");
        assertNotNull(countWithAlias);
        
        // 测试求和函数
        AggregateClause<TestEntity> sumResult = aggregateClause.sum(TestEntity::getAmount);
        assertNotNull(sumResult);
        
        // 测试平均值函数
        AggregateClause<TestEntity> avgResult = aggregateClause.avg(TestEntity::getAmount);
        assertNotNull(avgResult);
        
        // 测试最大值函数
        AggregateClause<TestEntity> maxResult = aggregateClause.max(TestEntity::getAmount);
        assertNotNull(maxResult);
        
        // 测试最小值函数
        AggregateClause<TestEntity> minResult = aggregateClause.min(TestEntity::getAmount);
        assertNotNull(minResult);
    }
    
    @Test
    public void testAggregateFunctionChain() {
        // 测试聚合函数链式调用
        AggregateClause<TestEntity> aggregateClause = queryBuilder.aggregate();
        
        AggregateClause<TestEntity> result = aggregateClause
            .count(TestEntity::getId, "total_count")
            .sum(TestEntity::getAmount, "total_amount")
            .avg(TestEntity::getAmount, "avg_amount")
            .max(TestEntity::getAmount, "max_amount")
            .min(TestEntity::getAmount, "min_amount");
        
        assertNotNull(result);
    }
    
    @Test
    public void testAggregateWithFrom() {
        // 测试聚合函数与FROM子句的组合
        AggregateClause<TestEntity> aggregateClause = queryBuilder.aggregate();
        
        FromClause<TestEntity> fromClause = aggregateClause
            .count(TestEntity::getId, "total_count")
            .sum(TestEntity::getAmount, "total_amount")
            .from("test_entity", "te");
        
        assertNotNull(fromClause);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private Double amount;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}

