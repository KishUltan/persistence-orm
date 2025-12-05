package com.kishultan.persistence.orm.delegate;

import com.kishultan.persistence.orm.EntityManagerFactory;
import com.kishultan.persistence.orm.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * SansOrm实体管理器工厂实现
 * 
 * 只负责创建EntityManager实例，不包含CRUD操作
 * 
 * @author Portal Team
 */
public class SansOrmEntityManagerFactory implements EntityManagerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(SansOrmEntityManagerFactory.class);
    
    private final DataSource dataSource;
    private final String dataSourceName;
    
    public SansOrmEntityManagerFactory(DataSource dataSource) {
        this(dataSource, com.kishultan.persistence.config.PersistenceDefaults.getDataSourceName());
    }
    
    public SansOrmEntityManagerFactory(DataSource dataSource, String dataSourceName) {
        this.dataSource = dataSource;
        this.dataSourceName = dataSourceName;
        logger.debug("创建SansOrmEntityManagerFactory实例，数据源: {}", dataSourceName);
    }
    
    /*@Override
    public com.kishultan.persistence.orm.EntityManager createEntityManager() {
        logger.debug("创建EntityManager实例");
        // 使用反射避免循环依赖
        try {
            Class<?> entityManagerClass = Class.forName("com.kishultan.persistence.orm.EntityManager");
            return (com.kishultan.persistence.orm.EntityManager) entityManagerClass.getConstructor(EntityManagerFactory.class).newInstance(this);
        } catch (Exception e) {
            logger.error("创建EntityManager失败", e);
            throw new RuntimeException("Failed to create EntityManager", e);
        }
    }
    
    @Override
    public com.kishultan.persistence.orm.EntityManager createEntityManager(EntityTransaction transaction) {
        logger.debug("创建支持事务的EntityManager实例");
        com.kishultan.persistence.orm.EntityManager entityManager = createEntityManager();
        return entityManager;
    }*/
    
    @Override
    public boolean isLambdaSupported() {
        return true; // SansOrm支持Lambda表达式
    }
    
    @Override
    public boolean isAdvancedQuerySupported() {
        return true; // SansOrm支持高级查询
    }
    
    @Override
    public javax.sql.DataSource getDataSource() {
        return dataSource;
    }
    
    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    @Override
    public EntityTransaction createTransaction() {
        logger.debug("创建EntityTransaction实例");
        return new SansOrmEntityTransaction(dataSource);
    }
    
    @Override
    public void shutdown() {
        logger.info("关闭SansOrmEntityManagerFactory");
        // SansOrm不需要显式关闭
    }
}
