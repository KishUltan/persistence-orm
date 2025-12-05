package com.kishultan.persistence;

import com.kishultan.persistence.config.PersistenceDefaults;
import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.orm.EntityManager;
import com.kishultan.persistence.orm.EntityManagerFactory;
import com.kishultan.persistence.orm.delegate.SansOrmFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 持久化管理器
 * 
 * 提供统一的持久化操作入口
 * 线程安全实现，使用原子引用和并发缓存
 * 
 * @author Portal Team
 */
public class PersistenceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
    
    private static final String DEFAULT_DATASOURCE_NAME = "default";
    
    // 使用原子引用确保线程安全
    private static final AtomicReference<EntityManagerFactory> defaultManagerRef = new AtomicReference<>();
    
    // 使用并发缓存存储不同数据源的EntityManagerFactory实例
    private static final ConcurrentHashMap<String, EntityManagerFactory> managerCache = new ConcurrentHashMap<>();
    
    // 工厂提供者（直接依赖delegate包，无循环依赖）
    private static final SansOrmFactoryProvider factoryProvider = new SansOrmFactoryProvider();
    
    /**
     * 获取默认的EntityManager
     * 
     * 使用原子操作确保线程安全，避免双重检查锁定的复杂性
     */
    public static EntityManager getDefaultManager() {
        EntityManagerFactory manager = defaultManagerRef.get();
        if (manager == null) {
            // 直接使用工厂提供者创建EntityManagerFactory实例
            EntityManagerFactory newManager = createEntityManagerFactory(
                DataSourceManager.getDataSource(PersistenceDefaults.getDataSourceName())
            );
            
            // 原子性地设置默认管理器
            if (defaultManagerRef.compareAndSet(null, newManager)) {
                logger.info("初始化默认EntityManager");
            } else {
                // 如果另一个线程已经设置了，使用已设置的值
                manager = defaultManagerRef.get();
            }
        }
        
        // 每次都创建新的EntityManager实例，确保线程安全
        return new EntityManager(manager != null ? manager : defaultManagerRef.get());
    }
    
    /**
     * 获取指定数据源的EntityManager
     * 
     * 使用缓存避免重复创建EntityManagerFactory实例
     */
    public static EntityManager getManager(String dataSourceName) {
        logger.debug("获取EntityManager，数据源: {}", dataSourceName);
        
        // 从缓存中获取或创建EntityManagerFactory
        EntityManagerFactory factory = managerCache.computeIfAbsent(dataSourceName, name -> {
            logger.debug("为数据源创建新的EntityManagerFactory: {}", name);
            return createEntityManagerFactory(DataSourceManager.getDataSource(name));
        });
        
        // 每次都创建新的EntityManager实例，确保线程安全
        return new EntityManager(factory);
    }
    
    /**
     * 关闭默认管理器
     * 
     * 线程安全的方法，使用原子操作
     */
    public static void shutdown() {
        EntityManagerFactory manager = defaultManagerRef.get();
        if (manager != null && defaultManagerRef.compareAndSet(manager, null)) {
            logger.info("关闭默认EntityManager");
            manager.shutdown();
        }
    }
    
    /**
     * 关闭指定数据源的管理器
     * 
     * 线程安全的方法，从缓存中移除
     */
    public static void shutdown(String dataSourceName) {
        EntityManagerFactory manager = managerCache.remove(dataSourceName);
        if (manager != null) {
            logger.info("关闭数据源EntityManager: {}", dataSourceName);
            manager.shutdown();
        }
    }
    
    /**
     * 关闭所有管理器
     * 
     * 线程安全的方法，清理所有缓存的管理器
     */
    public static void shutdownAll() {
        // 关闭默认管理器
        shutdown();
        
        // 关闭所有缓存的管理器
        managerCache.forEach((name, manager) -> {
            logger.info("关闭数据源EntityManager: {}", name);
            manager.shutdown();
        });
        managerCache.clear();
    }
    
    /**
     * 检查默认数据源是否可用
     */
    public static boolean isDefaultDataSourceAvailable() {
        boolean available = DataSourceManager.hasDataSource(DEFAULT_DATASOURCE_NAME);
        logger.debug("默认数据源可用性检查: {}", available);
        return available;
    }
    
    /**
     * 检查指定数据源是否可用
     */
    public static boolean isDataSourceAvailable(String dataSourceName) {
        boolean available = DataSourceManager.hasDataSource(dataSourceName);
        logger.debug("数据源可用性检查: {} = {}", dataSourceName, available);
        return available;
    }
    
    /**
     * 获取缓存的管理器数量
     * 
     * 用于监控和调试
     */
    public static int getCachedManagerCount() {
        return managerCache.size();
    }
    
    /**
     * 清理缓存的管理器
     * 
     * 用于测试或内存管理
     */
    public static void clearCache() {
        int count = managerCache.size();
        managerCache.clear();
        logger.info("清理EntityManagerFactory缓存，共清理 {} 个管理器", count);
    }
    
    /**
     * 创建EntityManagerFactory实例
     * 直接使用工厂提供者，无需SPI或反射
     * 
     * @param dataSource 数据源
     * @return EntityManagerFactory实例
     */
    private static EntityManagerFactory createEntityManagerFactory(javax.sql.DataSource dataSource) {
        return factoryProvider.createFactory(dataSource);
    }
}

