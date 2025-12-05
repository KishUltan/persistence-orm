package com.kishultan.persistence;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试Logback配置和SansORM日志集成
 */
public class LogbackIntegrationTest {

    @Test
    public void testLogbackConfiguration() {
        System.out.println("=== 测试 Logback 配置 ===");
        
        // 1. 检查 SLF4J 实现
        org.slf4j.ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        System.out.println("SLF4J 实现类: " + factory.getClass().getName());
        
        if (factory.getClass().getName().contains("logback")) {
            System.out.println("✓ 成功使用 Logback 作为 SLF4J 实现");
        } else {
            System.out.println("✗ 当前使用的不是 Logback: " + factory.getClass().getName());
        }
        
        // 2. 测试日志输出
        Logger logger = LoggerFactory.getLogger(LogbackIntegrationTest.class);
        logger.debug("这是 DEBUG 级别日志");
        logger.info("这是 INFO 级别日志");
        logger.warn("这是 WARN 级别日志");
        logger.error("这是 ERROR 级别日志");
        
        // 3. 测试SansORM Logger
        com.zaxxer.sansorm.LoggerFactory sansormLogger = 
            com.zaxxer.sansorm.LoggerFactory.getLogger(LogbackIntegrationTest.class);
        System.out.println("SansORM Logger 类型: " + sansormLogger.getClass().getName());
        
        sansormLogger.debug(() -> "SansORM DEBUG 日志测试");
        sansormLogger.info(() -> "SansORM INFO 日志测试");
        
        System.out.println("=== Logback 配置测试完成 ===");
    }
    
    @Test
    public void testSansOrmSpecificPackageLogging() {
        System.out.println("=== 测试 SansORM 包级别日志配置 ===");
        
        // 创建不同包下的Logger来测试配置
        Logger kishultanLogger = LoggerFactory.getLogger("com.kishultan.persistence.TestClass");
        Logger sansormLogger = LoggerFactory.getLogger("com.zaxxer.sansorm.TestClass");
        
        System.out.println("com.kishultan.persistence DEBUG启用: " + kishultanLogger.isDebugEnabled());
        System.out.println("com.zaxxer.sansorm DEBUG启用: " + sansormLogger.isDebugEnabled());
        
        // 输出测试日志
        kishultanLogger.debug("Kishultan包 DEBUG 日志");
        sansormLogger.debug("SansORM包 DEBUG 日志");
        
        System.out.println("=== 包级别日志配置测试完成 ===");
    }
}