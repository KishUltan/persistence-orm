package com.kishultan.persistence.orm;

import com.kishultan.persistence.orm.SimpleEntityQuery;
import com.kishultan.persistence.orm.query.impl.StandardQueryBuilder;
import com.zaxxer.sansorm.OrmElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 实体管理器 - 抽象层
 * 
 * 提供实体CRUD操作和事务管理的统一接口
 * 线程安全实现，使用ThreadLocal管理事务状态
 * 不依赖具体的实现类，只依赖接口
 * 
 * @author Portal Team
 */
public class EntityManager {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
    
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private final String dataSourceName;
    
    // 使用ThreadLocal确保每个线程有独立的事务上下文
    private static final ThreadLocal<EntityTransaction> currentTransactionHolder = new ThreadLocal<>();
    
    public EntityManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        // 通过接口获取数据源信息，完全依赖抽象
        this.dataSource = entityManagerFactory.getDataSource();
        this.dataSourceName = entityManagerFactory.getDataSourceName();
//        logger.debug("创建EntityManager实例");
    }
    
    /**
     * 创建查询
     */
    public <T> EntityQuery<T> createQuery(Class<T> entityClass) {
        logger.debug("创建查询: {}", entityClass.getSimpleName());
        if (dataSource != null) {
            // 创建QueryBuilder实例并传入SimpleEntityQuery
            com.kishultan.persistence.orm.query.QueryBuilder<T> queryBuilder = createQueryBuilder(entityClass);
            return new SimpleEntityQuery<>(entityClass, queryBuilder);
        }
        throw new UnsupportedOperationException("Queries are only supported with SansOrm");
    }
    
    /**
     * 创建查询构建器 - 新架构支持
     */
    public <T> com.kishultan.persistence.orm.query.QueryBuilder<T> createQueryBuilder(Class<T> entityClass) {
        logger.debug("创建查询构建器: {}", entityClass.getSimpleName());
        if (dataSource != null) {
            // 使用DataSource构造函数创建QueryBuilder
            StandardQueryBuilder<T> queryBuilder =
                new StandardQueryBuilder<>(entityClass, dataSource);
            return queryBuilder;
        }
        throw new UnsupportedOperationException("QueryBuilder is only supported with SansOrm");
    }

    /**
     * 保存实体
     */
    public <T> T save(T entity) {
        logger.debug("保存实体: {}", entity.getClass().getSimpleName());
        return executeWithTransactionOrConnection(
            () -> "保存实体",
            connection -> saveWithConnection(entity, connection),
            () -> saveWithConnection(entity, null)
        );
    }
    
    /**
     * 批量保存实体
     */
    public <T> List<T> saveAll(List<T> entities) {
        logger.debug("批量保存实体，数量: {}", entities.size());
        return executeWithTransactionOrConnection(
            () -> "批量保存实体",
            connection -> saveAllWithConnection(entities, connection),
            () -> saveAllWithConnection(entities, null)
        );
    }
    
    /**
     * 更新实体
     */
    public <T> T update(T entity) {
        logger.debug("更新实体: {}", entity.getClass().getSimpleName());
        return executeWithTransactionOrConnection(
            () -> "更新实体",
            connection -> updateWithConnection(entity, connection),
            () -> updateWithConnection(entity, null)
        );
    }
    
    /**
     * 删除实体
     */
    public <T> void delete(T entity) {
        logger.debug("删除实体: {}", entity.getClass().getSimpleName());
        executeWithTransactionOrConnection(
            () -> "删除实体",
            connection -> {
                deleteWithConnection(entity, connection);
                return null;
            },
            () -> {
                deleteWithConnection(entity, null);
                return null;
            }
        );
    }
    
    /**
     * 根据ID删除实体
     */
    public <T> void deleteById(Class<T> entityClass, Object id) {
        logger.debug("根据ID删除实体: {} - {}", entityClass.getSimpleName(), id);
        executeWithTransactionOrConnection(
            () -> "根据ID删除实体",
            connection -> {
                deleteByIdWithConnection(entityClass, id, connection);
                return null;
            },
            () -> {
                deleteByIdWithConnection(entityClass, id, null);
                return null;
            }
        );
    }
    
    /**
     * 根据ID查找实体
     */
    public <T> T findById(Class<T> entityClass, Object id) {
        logger.debug("根据ID查找实体: {} - {}", entityClass.getSimpleName(), id);
        return findByIdWithConnection(entityClass, id, null);
    }
    
    /**
     * 查找所有实体
     */
    public <T> List<T> findAll(Class<T> entityClass) {
        logger.debug("查找所有实体: {}", entityClass.getSimpleName());
        return findAllWithConnection(entityClass, null);
    }
    
    /**
     * 执行原生SQL查询
     */
    public List<Object> executeSql(String sql, Object... params) {
        logger.debug("执行原生SQL查询: {}", sql);
        return executeSqlWithConnection(sql, null, params);
    }
    
    /**
     * 开始事务
     */
    public EntityTransaction beginTransaction() {
        logger.debug("开始事务");
        EntityTransaction transaction = entityManagerFactory.createTransaction();
        transaction.begin(); // 确保事务开始
        currentTransactionHolder.set(transaction);
        return transaction;
    }
    
    /**
     * 获取当前事务
     */
    public EntityTransaction getCurrentTransaction() {
        return currentTransactionHolder.get();
    }
    
    /**
     * 关闭EntityManager
     */
    public void close() {
        logger.debug("关闭EntityManager");
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction != null && currentTransaction.isActive()) {
            logger.warn("关闭EntityManager时发现活动事务，将回滚");
            currentTransaction.rollback();
        }
        currentTransactionHolder.remove();
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 统一的执行策略：优先使用事务连接，否则使用新连接
     * 
     * @param operationName 操作名称提供者
     * @param withTransaction 使用事务连接的执行逻辑
     * @param withoutTransaction 不使用事务的执行逻辑
     * @return 执行结果
     */
    private <T> T executeWithTransactionOrConnection(
            Supplier<String> operationName,
            Function<Connection, T> withTransaction,
            Supplier<T> withoutTransaction) {
        
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction != null && currentTransaction.isActive()) {
            logger.debug("在事务中执行: {}", operationName.get());
            Connection connection = currentTransaction.getConnection();
            if (connection != null) {
                return withTransaction.apply(connection);
            }
        }
        
        logger.debug("非事务执行: {}", operationName.get());
        return withoutTransaction.get();
    }
    
    private <T> T saveWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                return OrmElf.insertObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.insertObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("保存实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }
    
    private <T> List<T> saveAllWithConnection(List<T> entities, Connection connection) {
        try {
            if (connection != null) {
                for (T entity : entities) {
                    OrmElf.insertObject(connection, entity);
                }
                return entities;
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    for (T entity : entities) {
                        OrmElf.insertObject(conn, entity);
                    }
                    return entities;
                }
            }
        } catch (Exception e) {
            logger.error("批量保存实体失败", e);
            throw new RuntimeException("Failed to save entities", e);
        }
    }
    
    private <T> T updateWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                return OrmElf.updateObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.updateObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("更新实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }
    
    private <T> void deleteWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                OrmElf.deleteObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    OrmElf.deleteObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("删除实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }
    
    private <T> void deleteByIdWithConnection(Class<T> entityClass, Object id, Connection connection) {
        try {
            if (connection != null) {
                OrmElf.deleteObjectById(connection, entityClass, id);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    OrmElf.deleteObjectById(conn, entityClass, id);
                }
            }
        } catch (Exception e) {
            logger.error("根据ID删除实体失败: {} - {}", entityClass.getSimpleName(), id, e);
            throw new RuntimeException("Failed to delete entity by ID", e);
        }
    }
    
    private <T> T findByIdWithConnection(Class<T> entityClass, Object id, Connection connection) {
        try {
            if (connection != null) {
                // 使用SansOrm的正确方法
                return OrmElf.objectById(connection, entityClass, id);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    // 使用SansOrm的正确方法
                    return OrmElf.objectById(conn, entityClass, id);
                }
            }
        } catch (Exception e) {
            logger.error("根据ID查找实体失败: {} - {}", entityClass.getSimpleName(), id, e);
            throw new RuntimeException("Failed to find entity by ID", e);
        }
    }
    
    private <T> List<T> findAllWithConnection(Class<T> entityClass, Connection connection) {
        try {
            if (connection != null) {
                return OrmElf.listFromClause(connection, entityClass, "");
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.listFromClause(conn, entityClass, "");
                }
            }
        } catch (Exception e) {
            logger.error("查找所有实体失败: {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Failed to find all entities", e);
        }
    }
    
    private List<Object> executeSqlWithConnection(String sql, Connection connection, Object... params) {
        try {
            if (connection != null) {
                return OrmElf.listFromClause(connection, Object.class, sql, params);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.listFromClause(conn, Object.class, sql, params);
                }
            }
        } catch (Exception e) {
            logger.error("执行SQL失败: {}", sql, e);
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    /**
     * 回滚当前事务
     */
    public void rollbackTransaction() {
        executeTransactionOperation("回滚事务", EntityTransaction::rollback);
    }

    /**
     * 提交当前事务
     */
    public void commitTransaction() {
        executeTransactionOperation("提交事务", EntityTransaction::commit);
    }

    /**
     * 检查事务是否处于活动状态
     */
    public boolean isTransactionActive() {
        EntityTransaction currentTransaction = getCurrentTransaction();
        return currentTransaction != null && currentTransaction.isActive();
    }

    /**
     * 关闭当前事务
     */
    public void closeTransaction() {
        executeTransactionOperation("关闭事务", transaction -> {
            if (transaction.isActive()) {
                logger.warn("关闭活动事务，自动回滚");
                transaction.rollback();
            }
        });
    }

    /**
     * 清除当前事务
     * 线程安全的方法，使用ThreadLocal管理事务状态
     */
    private void clearCurrentTransaction() {
        currentTransactionHolder.remove();
    }
    
    /**
     * 统一的事务操作执行器
     * 
     * @param operationName 操作名称
     * @param operation 事务操作
     */
    private void executeTransactionOperation(String operationName, java.util.function.Consumer<EntityTransaction> operation) {
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null || !currentTransaction.isActive()) {
            logger.warn("尝试{}不存在或已结束的事务", operationName);
            return;
        }

        logger.info(operationName);
        operation.accept(currentTransaction);
        clearCurrentTransaction();
    }
}