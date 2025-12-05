package com.kishultan.persistence.orm;

import javax.sql.DataSource;

/**
 * EntityManagerFactory提供者接口
 * 
 * 用于解耦orm包和delegate包，避免循环依赖
 * delegate包实现此接口并注册到PersistenceManager
 * 
 * @author Portal Team
 */
public interface EntityManagerFactoryProvider {
    
    /**
     * 创建EntityManagerFactory实例
     * 
     * @param dataSource 数据源
     * @return EntityManagerFactory实例
     */
    EntityManagerFactory createFactory(DataSource dataSource);
    
    /**
     * 创建EntityManagerFactory实例（指定数据源名称）
     * 
     * @param dataSource 数据源
     * @param dataSourceName 数据源名称
     * @return EntityManagerFactory实例
     */
    EntityManagerFactory createFactory(DataSource dataSource, String dataSourceName);
}

