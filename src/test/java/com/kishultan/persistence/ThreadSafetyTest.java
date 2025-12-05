package com.kishultan.persistence;

import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.orm.EntityManager;
import com.kishultan.persistence.PersistenceManager;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全测试
 * 
 * 测试持久层在多线程环境下的安全性
 * 
 * @author Portal Team
 */
public class ThreadSafetyTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadSafetyTest.class);
    
    @Before
    public void setUp() {
        // 设置本地数据源用于测试
        DataSourceManager.setUseJNDI(false);
        
        // 创建测试数据源，增加连接池大小
        //org.apache.commons.dbcp2.BasicDataSource dataSource = new org.apache.commons.dbcp2.BasicDataSource();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:thread_safety_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        
        // 增加连接池配置，确保有足够的连接
//        dataSource.setInitialSize(20);
//        dataSource.setMaxTotal(50);
//        dataSource.setMaxIdle(20);
//        dataSource.setMinIdle(10);
//        dataSource.setMaxWaitMillis(10000);
        
        DataSourceManager.addLocalDataSource("default", dataSource);
    }
    
    /**
     * 测试PersistenceManager.getDefaultManager()的线程安全性
     */
    @Test
    public void testPersistenceManagerThreadSafety() throws InterruptedException {
        final int threadCount = 5; // 增加线程数
        final int iterationsPerThread = 20; // 增加迭代次数
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        
        logger.info("开始测试PersistenceManager线程安全性，线程数: {}, 每线程迭代次数: {}", threadCount, iterationsPerThread);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            EntityManager em = PersistenceManager.getDefaultManager();
                            if (em != null) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            logger.error("线程 {} 第 {} 次迭代失败", threadId, j, e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 添加超时机制
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            logger.error("测试超时，强制关闭线程池");
            executor.shutdownNow();
            throw new RuntimeException("测试超时");
        }
        
        executor.shutdown();
        
        int totalOperations = threadCount * iterationsPerThread;
        logger.info("测试完成 - 总操作数: {}, 成功: {}, 失败: {}", 
                   totalOperations, successCount.get(), errorCount.get());
        
        // 验证所有操作都成功
        assert successCount.get() == totalOperations : 
            "期望所有操作成功，但实际成功: " + successCount.get() + ", 失败: " + errorCount.get();
        assert errorCount.get() == 0 : 
            "期望没有失败操作，但实际失败: " + errorCount.get();
    }
    
    /**
     * 测试DataSourceManager.setUseJNDI()的线程安全性
     * 现在应该完全线程安全
     */
    @Test
    public void testDataSourceManagerThreadSafety() throws InterruptedException {
        final int threadCount = 5; // 增加线程数
        final int iterationsPerThread = 20; // 增加迭代次数
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        
        logger.info("开始测试DataSourceManager线程安全性，线程数: {}, 每线程迭代次数: {}", threadCount, iterationsPerThread);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            // 交替设置JNDI模式
                            boolean useJNDI = (j % 2 == 0);
                            DataSourceManager.setUseJNDI(useJNDI);
                            
                            // 验证设置是否生效
                            boolean currentUseJNDI = DataSourceManager.isUseJNDI();
                            if (currentUseJNDI == useJNDI) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                                logger.error("线程 {} 第 {} 次迭代：设置值 {} 与当前值 {} 不匹配", 
                                           threadId, j, useJNDI, currentUseJNDI);
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            logger.error("线程 {} 第 {} 次迭代失败", threadId, j, e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 添加超时机制
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            logger.error("测试超时，强制关闭线程池");
            executor.shutdownNow();
            throw new RuntimeException("测试超时");
        }
        
        executor.shutdown();
        
        int totalOperations = threadCount * iterationsPerThread;
        logger.info("测试完成 - 总操作数: {}, 成功: {}, 失败: {}", 
                   totalOperations, successCount.get(), errorCount.get());
        
        // 现在应该完全线程安全，期望所有操作成功
        assert successCount.get() == totalOperations : 
            "期望所有操作成功，但实际成功: " + successCount.get() + ", 失败: " + errorCount.get();
        assert errorCount.get() == 0 : 
            "期望没有失败操作，但实际失败: " + errorCount.get();
    }
    
    /**
     * 测试EntityManager实例的线程安全性
     * 现在使用ThreadLocal，应该完全线程安全
     */
    @Test
    public void testEntityManagerInstanceThreadSafety() throws InterruptedException {
        final int threadCount = 5; // 增加线程数
        final int iterationsPerThread = 10; // 增加迭代次数
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        
        logger.info("开始测试EntityManager实例线程安全性，线程数: {}, 每线程迭代次数: {}", threadCount, iterationsPerThread);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // 每个线程使用独立的EntityManager实例
                    EntityManager em = PersistenceManager.getManager("default");
                    
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            // 测试事务操作
                            em.beginTransaction();
                            
                            // 模拟一些操作，减少等待时间
                            Thread.sleep(5);
                            
                            em.commitTransaction();
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            logger.error("线程 {} 第 {} 次迭代失败", threadId, j, e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 添加超时机制
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            logger.error("测试超时，强制关闭线程池");
            executor.shutdownNow();
            throw new RuntimeException("测试超时");
        }
        
        executor.shutdown();
        
        int totalOperations = threadCount * iterationsPerThread;
        logger.info("测试完成 - 总操作数: {}, 成功: {}, 失败: {}", 
                   totalOperations, successCount.get(), errorCount.get());
        
        // 现在应该完全线程安全，期望所有操作成功
        assert successCount.get() == totalOperations : 
            "期望所有操作成功，但实际成功: " + successCount.get() + ", 失败: " + errorCount.get();
        assert errorCount.get() == 0 : 
            "期望没有失败操作，但实际失败: " + errorCount.get();
    }
    
    /**
     * 测试多个EntityManager实例的并发安全性
     */
    @Test
    public void testMultipleEntityManagerConcurrency() throws InterruptedException {
        final int threadCount = 3;
        final int iterationsPerThread = 5;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        
        logger.info("开始测试多个EntityManager实例并发安全性，线程数: {}, 每线程迭代次数: {}", threadCount, iterationsPerThread);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            // 每次迭代都创建新的EntityManager实例
                            EntityManager em = PersistenceManager.getManager("default");
                            
                            // 测试事务操作
                            em.beginTransaction();
                            Thread.sleep(2);
                            em.commitTransaction();
                            
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            logger.error("线程 {} 第 {} 次迭代失败", threadId, j, e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 添加超时机制
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            logger.error("测试超时，强制关闭线程池");
            executor.shutdownNow();
            throw new RuntimeException("测试超时");
        }
        
        executor.shutdown();
        
        int totalOperations = threadCount * iterationsPerThread;
        logger.info("测试完成 - 总操作数: {}, 成功: {}, 失败: {}", 
                   totalOperations, successCount.get(), errorCount.get());
        
        // 验证所有操作都成功
        assert successCount.get() == totalOperations : 
            "期望所有操作成功，但实际成功: " + successCount.get() + ", 失败: " + errorCount.get();
        assert errorCount.get() == 0 : 
            "期望没有失败操作，但实际失败: " + errorCount.get();
    }
} 