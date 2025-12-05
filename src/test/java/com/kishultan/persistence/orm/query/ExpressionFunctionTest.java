package com.kishultan.persistence.orm.query;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;
import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;

/**
 * 表达式函数测试类
 */
public class ExpressionFunctionTest {
    
    private DataSource dataSource;
    private QueryBuilder<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的QueryBuilder
        queryBuilder = new StandardQueryBuilder<>(TestEntity.class, null);
    }
    
    @Test
    public void testStringFunctions() {
        // 测试字符串函数
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        // 测试大小写转换
        ExpressionClause<TestEntity> upperResult = expressionClause.upper(TestEntity::getName, "upper_name");
        assertNotNull(upperResult);
        
        ExpressionClause<TestEntity> lowerResult = expressionClause.lower(TestEntity::getName, "lower_name");
        assertNotNull(lowerResult);
        
        // 测试修剪函数
        ExpressionClause<TestEntity> trimResult = expressionClause.trim(TestEntity::getName, "trimmed");
        assertNotNull(trimResult);
        
        // 测试长度函数
        ExpressionClause<TestEntity> lengthResult = expressionClause.length(TestEntity::getName, "name_length");
        assertNotNull(lengthResult);
        
        // 测试字符串操作
        ExpressionClause<TestEntity> replaceResult = expressionClause.replace(TestEntity::getName, "old", "new", "replaced");
        assertNotNull(replaceResult);
        
        // 测试连接函数（简化版本）
        ExpressionClause<TestEntity> concatResult = expressionClause.concat("full_name", "Hello", " ", "World");
        assertNotNull(concatResult);
    }
    
    @Test
    public void testMathFunctions() {
        // 测试数学函数
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        // 测试基本运算
        ExpressionClause<TestEntity> absResult = expressionClause.abs(TestEntity::getAmount, "abs_amount");
        assertNotNull(absResult);
        
        ExpressionClause<TestEntity> ceilResult = expressionClause.ceil(TestEntity::getPrice, "ceil_price");
        assertNotNull(ceilResult);
        
        ExpressionClause<TestEntity> floorResult = expressionClause.floor(TestEntity::getPrice, "floor_price");
        assertNotNull(floorResult);
        
        // 测试四舍五入
        ExpressionClause<TestEntity> roundResult = expressionClause.round(TestEntity::getScore, "rounded_score");
        assertNotNull(roundResult);
        
        ExpressionClause<TestEntity> round2Result = expressionClause.round(TestEntity::getScore, 2, "rounded_score_2");
        assertNotNull(round2Result);
        
        // 测试数学运算
        ExpressionClause<TestEntity> modResult = expressionClause.mod(TestEntity::getId, 10, "mod_10");
        assertNotNull(modResult);
        
        ExpressionClause<TestEntity> powerResult = expressionClause.power(TestEntity::getValue, 2, "squared");
        assertNotNull(powerResult);
        
        ExpressionClause<TestEntity> sqrtResult = expressionClause.sqrt(TestEntity::getArea, "side_length");
        assertNotNull(sqrtResult);
        
        // 测试四则运算
        ExpressionClause<TestEntity> addResult = expressionClause.add(TestEntity::getPrice, TestEntity::getTax, "total");
        assertNotNull(addResult);
        
        ExpressionClause<TestEntity> subtractResult = expressionClause.subtract(TestEntity::getTotal, TestEntity::getDiscount, "final");
        assertNotNull(subtractResult);
    }
    
    @Test
    public void testTimeFunctions() {
        // 测试时间函数
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        // 测试日期提取
        ExpressionClause<TestEntity> yearResult = expressionClause.year(TestEntity::getDate, "year");
        assertNotNull(yearResult);
        
        ExpressionClause<TestEntity> monthResult = expressionClause.month(TestEntity::getDate, "month");
        assertNotNull(monthResult);
        
        ExpressionClause<TestEntity> dayResult = expressionClause.day(TestEntity::getDate, "day");
        assertNotNull(dayResult);
        
        // 测试时间提取
        ExpressionClause<TestEntity> hourResult = expressionClause.hour(TestEntity::getTime, "hour");
        assertNotNull(hourResult);
        
        ExpressionClause<TestEntity> minuteResult = expressionClause.minute(TestEntity::getTime, "minute");
        assertNotNull(minuteResult);
        
        ExpressionClause<TestEntity> secondResult = expressionClause.second(TestEntity::getTime, "second");
        assertNotNull(secondResult);
        
        // 测试日期运算
        ExpressionClause<TestEntity> addDateResult = expressionClause.add(TestEntity::getDate, "DAY", 7, "next_week");
        assertNotNull(addDateResult);
        
        ExpressionClause<TestEntity> subDateResult = expressionClause.sub(TestEntity::getDate, "MONTH", 1, "last_month");
        assertNotNull(subDateResult);
        
        // 测试日期差
        ExpressionClause<TestEntity> diffResult = expressionClause.diff(TestEntity::getStartDate, TestEntity::getEndDate, "duration");
        assertNotNull(diffResult);
        
        // 测试当前时间
        ExpressionClause<TestEntity> nowResult = expressionClause.now("current_time");
        assertNotNull(nowResult);
        
        ExpressionClause<TestEntity> currentResult = expressionClause.current("current_timestamp");
        assertNotNull(currentResult);
    }
    
    @Test
    public void testExpressionWithFrom() {
        // 测试表达式函数与FROM子句的组合
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        FromClause<TestEntity> fromClause = expressionClause
            .upper(TestEntity::getName, "upper_name")
            .lower(TestEntity::getEmail, "lower_email")
            .length(TestEntity::getName, "name_length")
            .round(TestEntity::getScore, 2, "rounded_score")
            .year(TestEntity::getDate, "order_year")
            .from("test_entity", "te");
        
        assertNotNull(fromClause);
    }
    
    @Test
    public void testExpressionChain() {
        // 测试表达式函数链式调用
        ExpressionClause<TestEntity> expressionClause = queryBuilder.expression();
        
        ExpressionClause<TestEntity> result = expressionClause
            .upper(TestEntity::getName, "upper_name")
            .lower(TestEntity::getEmail, "lower_email")
            .length(TestEntity::getName, "name_length")
            .round(TestEntity::getScore, 2, "rounded_score")
            .year(TestEntity::getDate, "order_year");
        
        assertNotNull(result);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private String firstName;
        private String lastName;
        private String email;
        private Double amount;
        private Double price;
        private Double score;
        private Double value;
        private Double area;
        private Double tax;
        private Double total;
        private Double discount;
        private java.util.Date date;
        private java.util.Date time;
        private java.util.Date startDate;
        private java.util.Date endDate;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
        
        public Double getArea() { return area; }
        public void setArea(Double area) { this.area = area; }
        
        public Double getTax() { return tax; }
        public void setTax(Double tax) { this.tax = tax; }
        
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        
        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }
        
        public java.util.Date getDate() { return date; }
        public void setDate(java.util.Date date) { this.date = date; }
        
        public java.util.Date getTime() { return time; }
        public void setTime(java.util.Date time) { this.time = time; }
        
        public java.util.Date getStartDate() { return startDate; }
        public void setStartDate(java.util.Date startDate) { this.startDate = startDate; }
        
        public java.util.Date getEndDate() { return endDate; }
        public void setEndDate(java.util.Date endDate) { this.endDate = endDate; }
    }
}
