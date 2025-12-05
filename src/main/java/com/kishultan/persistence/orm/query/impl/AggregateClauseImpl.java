package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.ColumnabledLambda;
import com.kishultan.persistence.orm.query.ClauseBuilder;
import com.kishultan.persistence.orm.query.context.ClauseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合函数子句实现类
 */
public class AggregateClauseImpl<T> extends SelectClauseImpl<T> implements AggregateClause<T>, ClauseBuilder<T> {
    
    // 聚合函数字段
    private final List<String> aggregateFunctions = new ArrayList<>();
    
    public AggregateClauseImpl(QueryBuilder<T> queryBuilder) {
        super(queryBuilder);
    }
    
    // ==================== 聚合函数实现 ====================
    
    @Override
    public AggregateClause<T> count(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("COUNT(" + qualifiedField + ")");
        return this;
    }
    
    @Override
    public AggregateClause<T> count(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("COUNT(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public AggregateClause<T> sum(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("SUM(" + qualifiedField + ")");
        return this;
    }
    
    @Override
    public AggregateClause<T> sum(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("SUM(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public AggregateClause<T> avg(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("AVG(" + qualifiedField + ")");
        return this;
    }
    
    @Override
    public AggregateClause<T> avg(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("AVG(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public AggregateClause<T> max(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("MAX(" + qualifiedField + ")");
        return this;
    }
    
    @Override
    public AggregateClause<T> max(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("MAX(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public AggregateClause<T> min(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("MIN(" + qualifiedField + ")");
        return this;
    }
    
    @Override
    public AggregateClause<T> min(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        aggregateFunctions.add("MIN(" + qualifiedField + ") AS " + alias);
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
        
        if (aggregateFunctions.isEmpty()) {
            sql.append("*");
        } else {
            for (int i = 0; i < aggregateFunctions.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(aggregateFunctions.get(i));
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
        if (aggregateFunctions.isEmpty()) {
            return "*";
        }
        return String.join(", ", aggregateFunctions);
    }
    
    // ==================== 辅助方法 ====================
    
    private String getCurrentTableAlias() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            StandardQueryBuilder<T> qb = (StandardQueryBuilder<T>) queryBuilder;
            return qb.getCurrentTableAlias();
        }
        return null;
    }
}
