package com.kishultan.persistence;

import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.orm.EntityManager;
import com.kishultan.persistence.orm.EntityQuery;
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.model.TestClinic;
import com.kishultan.persistence.model.TestContact;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Fetch查询测试
 * 
 * 测试关联查询的fetch功能
 * 
 * @author Portal Team
 */
public class FetchQueryTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchQueryTest.class);
    
    @Before
    public void setUp() throws Exception {
        // 设置本地数据源用于测试
        DataSourceManager.setUseJNDI(false);
        
        // 创建测试数据源
        //org.apache.commons.dbcp2.BasicDataSource dataSource = new org.apache.commons.dbcp2.BasicDataSource();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:fetch_query_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        
        DataSourceManager.addLocalDataSource("default", dataSource);
        
        // 创建测试表和数据
        createTestData();
    }
    
    /**
     * 创建测试数据
     */
    private void createTestData() throws Exception {
        try (java.sql.Connection conn = DataSourceManager.getDataSource("default").getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // 创建诊所表
            stmt.execute("CREATE TABLE IF NOT EXISTS his_clinics (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "region_code VARCHAR(20), " +
                "region_name VARCHAR(100), " +
                "address VARCHAR(200), " +
                "state INT DEFAULT 1, " +
                "ctime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "createdby VARCHAR(50))");
            
            // 创建联系人表
            stmt.execute("CREATE TABLE IF NOT EXISTS his_contacts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "phone VARCHAR(20), " +
                "address VARCHAR(200), " +
                "birthday DATE, " +
                "remark VARCHAR(500), " +
                "email VARCHAR(100), " +
                "gender VARCHAR(10), " +
                "state INT DEFAULT 1, " +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
            
            // 创建诊所-联系人关联表
            stmt.execute("CREATE TABLE IF NOT EXISTS his_clinic_contacts (" +
                "clinic_id INT, " +
                "contact_id INT, " +
                "PRIMARY KEY (clinic_id, contact_id))");
            
            // 清空表数据，避免数据累积
            stmt.execute("DELETE FROM his_clinic_contacts");
            stmt.execute("DELETE FROM his_contacts");
            stmt.execute("DELETE FROM his_clinics");
            
            // 插入诊所测试数据
            stmt.execute("INSERT INTO his_clinics (name, region_code, region_name, address, state, createdby) VALUES " +
                "('Test Clinic 1', 'REGION_001', 'Test Region 1', 'Test Address 1', 1, 'test_user'), " +
                "('Test Clinic 2', 'REGION_002', 'Test Region 2', 'Test Address 2', 1, 'test_user'), " +
                "('Test Clinic 3', 'REGION_001', 'Test Region 1', 'Test Address 3', 1, 'test_user')");
            
            // 插入联系人测试数据
            stmt.execute("INSERT INTO his_contacts (name, phone, email) VALUES " +
                "('Contact 1', '123-456-7890', 'contact1@test.com'), " +
                "('Contact 2', '123-456-7891', 'contact2@test.com'), " +
                "('Contact 3', '123-456-7892', 'contact3@test.com'), " +
                "('Contact 4', '123-456-7893', 'contact4@test.com')");
            
            // 插入关联数据
            stmt.execute("INSERT INTO his_clinic_contacts (clinic_id, contact_id) VALUES " +
                "(1, 1), (1, 2), (2, 3), (3, 4)");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        // 清理数据源
        DataSourceManager.removeLocalDataSource("default");
        DataSourceManager.setUseJNDI(true);
    }
    
    /**
     * 测试fetch功能 - 不调用fetch
     */
    @Test
    public void testQueryWithoutFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，不填充contacts
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证不fetch时contacts应该为null或空
            Assert.assertTrue("不fetch时contacts应该为null或空", 
                           clinic.getContacts() == null || clinic.getContacts().isEmpty());
        }
    }
    
    /**
     * 测试fetch功能 - 调用fetch
     */
    @Test
    public void testQueryWithFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，并填充contacts
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        //query.fetch("contacts");  // 填充contacts关联
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证fetch时contacts应该不为空
            Assert.assertNotNull("fetch时contacts不应为null", clinic.getContacts());
            Assert.assertTrue("fetch时contacts应该不为空", !clinic.getContacts().isEmpty());
            
            // 输出联系人信息
            if (clinic.getContacts() != null) {
                for (TestContact contact : clinic.getContacts()) {
                    logger.info("  - 联系人: {}, 电话: {}", contact.getName(), contact.getPhone());
                }
            }
        }
    }
    
    /**
     * 测试fetch多个属性
     */
    @Test
    public void testQueryWithMultipleFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，并填充contacts关联属性
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        //query.fetch("contacts");  // 填充contacts关联属性
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("多属性fetch查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证多属性fetch时contacts应该不为空
            Assert.assertNotNull("多属性fetch时contacts不应为null", clinic.getContacts());
            Assert.assertTrue("多属性fetch时contacts应该不为空", !clinic.getContacts().isEmpty());
        }
    }
    
    /**
     * 测试Lambda版本的fetch功能 - 单个属性
     */
    @Test
    public void testQueryWithLambdaFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，使用Lambda表达式填充contacts
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        //query.fetch(TestClinic::getContacts);  // 使用Lambda表达式
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("Lambda fetch查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证Lambda fetch时contacts应该不为空
            Assert.assertNotNull("Lambda fetch时contacts不应为null", clinic.getContacts());
            Assert.assertTrue("Lambda fetch时contacts应该不为空", !clinic.getContacts().isEmpty());
            
            // 输出联系人信息
            if (clinic.getContacts() != null) {
                for (TestContact contact : clinic.getContacts()) {
                    logger.info("  - 联系人: {}, 电话: {}", contact.getName(), contact.getPhone());
                }
            }
        }
    }
    
    /**
     * 测试Lambda版本的fetch功能 - 多个属性
     */
    @Test
    public void testQueryWithMultipleLambdaFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，使用Lambda表达式填充多个关联属性
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        //query.fetch(TestClinic::getContacts);  // 使用Lambda表达式
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("Lambda多属性fetch查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证Lambda多属性fetch时contacts应该不为空
            Assert.assertNotNull("Lambda多属性fetch时contacts不应为null", clinic.getContacts());
            Assert.assertTrue("Lambda多属性fetch时contacts应该不为空", !clinic.getContacts().isEmpty());
        }
    }
    
    /**
     * 测试Lambda版本的fetch功能 - 混合使用
     */
    @Test
    public void testQueryWithMixedFetch() {
        EntityManager em = PersistenceManager.getDefaultManager();
        
        // 查询Clinic，混合使用字符串和Lambda表达式
        EntityQuery<TestClinic> query = em.createQuery(TestClinic.class);
        //query.fetch("contacts");  // 字符串方式
        //query.fetch(Clinic::getContacts);  // Lambda方式（重复添加，测试兼容性）
        query.where().eq("state", 1);
        List<TestClinic> clinics = query.findList();
        
        logger.info("混合fetch查询到 {} 个诊所", clinics.size());
        
        // 验证查询结果
        Assert.assertNotNull("查询结果不应为null", clinics);
        Assert.assertEquals("应该有3个诊所", 3, clinics.size());
        
        for (TestClinic clinic : clinics) {
            logger.info("诊所: {}, 联系人数量: {}", 
                       clinic.getName(), 
                       clinic.getContacts() != null ? clinic.getContacts().size() : "null");
            
            // 验证混合fetch时contacts应该不为空
            Assert.assertNotNull("混合fetch时contacts不应为null", clinic.getContacts());
            Assert.assertTrue("混合fetch时contacts应该不为空", !clinic.getContacts().isEmpty());
        }
    }
} 