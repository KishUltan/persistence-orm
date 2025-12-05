package com.kishultan.persistence;

import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.config.PersistenceDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.After;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * 数据源初始化测试用例
 * 验证DataSourceManager.addLocalDataSource是否正常工作
 */
public class DataSourceTest {
    
    @Before
    public void setUp() throws Exception {
        // 设置默认数据源名称
        PersistenceDefaults.setDataSourceName("default");
        
        // 设置测试环境（禁用JNDI，启用本地数据源）
        DataSourceManager.setUseJNDI(false);
        
        // 创建H2内存数据库数据源
        org.h2.jdbcx.JdbcDataSource h2DataSource = new org.h2.jdbcx.JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");
        
        // 将数据源添加到本地数据源表
        DataSourceManager.addLocalDataSource("default", h2DataSource);
        
        // 添加数据源类型标识
        DataSourceManager.addDataSourceFlavor("default", "h2");
    }
    
    @After
    public void tearDown() throws Exception {
        // 清理数据源
        DataSourceManager.removeLocalDataSource("default");
        DataSourceManager.setUseJNDI(true);
    }
    
    @Test
    public void testDataSourceInitialization() throws Exception {
        // 验证数据源是否正确添加
        Assert.assertTrue("数据源应该存在", DataSourceManager.hasDataSource("default"));
        
        // 获取数据源
        DataSource dataSource = DataSourceManager.getDataSource("default");
        Assert.assertNotNull("数据源不应为null", dataSource);
        
        // 测试连接
        try (Connection conn = dataSource.getConnection()) {
            Assert.assertNotNull("连接不应为null", conn);
            Assert.assertFalse("连接不应为关闭状态", conn.isClosed());
            
            // 创建测试表
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100))");
                
                // 插入测试数据
                stmt.execute("INSERT INTO test_table (name) VALUES ('test1'), ('test2')");
                
                // 查询测试数据
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table");
                rs.next();
                int count = rs.getInt(1);
                
                Assert.assertEquals("应该有2条测试数据", 2, count);
            }
        }
        
        System.out.println("✓ 数据源初始化测试通过");
    }
    
    @Test
    public void testDataSourceManagerIntegration() throws Exception {
        // 测试通过DataSourceManager.getConnection()获取连接
        try (Connection conn = DataSourceManager.getConnection()) {
            Assert.assertNotNull("通过DataSourceManager获取的连接不应为null", conn);
            
            // 执行简单查询
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS integration_test (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "test_value VARCHAR(100))");
                
                stmt.execute("INSERT INTO integration_test (test_value) VALUES ('integration_test_value')");
                
                ResultSet rs = stmt.executeQuery("SELECT test_value FROM integration_test WHERE id = 1");
                rs.next();
                String value = rs.getString("test_value");
                
                Assert.assertEquals("应该能正确查询到数据", "integration_test_value", value);
            }
        }
        
        System.out.println("✓ DataSourceManager集成测试通过");
    }
    
    @Test
    public void testDataSourceFlavor() throws Exception {
        // 验证数据源类型标识
        String flavor = DataSourceManager.getDataSourceFlavor("default");
        Assert.assertEquals("数据源类型应该是h2", "h2", flavor);
        
        System.out.println("✓ 数据源类型标识测试通过");
    }
    
    @Test
    public void testDataSourceCleanup() throws Exception {
        // 验证数据源清理
        Assert.assertTrue("清理前数据源应该存在", DataSourceManager.hasDataSource("default"));
        
        DataSourceManager.removeLocalDataSource("default");
        
        Assert.assertFalse("清理后数据源应该不存在", DataSourceManager.hasDataSource("default"));
        
        System.out.println("✓ 数据源清理测试通过");
    }
} 