package com.kishultan.persistence;

import com.zaxxer.sansorm.SqlClosure;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试 SansORM 日志功能
 */
public class SansOrmLogTest {

    @Test
    public void testSansOrmLogging() {
        System.out.println("=== 测试 SansORM 日志功能 ===");
        
        // 测试 SansORM 的 LoggerFactory
        com.zaxxer.sansorm.LoggerFactory sansormLogger = com.zaxxer.sansorm.LoggerFactory.getLogger(SansOrmLogTest.class);
        System.out.println("SansORM Logger 类型: " + sansormLogger.getClass().getName());
        
        // 测试各种日志级别
        sansormLogger.trace(() -> "这是 SansORM TRACE 日志");
        sansormLogger.debug(() -> "这是 SansORM DEBUG 日志");
        sansormLogger.info(() -> "这是 SansORM INFO 日志");
        sansormLogger.warn(() -> "这是 SansORM WARN 日志");
        sansormLogger.error(() -> "这是 SansORM ERROR 日志");
        
        // 测试 SLF4J 日志
        Logger slf4jLogger = LoggerFactory.getLogger(SansOrmLogTest.class);
        System.out.println("SLF4J Logger 类型: " + slf4jLogger.getClass().getName());
        
        slf4jLogger.trace("这是 SLF4J TRACE 日志");
        slf4jLogger.debug("这是 SLF4J DEBUG 日志");
        slf4jLogger.info("这是 SLF4J INFO 日志");
        slf4jLogger.warn("这是 SLF4J WARN 日志");
        slf4jLogger.error("这是 SLF4J ERROR 日志");
        
        // 测试 SqlClosure 的日志
        try {
            SqlClosure<String> closure = new SqlClosure<>();
            System.out.println("SqlClosure 创建成功");
        } catch (Exception e) {
            System.err.println("SqlClosure 创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void testSansOrmLogLevels() {
        System.out.println("=== 测试 SansORM 日志级别 ===");
        
        com.zaxxer.sansorm.LoggerFactory sansormLogger = com.zaxxer.sansorm.LoggerFactory.getLogger(SansOrmLogTest.class);
        
        // 测试 TRACE 级别
        System.out.println("--- 测试 TRACE 级别 ---");
        sansormLogger.trace(() -> "这是 SansORM TRACE 日志");
        
        // 测试 DEBUG 级别（现在应该显示，因为根级别设置为 debug）
        System.out.println("--- 测试 DEBUG 级别 ---");
        sansormLogger.debug(() -> "这是 SansORM DEBUG 日志");
        
        // 测试 INFO 级别
        System.out.println("--- 测试 INFO 级别 ---");
        sansormLogger.info(() -> "这是 SansORM INFO 日志");
        
        // 测试 WARN 级别
        System.out.println("--- 测试 WARN 级别 ---");
        sansormLogger.warn(() -> "这是 SansORM WARN 日志");
        
        // 测试 ERROR 级别
        System.out.println("--- 测试 ERROR 级别 ---");
        sansormLogger.error(() -> "这是 SansORM ERROR 日志");
    }
    
    @Test
    public void testClassLoader() {
        System.out.println("=== 测试类加载器 ===");
        
        // 检查 SansORM LoggerFactory 的类加载器
        ClassLoader sansormLoader = com.zaxxer.sansorm.LoggerFactory.class.getClassLoader();
        System.out.println("SansORM LoggerFactory 类加载器: " + sansormLoader);
        
        // 检查当前线程的上下文类加载器
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("当前线程上下文类加载器: " + contextLoader);
        
        // 检查系统类加载器
        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        System.out.println("系统类加载器: " + systemLoader);
        
        // 检查 SLF4J 是否可用
        try {
            Class.forName("org.slf4j.LoggerFactory");
            System.out.println("SLF4J LoggerFactory 类可用");
            
            Class.forName("org.slf4j.Logger");
            System.out.println("SLF4J Logger 类可用");
        } catch (ClassNotFoundException e) {
            System.err.println("SLF4J 类不可用: " + e.getMessage());
        }
    }

    @Test
    public void testSlf4jConfiguration() {
        System.out.println("=== 测试 SLF4J 配置 ===");
        
        // 检查 SLF4J 的实现
//        org.slf4j.LoggerFactory slf4jFactory = org.slf4j.LoggerFactory.getILoggerFactory();
//        System.out.println("SLF4J 实现类: " + slf4jFactory.getClass().getName());
        
        // 检查日志级别
        org.slf4j.Logger testLogger = org.slf4j.LoggerFactory.getLogger("com.zaxxer.sansorm");
        System.out.println("SansORM 包的日志级别检查:");
        System.out.println("  TRACE 启用: " + testLogger.isTraceEnabled());
        System.out.println("  DEBUG 启用: " + testLogger.isDebugEnabled());
        System.out.println("  INFO 启用: " + testLogger.isInfoEnabled());
        System.out.println("  WARN 启用: " + testLogger.isWarnEnabled());
        System.out.println("  ERROR 启用: " + testLogger.isErrorEnabled());
        
        // 测试不同级别的日志
        System.out.println("--- 测试 SLF4J 日志级别 ---");
        testLogger.trace("这是 SLF4J TRACE 日志 - 应该不显示");
        testLogger.debug("这是 SLF4J DEBUG 日志 - 应该不显示");
        testLogger.info("这是 SLF4J INFO 日志 - 应该显示");
        testLogger.warn("这是 SLF4J WARN 日志 - 应该显示");
        testLogger.error("这是 SLF4J ERROR 日志 - 应该显示");
    }
}
