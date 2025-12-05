package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * FromClause where(Consumer)方法测试
 * 
 * @author Portal Team
 */
public class FromClauseWhereConsumerTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private StandardQueryBuilder<User> queryBuilder;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 设置Mock对象
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // 空结果集
        
        // 创建QueryBuilder
        queryBuilder = new StandardQueryBuilder<>(User.class, dataSource);
    }
    
    @Test
    public void testWhereConsumer() {
        // 测试新的where(Consumer)方法
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"));
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        
        // 验证可以继续链式调用
        List<User> users = whereClause.findList();
        assertNotNull("查询结果不应为null", users);
    }
    
    @Test
    public void testWhereConsumerWithMultipleConditions() {
        // 测试多个条件
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(w -> {
                w.eq(User::getName, "John");
                w.like(User::getEmail, "@gmail.com");
                w.gt(User::getId, 100L);
            });
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        
        // 验证可以继续链式调用
        List<User> users = whereClause.findList();
        assertNotNull("查询结果不应为null", users);
    }
    
    @Test
    public void testWhereConsumerWithNull() {
        // 测试传入null Consumer
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(null);
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        
        // 验证可以继续链式调用
        List<User> users = whereClause.findList();
        assertNotNull("查询结果不应为null", users);
    }
    
    @Test
    public void testWhereConsumerChaining() {
        // 测试链式调用
        List<User> users = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .and(w -> w.like(User::getEmail, "@gmail.com"))
            .findList();
        
        assertNotNull("查询结果不应为null", users);
    }
    
    // 测试用户类
    public static class User {
        private Long id;
        private String name;
        private String email;
        
        public User() {}
        
        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}



