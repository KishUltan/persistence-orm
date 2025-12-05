package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.query.ClauseBuilder;
import com.kishultan.persistence.orm.query.context.ClauseResult;
import com.kishultan.persistence.orm.query.utils.EntityUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * SELECT子句实现类
 */
public class SelectClauseImpl<T> extends AbstractClause<T> implements SelectClause<T>, ClauseBuilder<T> {
    
    private boolean selectAll = false;
    private List<String> selectedFields = new ArrayList<>();
    
    public SelectClauseImpl(QueryBuilder<T> queryBuilder) {
        super(queryBuilder);
    }
    
    public SelectClauseImpl(QueryBuilder<T> queryBuilder, boolean selectAll) {
        super(queryBuilder);
        this.selectAll = selectAll;
    }
    
    // ==================== SELECT字段 ====================
    
    /**
     * 添加字段到SELECT子句
     */
    public void addField(String field) {
        selectedFields.add(field);
    }
    
    // ==================== FROM子句 ====================
    
    @Override
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
    }
    
    @Override
    public FromClause<T> fromSubquery(String subquerySql) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, "(" + subquerySql + ")", "subquery");
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> fromSubquery(QueryBuilder<T> subquery) {
        // 获取子查询SQL，但不立即合并参数
        //System.out.println("*** 子查询调用 生成的SQL开始 ***");
        String subquerySql = subquery.getGeneratedSql();
        //System.out.println("*** 子查询调用 生成的SQL结束 ***");
        
        // 创建FROM子句，并保存子查询引用以便后续动态收集参数
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, "(" + subquerySql + ")", "subquery");
        
        // 如果是QueryBuilderImpl，保存子查询引用以便后续参数收集
        if (queryBuilder instanceof StandardQueryBuilder && subquery instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setSubquery((StandardQueryBuilder<?>) subquery);
        }
        
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    // ==================== 新架构方法 ====================
    
    @Override
    public ClauseResult buildClause() {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        if (selectAll || selectedFields.isEmpty()) {
            // 🔧 智能展开：如果有JOIN，自动展开为所有相关表字段避免歧义
            if (hasJoinClause()) {
                sql.append("SELECT ");
                String[] allTableFields = getQueryBuilderTableFields();
                for (int i = 0; i < allTableFields.length; i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append(allTableFields[i]);
                }
            } else {
                sql.append("SELECT *");
            }
        } else {
            sql.append("SELECT ");
            // 🔧 如果有JOIN，为用户选择的字段也添加别名
            if (hasJoinClause()) {
                for (int i = 0; i < selectedFields.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    String field = selectedFields.get(i);
                    // 为字段添加别名：将.替换为__，避免字段名冲突
                    String fieldAlias = field.replace(".", "__");
                    sql.append(field).append(" AS ").append(fieldAlias);
                }
            } else {
                for (int i = 0; i < selectedFields.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append(selectedFields.get(i));
                }
            }
        }
        
        return new ClauseResult(sql.toString(), parameters);
    }
    
    // ==================== 智能展开辅助方法 ====================
    
    /**
     * 检查是否有JOIN子句
     */
    private boolean hasJoinClause() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            StandardQueryBuilder<T> qb = (StandardQueryBuilder<T>) queryBuilder;
            return qb.hasJoinClause();
        }
        return false;
    }
    
    /**
     * 获取所有相关表字段（带表别名）
     */
    private String[] getQueryBuilderTableFields() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            StandardQueryBuilder<T> qb = (StandardQueryBuilder<T>) queryBuilder;
            return qb.getAllTableFields();
        }
        return new String[0];
    }
    

    
    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== SELECT字段设置 ====================
    
    public void setSelectedFields(String... fields) {
        selectedFields.clear();
        if (fields != null) {
            for (String field : fields) {
                selectedFields.add(field);
            }
        }
    }
    
    public List<String> getSelectedFields() {
        return selectedFields;
    }
    

}
