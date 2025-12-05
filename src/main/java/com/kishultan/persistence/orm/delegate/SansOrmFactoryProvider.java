package com.kishultan.persistence.orm.delegate;

import com.kishultan.persistence.orm.EntityManagerFactory;
import com.kishultan.persistence.orm.EntityManagerFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * SansOrm工厂提供者实现
 * 
 * 实现EntityManagerFactoryProvider接口，用于解耦orm包和delegate包
 * 
 * 使用方式：
 * 1. 在应用启动时调用 PersistenceManager.setFactoryProvider(new SansOrmFactoryProvider())
 * 2. 或者使用SPI机制（见方案2）
 * 
 * @author Portal Team
 */
public class SansOrmFactoryProvider implements EntityManagerFactoryProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SansOrmFactoryProvider.class);
    
    @Override
    public EntityManagerFactory createFactory(DataSource dataSource) {
        return new SansOrmEntityManagerFactory(dataSource);
    }
    
    @Override
    public EntityManagerFactory createFactory(DataSource dataSource, String dataSourceName) {
        return new SansOrmEntityManagerFactory(dataSource, dataSourceName);
    }
}

