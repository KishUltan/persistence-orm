package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.ColumnabledLambda;
import com.kishultan.persistence.orm.query.ClauseBuilder;
import com.kishultan.persistence.orm.query.context.ClauseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 窗口函数子句实现类
 */
public class WindowClauseImpl<T> extends SelectClauseImpl<T> implements WindowClause<T>, ClauseBuilder<T> {
    
    // 窗口函数字段
    private final List<String> windowFunctions = new ArrayList<>();
    
    public WindowClauseImpl(QueryBuilder<T> queryBuilder) {
        super(queryBuilder);
    }
    
    // ==================== 窗口函数实现 ====================
    
    @Override
    public WindowClause<T> rowNumber(String alias) {
        windowFunctions.add("ROW_NUMBER() AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> rowNumber(List<Columnable<T, ?>> partitionBy,
                                     List<Columnable<T, ?>> orderBy,
                                     String alias) {
        String overClause = buildOverClause(partitionBy, orderBy);
        windowFunctions.add("ROW_NUMBER() OVER (" + overClause + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> rank(String alias) {
        windowFunctions.add("RANK() AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> rank(List<Columnable<T, ?>> partitionBy,
                                List<Columnable<T, ?>> orderBy,
                                String alias) {
        String overClause = buildOverClause(partitionBy, orderBy);
        windowFunctions.add("RANK() OVER (" + overClause + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> denseRank(String alias) {
        windowFunctions.add("DENSE_RANK() AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> denseRank(List<Columnable<T, ?>> partitionBy,
                                     List<Columnable<T, ?>> orderBy,
                                     String alias) {
        String overClause = buildOverClause(partitionBy, orderBy);
        windowFunctions.add("DENSE_RANK() OVER (" + overClause + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> lag(Columnable<T, ?> field, int offset, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        windowFunctions.add("LAG(" + qualifiedField + ", " + offset + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> lead(Columnable<T, ?> field, int offset, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        windowFunctions.add("LEAD(" + qualifiedField + ", " + offset + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> ntile(int buckets, String alias) {
        windowFunctions.add("NTILE(" + buckets + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> percentRank(String alias) {
        windowFunctions.add("PERCENT_RANK() AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> cumeDist(String alias) {
        windowFunctions.add("CUME_DIST() AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> firstValue(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        windowFunctions.add("FIRST_VALUE(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> lastValue(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        windowFunctions.add("LAST_VALUE(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public WindowClause<T> nthValue(Columnable<T, ?> field, int n, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        windowFunctions.add("NTH_VALUE(" + qualifiedField + ", " + n + ") AS " + alias);
        return this;
    }
    
    // ==================== FROM子句 ====================
    
    /*@Override
    public FromClause<T> from() {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(Class<T> entityClass) {
        String tableName = EntityUtils.getTableName(entityClass);
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, entityClass, tableName, tableName);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(String tableName) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, tableName, tableName);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(String tableName, String alias) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, tableName, alias);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }*/
    
    // ==================== 构建子句 ====================
    
    @Override
    public ClauseResult buildClause() {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        if (windowFunctions.isEmpty()) {
            sql.append("*");
        } else {
            for (int i = 0; i < windowFunctions.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(windowFunctions.get(i));
            }
        }
        
        return new ClauseResult(sql.toString(), parameters);
    }
    
    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    @Override
    public String toFunctionSql() {
        if (windowFunctions.isEmpty()) {
            return "ROW_NUMBER()";
        }
        return String.join(", ", windowFunctions);
    }
    
    // ==================== 辅助方法 ====================
    
    private String getCurrentTableAlias() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            StandardQueryBuilder<T> qb = (StandardQueryBuilder<T>) queryBuilder;
            return qb.getCurrentTableAlias();
        }
        return null;
    }
    
    /**
     * 构建OVER子句
     */
    private String buildOverClause(List<Columnable<T, ?>> partitionBy,
                                   List<Columnable<T, ?>> orderBy) {
        StringBuilder overClause = new StringBuilder();
        
        // 构建PARTITION BY子句
        if (partitionBy != null && !partitionBy.isEmpty()) {
            overClause.append("PARTITION BY ");
            for (int i = 0; i < partitionBy.size(); i++) {
                if (i > 0) {
                    overClause.append(", ");
                }
                String fieldName = ColumnabledLambda.getColumnName(partitionBy.get(i));
                String currentTableAlias = getCurrentTableAlias();
                String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
                overClause.append(qualifiedField);
            }
        }
        
        // 构建ORDER BY子句
        if (orderBy != null && !orderBy.isEmpty()) {
            if (overClause.length() > 0) {
                overClause.append(" ");
            }
            overClause.append("ORDER BY ");
            for (int i = 0; i < orderBy.size(); i++) {
                if (i > 0) {
                    overClause.append(", ");
                }
                String fieldName = ColumnabledLambda.getColumnName(orderBy.get(i));
                String currentTableAlias = getCurrentTableAlias();
                String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
                overClause.append(qualifiedField);
            }
        }
        
        return overClause.toString();
    }
}
