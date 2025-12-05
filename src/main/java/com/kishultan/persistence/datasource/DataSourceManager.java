package com.kishultan.persistence.datasource;

import com.kishultan.persistence.config.PersistenceDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

/**
 * 数据源管理器
 * 
 * 支持JNDI数据源和本地数据源的管理
 * 线程安全实现，使用StampedLock提供更好的读写性能
 * 
 * @author Portal Team
 */
public class DataSourceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
    
    // 使用AtomicBoolean确保线程安全
    private static final AtomicBoolean useJNDI = new AtomicBoolean(true);
    
    // 使用StampedLock提供更好的读写性能
    private static final StampedLock stateLock = new StampedLock();
    
    private static final Map<String, DataSource> localDSTable = new ConcurrentHashMap<>();
    private static final Map<String, String> dsFlavorsTable = new ConcurrentHashMap<>();
    
    /**
     * 设置是否使用JNDI
     * 
     * 线程安全的方法，使用写锁确保一致性
     */
    public static void setUseJNDI(boolean useJNDIValue) {
        long stamp = stateLock.writeLock();
        try {
            boolean oldValue = useJNDI.getAndSet(useJNDIValue);
            if (oldValue != useJNDIValue) {
                logger.info("数据源模式从 {} 更改为: {}", 
                           oldValue ? "JNDI" : "本地", 
                           useJNDIValue ? "JNDI" : "本地");
            }
        } finally {
            stateLock.unlockWrite(stamp);
        }
    }
    
    /**
     * 获取当前是否使用JNDI
     * 
     * 线程安全的方法，使用乐观读锁提供更好的性能
     */
    public static boolean isUseJNDI() {
        long stamp = stateLock.tryOptimisticRead();
        boolean value = useJNDI.get();
        
        if (!stateLock.validate(stamp)) {
            // 乐观读失败，使用悲观读锁
            stamp = stateLock.readLock();
            try {
                value = useJNDI.get();
            } finally {
                stateLock.unlockRead(stamp);
            }
        }
        
        return value;
    }
    
    /**
     * 获取数据库连接（使用默认数据源）
     */
    public static Connection getConnection() throws SQLException, NamingException {
        return getConnection(PersistenceDefaults.getDataSourceName());
    }
    
    /**
     * 获取数据库连接
     */
    public static Connection getConnection(String dsName) throws SQLException, NamingException {
        dsName = Optional.ofNullable(dsName).orElse(PersistenceDefaults.getDataSourceName());
//        logger.debug("获取数据库连接: {}", dsName);
        DataSource dataSource = getDataSource(dsName);
        Connection connection = dataSource.getConnection();
        logger.debug("数据库连接获取成功: {}", dsName);
        return connection;
    }
    
    /**
     * 获取数据源
     * 
     * 线程安全的方法，使用乐观读锁提供更好的性能
     */
    public static DataSource getDataSource(String name) {
        long stamp = stateLock.tryOptimisticRead();
        boolean useJNDIValue = useJNDI.get();
        DataSource result = null;
        
        if (useJNDIValue) {
            result = getJNDIDataSource(name);
        } else {
            result = getLocalDataSource(name);
        }
        
        if (!stateLock.validate(stamp)) {
            // 乐观读失败，使用悲观读锁重新获取
            stamp = stateLock.readLock();
            try {
                useJNDIValue = useJNDI.get();
                if (useJNDIValue) {
                    result = getJNDIDataSource(name);
                } else {
                    result = getLocalDataSource(name);
                }
            } finally {
                stateLock.unlockRead(stamp);
            }
        }
        
        return result;
    }
    
    /**
     * 获取JNDI数据源
     */
    private static DataSource getJNDIDataSource(String name) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(name);
//            logger.debug("从JNDI获取数据源: {}", name);
            return ds;
        } catch (NamingException e) {
            logger.error("从JNDI获取数据源失败: {}", name, e);
            throw new RuntimeException("Failed to get JNDI data source: " + name, e);
        }
    }
    
    /**
     * 获取本地数据源
     */
    private static DataSource getLocalDataSource(String name) {
        DataSource ds = localDSTable.get(name);
        if (ds == null) {
            logger.warn("本地数据源不存在: {}", name);
            throw new RuntimeException("Local data source not found: " + name);
        }
        logger.debug("从本地获取数据源: {}", name);
        return ds;
    }
    
    /**
     * 添加本地数据源
     * 
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void addLocalDataSource(String name, DataSource dataSource) {
        DataSource oldDataSource = localDSTable.put(name, dataSource);
        if (oldDataSource != null) {
            logger.info("替换本地数据源: {}", name);
        } else {
            logger.info("添加本地数据源: {}", name);
        }
    }
    
    /**
     * 移除本地数据源
     * 
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void removeLocalDataSource(String name) {
        DataSource removed = localDSTable.remove(name);
        if (removed != null) {
            logger.info("移除本地数据源: {}", name);
        } else {
            logger.warn("尝试移除不存在的本地数据源: {}", name);
        }
    }
    
    /**
     * 添加数据源类型映射
     * 
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void addDataSourceFlavor(String dsName, String flavor) {
        String oldFlavor = dsFlavorsTable.put(dsName, flavor);
        if (oldFlavor != null && !oldFlavor.equals(flavor)) {
            logger.debug("更新数据源类型映射: {} -> {} (原值: {})", dsName, flavor, oldFlavor);
        } else {
            logger.debug("添加数据源类型映射: {} -> {}", dsName, flavor);
        }
    }
    
    /**
     * 获取数据源类型
     * 
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static String getDataSourceFlavor(String dsName) {
        String flavor = dsFlavorsTable.get(dsName);
        logger.debug("获取数据源类型: {} -> {}", dsName, flavor);
        return flavor;
    }
    
    /**
     * 检查数据源是否存在
     * 
     * 线程安全的方法，使用乐观读锁提供更好的性能
     */
    public static boolean hasDataSource(String name) {
        long stamp = stateLock.tryOptimisticRead();
        boolean useJNDIValue = useJNDI.get();
        boolean result = false;
        
        if (useJNDIValue) {
            try {
                Context ctx = new InitialContext();
                ctx.lookup(name);
                logger.debug("JNDI数据源存在: {}", name);
                result = true;
            } catch (NamingException e) {
                logger.debug("JNDI数据源不存在: {}", name);
                result = false;
            }
        } else {
            result = localDSTable.containsKey(name);
            logger.debug("本地数据源存在检查: {} = {}", name, result);
        }
        
        if (!stateLock.validate(stamp)) {
            // 乐观读失败，使用悲观读锁重新检查
            stamp = stateLock.readLock();
            try {
                useJNDIValue = useJNDI.get();
                if (useJNDIValue) {
                    try {
                        Context ctx = new InitialContext();
                        ctx.lookup(name);
                        logger.debug("JNDI数据源存在: {}", name);
                        result = true;
                    } catch (NamingException e) {
                        logger.debug("JNDI数据源不存在: {}", name);
                        result = false;
                    }
                } else {
                    result = localDSTable.containsKey(name);
                    logger.debug("本地数据源存在检查: {} = {}", name, result);
                }
            } finally {
                stateLock.unlockRead(stamp);
            }
        }
        
        return result;
    }
    
    /**
     * 获取所有本地数据源名称
     * 
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static String[] getLocalDataSourceNames() {
        String[] names = localDSTable.keySet().toArray(new String[0]);
        logger.debug("获取本地数据源名称列表: {}", String.join(", ", names));
        return names;
    }
    
    /**
     * 清除所有本地数据源
     * 
     * 线程安全的方法，用于测试清理
     */
    public static void clearLocalDataSources() {
        int count = localDSTable.size();
        localDSTable.clear();
        dsFlavorsTable.clear();
        logger.info("清除所有本地数据源，共清除 {} 个数据源", count);
    }
} 