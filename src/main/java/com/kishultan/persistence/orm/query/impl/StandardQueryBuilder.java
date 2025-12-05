package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.query.monitor.QueryPerformanceMonitor;
import com.kishultan.persistence.orm.query.monitor.QueryMetrics;
import com.kishultan.persistence.orm.query.cache.QueryCache;
import com.kishultan.persistence.orm.query.config.QueryBuilderConfigManager;
import com.kishultan.persistence.orm.ColumnabledLambda;
import com.kishultan.persistence.orm.query.context.*;
import com.kishultan.persistence.orm.query.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.sql.Connection;
import javax.sql.DataSource;

/**
 * 查询构建器实现类
 * 使用新的架构：存储子句对象，通过 buildClause() 方法生成SQL
 */
public class StandardQueryBuilder<T> implements QueryBuilder<T> {


    private static final Logger logger = LoggerFactory.getLogger(StandardQueryBuilder.class);
    
    private final Class<T> entityClass;
    private final TableAliasRegistry aliasRegistry = new TableAliasRegistry();
    private final SqlBuildContext<T> buildContext = new SqlBuildContext<>();
    private final DefaultRowMapper defaultMapper = new DefaultRowMapper();
    private RowMapper customRowMapper = null;
    private Class<?> customResultType;
    
    // 存储各个子句对象
    private SelectClause<T> selectClause;
    private AggregateClause<T> aggregateClause;
    private WindowClause<T> windowClause;
    private ExpressionClause<T> expressionClause;
    private CaseWhenClause<T> caseWhenClause;
    private FromClause<T> fromClause;
    private List<JoinClause<T>> joinClauses = new ArrayList<>();
    private WhereClause<T> whereClause;
    private GroupClause<T> groupClause;
    private HavingClause<T> havingClause;
    private OrderClause<T> orderClause;
    
    // 子查询引用
    private StandardQueryBuilder<?> subquery;
    
    // 分页参数
    private int offsetValue = 0;
    private int limitValue = 0;
    
    // 执行器
    private SqlExecutor sqlExecutor;
    
    // 数据源引用
    private DataSource dataSource;
    
    // 性能监控和缓存（通过配置管理器获取）
    private QueryPerformanceMonitor performanceMonitor;
    private QueryCache queryCache;
    private boolean performanceMonitoringEnabled = false;
    private boolean cacheEnabled = false;
    
    // ==================== 构造函数 ====================
    
    public StandardQueryBuilder(Class<T> entityClass, DataSource dataSource) {
        this.entityClass = entityClass;
        this.dataSource = dataSource;
        this.sqlExecutor = new SimpleSqlExecutor(dataSource);
        
        // 注册主表到ResultSetMapper
        String tableName = EntityUtils.getTableName(entityClass);
        defaultMapper.register(entityClass, tableName);
    }
    
    /**
     * 设置数据源
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 设置SQL执行器
     */
    public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    // ==================== 别名注册表管理 ====================
    
    public TableAliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }
    
    public void registerTable(Class<?> entityClass, String tableName, String alias) {
        aliasRegistry.registerTable(tableName, alias);
        defaultMapper.register(entityClass, alias);
    }
    
    public String getTableAlias(String tableName) {
        return aliasRegistry.getAlias(tableName);
    }
    
    public DefaultRowMapper getResultSetMapper() {
        return defaultMapper;
    }
    
    public SqlBuildContext<T> getBuildContext() {
        return buildContext;
    }
    
    // ==================== 智能展开辅助方法 ====================
    
    /**
     * 检查是否有JOIN子句
     */
    public boolean hasJoinClause() {
        return !joinClauses.isEmpty();
    }
    
    /**
     * 获取所有相关表的字段（带表别名和字段别名）
     * 包括主表和所有JOIN表的字段，避免歧义和重复展开
     * 字段别名规则：表别名__列名
     */
    public String[] getAllTableFields() {
        List<String> allFields = new ArrayList<>();
        Set<Class<?>> processedEntityClasses = new HashSet<>();  // 记录已处理的实体类
        
        // 1. 添加主表字段
        String mainTableAlias = getCurrentTableAlias();
        if (mainTableAlias == null) {
            mainTableAlias = EntityUtils.getTableName(entityClass);
        }
        
        String[] mainTableFields = EntityUtils.getColumnNames(entityClass);
        for (String field : mainTableFields) {
            // 🔧 添加字段别名：表别名__列名
            String fieldWithAlias = mainTableAlias + "." + field + " AS " + mainTableAlias + "__" + field;
            allFields.add(fieldWithAlias);
        }
        processedEntityClasses.add(entityClass);  // 标记主表已处理
        
        // 2. 添加所有JOIN表的字段（避免重复展开）
        for (JoinClause<T> joinClause : joinClauses) {
            if (joinClause instanceof JoinClauseImpl) {
                JoinClauseImpl<T> joinImpl = (JoinClauseImpl<T>) joinClause;
                String joinTableAlias = joinImpl.getCurrentTableAlias();
                Class<?> joinEntityClass = joinImpl.getJoinEntityClass();
                
                // 🔧 检查实体类是否已经展开过，避免重复
                if (joinTableAlias != null && joinEntityClass != null && 
                    !processedEntityClasses.contains(joinEntityClass)) {
                    
                    String[] joinTableFields = EntityUtils.getColumnNames(joinEntityClass);
                    for (String field : joinTableFields) {
                        // 🔧 添加字段别名：表别名__列名
                        String fieldWithAlias = joinTableAlias + "." + field + " AS " + joinTableAlias + "__" + field;
                        allFields.add(fieldWithAlias);
                    }
                    processedEntityClasses.add(joinEntityClass);  // 标记已处理
                }
            }
        }
        
        return allFields.toArray(new String[0]);
    }
    
    // ==================== 主查询构建 ====================
    
    @Override
    public SelectClause<T> select() {
        this.selectClause = new SelectClauseImpl<>(this);
        return this.selectClause;
    }
    
    @Override
    public SelectClause<T> select(String... columns) {
        this.selectClause = new SelectClauseImpl<T>(this);
        // 将选择的字段传递给SelectClauseImpl
        if (columns != null && columns.length > 0) {
            ((SelectClauseImpl<T>) this.selectClause).setSelectedFields(columns);
        }
        return this.selectClause;
    }
    
    @Override
    @SafeVarargs
    public final SelectClause<T> select(Columnable<T, ?>... fields) {
        this.selectClause = new SelectClauseImpl<T>(this);
        // 将选择的字段传递给SelectClauseImpl
        if (fields != null && fields.length > 0) {
            String[] fieldNames = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isField()){
                    fieldNames[i] = fields[i].columnName();
                }else {
                    fieldNames[i] = fields[i].toSql(); //支持聚合、窗口、表达式、标量
                }
            }
            ((SelectClauseImpl<T>) this.selectClause).setSelectedFields(fieldNames);
        }

        //作为标量查询时，置空防止重复生成SQL
        this.aggregateClause = null;
        this.windowClause = null;
        this.expressionClause = null;
        this.caseWhenClause = null;

        return this.selectClause;
    }
    
    @Override
    public SelectClause<T> selectAll() {
        this.selectClause = new SelectClauseImpl<T>(this, true);
        return this.selectClause;
    }
    
    // ==================== 聚合函数构建 ====================
    
    @Override
    public AggregateClause<T> aggregate() {
        this.aggregateClause = new AggregateClauseImpl<>(this);
        return this.aggregateClause;
    }
    
    @Override
    public WindowClause<T> window() {
        this.windowClause = new WindowClauseImpl<>(this);
        return this.windowClause;
    }
    
    @Override
    public ExpressionClause<T> expression() {
        this.expressionClause = new ExpressionClauseImpl<>(this);
        return this.expressionClause;
    }
    
    @Override
    public CaseWhenClause<T> caseWhen() {
        this.caseWhenClause = new CaseWhenClauseImpl<>(this);
        return this.caseWhenClause;
    }
    
    @Override
    public CaseWhenClause<T> caseWhen(Columnable<T, ?> field) {
        this.caseWhenClause = new CaseWhenClauseImpl<>(this, field, null);
        return this.caseWhenClause;
    }
    
    @Override
    public CaseWhenClause<T> caseWhen(Columnable<T, ?> field, String alias) {
        this.caseWhenClause = new CaseWhenClauseImpl<>(this, field, alias);
        return this.caseWhenClause;
    }
    
    @Override
    public CaseWhenClause<T> caseWhen(String alias) {
        this.caseWhenClause = new CaseWhenClauseImpl<>(this, alias);
        return this.caseWhenClause;
    }
    
    // ==================== 子查询构建 ====================
    
    @Override
    public QueryBuilder<T> subquery() {
        StandardQueryBuilder<T> subquery = new StandardQueryBuilder<>(entityClass, dataSource);
        return subquery;
    }
    
    // ==================== 执行方法 ====================
    
    @Override
    public List<T> findList() {
        if (sqlExecutor == null) {
            throw new IllegalStateException("SQL执行器未设置");
        }
        
        // 如果启用了缓存，先尝试从缓存获取
        if (QueryBuilderConfigManager.isCacheEnabled()) {
            QueryCache cache = getQueryCache();
            if (cache != null) {
                String cacheKey = generateCacheKey("findList");
                @SuppressWarnings("unchecked")
                List<T> cachedResult = cache.get(cacheKey, List.class);
                if (cachedResult != null) {
                    logger.debug("从缓存获取查询结果: cacheKey={}", cacheKey);
                    return cachedResult;
                }
            }
        }
        
        // 开始性能监控
        String contextId = startPerformanceMonitoring();
        
        try {
            QueryResult queryResult = buildQuery();
//            System.out.println("-------------------------------------");
//            System.out.println("findList->SQL : "+queryResult.getSql());
//            System.out.println("findList->parameters: " + buildContext.getParameters());
//            System.out.println("-------------------------------------");
            
            // 只判断customRowMapper并赋值
            List<T> result;
            if (customRowMapper != null) {
                // 使用自定义RowMapper
                @SuppressWarnings("unchecked")
                RowMapper typedRowMapper =  customRowMapper;
                @SuppressWarnings("unchecked")
                Class<T> typedResultType = (Class<T>) customResultType;
                result = sqlExecutor.executeQuery(queryResult.getSql(), queryResult.getParameters(), typedResultType, typedRowMapper);
            } else {
                // 使用默认的ResultSetMapper
                @SuppressWarnings("unchecked")
                DefaultRowMapper typedDefaultMapper = defaultMapper;
                result = sqlExecutor.executeQuery(queryResult.getSql(), queryResult.getParameters(), entityClass, typedDefaultMapper);
            }
            
            // 结束性能监控
            endPerformanceMonitoring(contextId, true, result != null ? result.size() : 0);
            
            // 如果启用了缓存，存储结果到缓存
            if (QueryBuilderConfigManager.isCacheEnabled() && result != null && !result.isEmpty()) {
                QueryCache cache = getQueryCache();
                if (cache != null) {
                    String cacheKey = generateCacheKey("findList");
                    cache.put(cacheKey, result, 300000); // 5分钟TTL
                    logger.debug("查询结果已缓存: cacheKey={}, resultSize={}", cacheKey, result.size());
                }
            }
            
            return result;
        } catch (Exception e) {
            // 记录性能监控错误
            recordPerformanceError(contextId, e);
            throw e;
        }
    }
    
    @Override
    public T findFirst() {
        List<T> list = findList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    @Override
    public long count() {
        if (sqlExecutor == null) {
            throw new IllegalStateException("SQL执行器未设置，请先设置数据源或SQL执行器");
        }
        
        // 如果启用了缓存，先尝试从缓存获取
        if (QueryBuilderConfigManager.isCacheEnabled()) {
            QueryCache cache = getQueryCache();
            if (cache != null) {
                String cacheKey = generateCacheKey("count");
                Long cachedResult = cache.get(cacheKey, Long.class);
                if (cachedResult != null) {
                    logger.debug("从缓存获取计数结果: cacheKey={}, count={}", cacheKey, cachedResult);
                    return cachedResult;
                }
            }
        }
        
        // 开始性能监控
        String contextId = startPerformanceMonitoring();
        
        try {
            QueryResult queryResult = buildQuery();
//            System.out.println("-------------------------------------");
//            System.out.println("count->SQL : "+queryResult.getCountSql());
//            System.out.println("count->parameters: " + buildContext.getParameters());
//            System.out.println("-------------------------------------");
            
            long result = sqlExecutor.executeAsLong(queryResult.getCountSql(), queryResult.getParameters());
            
            // 结束性能监控
            endPerformanceMonitoring(contextId, true, 1); // count查询结果数量为1
            
            // 如果启用了缓存，存储结果到缓存
            if (QueryBuilderConfigManager.isCacheEnabled()) {
                QueryCache cache = getQueryCache();
                if (cache != null) {
                    String cacheKey = generateCacheKey("count");
                    cache.put(cacheKey, result, 60000); // 1分钟TTL
                    logger.debug("计数结果已缓存: cacheKey={}, count={}", cacheKey, result);
                }
            }
            
            return result;
        } catch (Exception e) {
            // 记录性能监控错误
            recordPerformanceError(contextId, e);
            throw e;
        }
    }
    
    // 分页查询方法（不在接口中，但提供便利方法）
    public PaginationSupport.PaginatedResult<T> findPage(int page, int size) {
        this.offsetValue = (page - 1) * size;
        this.limitValue = size;
        
        long total = count();
        List<T> list = findList();
        
        return new PaginatedResultImpl<>(list, total, page, size);
    }
    
    // ==================== 实现接口要求的方法 ====================
    
    @Override
    public String getGeneratedSql() {
        QueryResult queryResult = buildQuery();
        return queryResult.getSql();
    }
    
    @Override
    public boolean isSubquery() {
        // TODO: 实现子查询检测逻辑
        return false;
    }
    
    @Override
    public String getSubquerySql() {
        return isSubquery() ? getGeneratedSql() : "";
    }
    
    // ==================== 新架构方法 ====================
    
    /**
     * 构建查询结果
     * 调用各个子句的 buildClause() 方法，组装完整的SQL
     */
    public QueryResult buildQuery() {
        // 清空构建上下文
        buildContext.clear();
        
        // 🔧 自动初始化必要的子句，确保无条件查询也能正常工作
        if (selectClause == null && aggregateClause == null && windowClause == null && expressionClause == null && caseWhenClause == null) {
            this.selectClause = new SelectClauseImpl<>(this);
        }
        if (fromClause == null) {
            // 使用实体类和表名初始化FromClause
            String tableName = EntityUtils.getTableName(entityClass);
            this.fromClause = new FromClauseImpl<>(this, entityClass, tableName, tableName);
        }
        
        // 如果有子查询，先收集子查询的参数
        if (subquery != null) {
            //System.out.println("-------构建子查询开始-------");
            QueryResult subQueryResult = subquery.buildQuery();
            buildContext.addParameters(subQueryResult.getParameters());
            //System.out.println("-------构建子查询结束-------");
        }
        
        // 构建各个子句
        if (selectClause instanceof ClauseBuilder) {
            ClauseResult selectResult = ((ClauseBuilder<T>) selectClause).buildClause();
            buildContext.setSelectClause(selectResult.getSql());
            buildContext.addParameters(selectResult.getParameters());
        }
        
        if (aggregateClause instanceof ClauseBuilder) {
            ClauseResult aggregateResult = ((ClauseBuilder<T>) aggregateClause).buildClause();
            buildContext.setSelectClause(aggregateResult.getSql());
            buildContext.addParameters(aggregateResult.getParameters());
        }
        
        if (windowClause instanceof ClauseBuilder) {
            ClauseResult windowResult = ((ClauseBuilder<T>) windowClause).buildClause();
            buildContext.setSelectClause(windowResult.getSql());
            buildContext.addParameters(windowResult.getParameters());
        }
        
        if (expressionClause instanceof ClauseBuilder) {
            ClauseResult expressionResult = ((ClauseBuilder<T>) expressionClause).buildClause();
            buildContext.setSelectClause(expressionResult.getSql());
            buildContext.addParameters(expressionResult.getParameters());
        }
        
        if (caseWhenClause instanceof ClauseBuilder) {
            ClauseResult caseWhenResult = ((ClauseBuilder<T>) caseWhenClause).buildClause();
            buildContext.setSelectClause(caseWhenResult.getSql());
            buildContext.addParameters(caseWhenResult.getParameters());
        }
        
        if (fromClause instanceof ClauseBuilder) {
            ClauseResult fromResult = ((ClauseBuilder<T>) fromClause).buildClause();
            buildContext.setFromClause(fromResult.getSql());
            buildContext.addParameters(fromResult.getParameters());
        }
        
        // 处理多个JOIN
        StringBuilder joinSql = new StringBuilder();
        for (JoinClause<T> joinClause : joinClauses) {
            if (joinClause instanceof ClauseBuilder) {
                ClauseResult joinResult = ((ClauseBuilder<T>) joinClause).buildClause();
                if (!joinResult.getSql().isEmpty()) {
                    if (joinSql.length() > 0) {
                        joinSql.append(" ");
                    }
                    joinSql.append(joinResult.getSql());
                }
                buildContext.addParameters(joinResult.getParameters());
            }
        }
        
        // 将JOIN SQL添加到构建上下文
        if (joinSql.length() > 0) {
            buildContext.setJoinClause(joinSql.toString());
        }
        
        if (whereClause instanceof ClauseBuilder) {
            ClauseResult whereResult = ((ClauseBuilder<T>) whereClause).buildClause();
            buildContext.setWhereClause(whereResult.getSql());
            buildContext.addParameters(whereResult.getParameters());
        }
        
        if (groupClause instanceof ClauseBuilder) {
            ClauseResult groupResult = ((ClauseBuilder<T>) groupClause).buildClause();
            buildContext.setGroupByClause(groupResult.getSql());
            buildContext.addParameters(groupResult.getParameters());
        }
        
        if (havingClause instanceof ClauseBuilder) {
            ClauseResult havingResult = ((ClauseBuilder<T>) havingClause).buildClause();
            buildContext.setHavingClause(havingResult.getSql());
            buildContext.addParameters(havingResult.getParameters());
        }
        
        // 🔧 添加ORDER BY调试信息
//        System.out.println("=== ORDER BY 调试信息 ===");
//        System.out.println("orderClause: " + orderClause);
//        System.out.println("orderClause instanceof ClauseBuilder: " + (orderClause instanceof ClauseBuilder));
//        if (orderClause != null) {
//            System.out.println("orderClause.getClass(): " + orderClause.getClass().getName());
//        }
        
        if (orderClause instanceof ClauseBuilder) {
            ClauseResult orderResult = ((ClauseBuilder<T>) orderClause).buildClause();
//            System.out.println("orderResult.getSql(): " + orderResult.getSql());
            buildContext.setOrderByClause(orderResult.getSql());
            buildContext.addParameters(orderResult.getParameters());
        }
//        System.out.println("================================");
        
        // 处理LIMIT子句
        if (limitValue > 0) {
            String limitSql = "LIMIT " + limitValue;
            if (offsetValue > 0) {
                limitSql += " OFFSET " + offsetValue;
            }
            buildContext.setLimitClause(limitSql);
        }
        
        // 组装最终结果
        StringBuilder sql = new StringBuilder();
        if (!buildContext.getSelectClause().isEmpty()) {
            sql.append(buildContext.getSelectClause()).append(" ");
        }
        if (!buildContext.getFromClause().isEmpty()) {
            sql.append(buildContext.getFromClause()).append(" ");
        }
        if (!buildContext.getJoinClause().isEmpty()) {
            sql.append(buildContext.getJoinClause()).append(" ");
        }
        if (!buildContext.getWhereClause().isEmpty()) {
            sql.append(buildContext.getWhereClause()).append(" ");
        }
        if (!buildContext.getGroupByClause().isEmpty()) {
            sql.append(buildContext.getGroupByClause()).append(" ");
        }
        if (!buildContext.getHavingClause().isEmpty()) {
            sql.append(buildContext.getHavingClause()).append(" ");
        }
        if (!buildContext.getOrderByClause().isEmpty()) {
            sql.append(buildContext.getOrderByClause()).append(" ");
        }
        if (!buildContext.getLimitClause().isEmpty()) {
            sql.append(buildContext.getLimitClause()).append(" ");
        }

        // COUNT查询不包含ORDER BY、GROUP BY、HAVING和LIMIT
        StringBuilder countSql = new StringBuilder();
        // 计数查询应该使用 SELECT COUNT(*)
        countSql.append("SELECT COUNT(*) ");
        if (!buildContext.getFromClause().isEmpty()) {
            countSql.append(buildContext.getFromClause()).append(" ");
        }
        if (!buildContext.getJoinClause().isEmpty()) {
            countSql.append(buildContext.getJoinClause()).append(" ");
        }
        if (!buildContext.getWhereClause().isEmpty()) {
            countSql.append(buildContext.getWhereClause()).append(" ");
        }
        if (!buildContext.getGroupByClause().isEmpty()) {
            countSql.append(buildContext.getGroupByClause()).append(" ");
        }
        if (!buildContext.getHavingClause().isEmpty()) {
            countSql.append(buildContext.getHavingClause()).append(" ");
        }
        
        /*// 添加调试信息
        System.out.println("=== 参数调试信息 ===");
        System.out.println("主查询参数数量: " + buildContext.getParameters().size());
        System.out.println("主查询参数: " + buildContext.getParameters());
        System.out.println("计数SQL: " + countSql.toString());
        System.out.println("================================");*/
        
        // 打印完整的SQL语句用于调试
        String finalSql = sql.toString().trim();
        String finalCountSql = countSql.toString().trim();
        
        /*System.out.println("=== QueryBuilder 生成的SQL ===");
        System.out.println("主查询SQL: " + finalSql);
        System.out.println("计数SQL: " + finalCountSql);
        System.out.println("参数: " + buildContext.getParameters());
        System.out.println("================================");*/
        logger.debug("=== QueryBuilder 生成的SQL ===");
        logger.debug("查询SQL：{}",finalSql);
        logger.debug("计数SQL：{}",finalCountSql);
        logger.debug("参数：{}",buildContext.getParameters());
        logger.debug("================================");

        return new QueryResult(finalSql, finalCountSql, buildContext.getParameters());
    }
    
    // ==================== 内部方法 ====================
    
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
    public int getOffsetValue() {
        return offsetValue;
    }
    
    public int getLimitValue() {
        return limitValue;
    }
    
    public boolean hasPagination() {
        return limitValue > 0;
    }
    
    // ==================== 必要的方法 ====================
    
    @Override
    public QueryBuilder<T> limit(int offset, int size) {
        this.offsetValue = offset;
        this.limitValue = size;
        return this;
    }
    
    // ==================== 子句设置方法 ====================
    
    void setFromClause(FromClause<T> fromClause) {
        this.fromClause = fromClause;
    }
    
    void addJoinClause(JoinClause<T> joinClause) {
        this.joinClauses.add(joinClause);
    }
    
    void setWhereClause(WhereClause<T> whereClause) {
        this.whereClause = whereClause;
    }
    
    /**
     * 条件构建器模式 - 支持 Consumer 的 where 方法
     * 允许在 QueryBuilder 构建完成后，通过 Consumer 动态添加 where 条件
     */
    @Override
    public QueryBuilder<T> where(Consumer<WhereClause<T>> whereBuilder) {
        if (whereBuilder != null) {
            // 如果还没有 WhereClause，创建一个
            if (this.whereClause == null) {
                this.whereClause = new WhereClauseImpl<>(this);
            }
            // 使用 Consumer 构建 where 条件
            whereBuilder.accept(this.whereClause);
        }
        return this;
    }
    
    void setGroupClause(GroupClause<T> groupClause) {
        this.groupClause = groupClause;
    }
    
    void setHavingClause(HavingClause<T> havingClause) {
        this.havingClause = havingClause;
    }
    
    public void setOrderClause(OrderClause<T> orderClause) {
        this.orderClause = orderClause;
    }
    
    @Override
    public OrderClause<T> createOrderClause() {
        OrderClause<T> orderClause = new OrderClauseImpl<>(this);
        setOrderClause(orderClause);
        return orderClause;
    }
    
    void setSubquery(StandardQueryBuilder<?> subquery) {
        this.subquery = subquery;
    }
    
    // ==================== 子查询字段引用 ====================
    
    @Override
    public String selfField(Columnable<T, ?> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String currentTableAlias = getCurrentTableAlias();
        return currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
    }
    
    @Override
    public String subqueryField(Columnable<T, ?> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        return "subquery." + fieldName;
    }
    
    public String getCurrentTableAlias() {
        // 主表的别名就是表名
        if (entityClass != null) {
            return EntityUtils.getTableName(entityClass);
        }
        return null;
    }
    
    /**
     * 尝试从当前线程获取数据库连接
     */
    private Connection getCurrentConnection() {
        try {
            // 如果有数据源，从数据源获取连接
            if (dataSource != null) {
                return dataSource.getConnection();
            }
            // 如果没有数据源，返回null
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== 性能监控支持 ====================
    
    @Override
    public QueryMetrics getPerformanceMetrics() {
        QueryPerformanceMonitor monitor = getPerformanceMonitor();
        return monitor != null ? monitor.getMetrics() : null;
    }
    
    @Override
    public QueryPerformanceMonitor getPerformanceMonitor() {
        if (performanceMonitor == null) {
            performanceMonitor = QueryBuilderConfigManager.getPerformanceMonitor();
            performanceMonitoringEnabled = QueryBuilderConfigManager.isPerformanceMonitoringEnabled();
        }
        return performanceMonitor;
    }
    
    // ==================== 缓存支持 ====================
    
    @Override
    public QueryCache getQueryCache() {
        if (queryCache == null) {
            queryCache = QueryBuilderConfigManager.getQueryCache();
            cacheEnabled = QueryBuilderConfigManager.isCacheEnabled();
        }
        return queryCache;
    }
    
    @Override
    public QueryBuilder setRowMapper(RowMapper rowMapper) {
        this.customRowMapper = rowMapper;
        //this.customResultType = getRowMapperResultType(rowMapper);
        return this;
    }
    
    @Override
    public RowMapper<?> getRowMapper() {
        return customRowMapper;
    }
    
    
    // ==================== 辅助方法 ====================
    
    
    /**
     * 生成缓存键
     * 
     * @param operation 操作类型
     * @return 缓存键
     */
    private String generateCacheKey(String operation) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("querybuilder:").append(entityClass.getSimpleName().toLowerCase());
        keyBuilder.append(":").append(operation);
        keyBuilder.append(":").append(getGeneratedSql().hashCode());
        
        // 添加参数哈希
        if (buildContext.getParameters() != null && !buildContext.getParameters().isEmpty()) {
            keyBuilder.append(":").append(buildContext.getParameters().hashCode());
        }
        
        return keyBuilder.toString();
    }

    /**
     * 开始性能监控
     * 
     * @return 监控上下文ID
     */
    private String startPerformanceMonitoring() {
        if (!QueryBuilderConfigManager.isPerformanceMonitoringEnabled()) {
            return null;
        }
        
        try {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                String sql = getGeneratedSql();
                Object[] parameters = buildContext.getParameters().toArray();
                return monitor.startMonitoring(sql, parameters);
            }
        } catch (Exception e) {
            logger.warn("开始性能监控失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 结束性能监控
     * 
     * @param contextId 监控上下文ID
     * @param success 是否成功
     * @param resultCount 结果数量
     */
    private void endPerformanceMonitoring(String contextId, boolean success, int resultCount) {
        if (contextId != null) {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                monitor.endMonitoring(contextId, success, resultCount);
            }
        }
    }
    
    /**
     * 记录性能监控错误
     * 
     * @param contextId 监控上下文ID
     * @param error 错误
     */
    private void recordPerformanceError(String contextId, Throwable error) {
        if (contextId != null) {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                monitor.recordError(contextId, error);
            }
        }
    }
}
