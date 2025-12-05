package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.AbstractClause;
import com.kishultan.persistence.orm.query.FromClause;
import com.kishultan.persistence.orm.query.JoinClause;
import com.kishultan.persistence.orm.query.QueryBuilder;
import com.kishultan.persistence.orm.query.WhereClause;
import com.kishultan.persistence.orm.query.GroupClause;
import com.kishultan.persistence.orm.query.ClauseBuilder;
import com.kishultan.persistence.orm.query.CommonClause;
import com.kishultan.persistence.orm.query.context.ClauseResult;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.query.utils.EntityUtils;
import com.kishultan.persistence.orm.ColumnabledLambda;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * FromClause 实现类
 */
public class FromClauseImpl<T> extends AbstractClause<T> implements FromClause<T>, ClauseBuilder<T>, CommonClause<T> {
    
    private final String tableName;
    private final String tableAlias;
    private final Class<?> entityClass; // 添加实体类字段
    
    public FromClauseImpl(QueryBuilder<T> queryBuilder, String tableName, String tableAlias) {
        super(queryBuilder);
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.entityClass = null; // 字符串构造器不设置实体类
    }
    
    public FromClauseImpl(QueryBuilder<T> queryBuilder) {
        super(queryBuilder);
        this.tableName = null;
        this.tableAlias = null;
        this.entityClass = null;
    }
    
    // 添加带实体类的构造器
    public FromClauseImpl(QueryBuilder<T> queryBuilder, Class<?> entityClass, String tableName, String tableAlias) {
        super(queryBuilder);
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.entityClass = entityClass;
    }
    
    @Override
    public JoinClause<T> innerJoin(Class<?> entityClass, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardQueryBuilder<T>) queryBuilder, "INNER JOIN", entityClass, alias);
            ((StandardQueryBuilder<T>) queryBuilder).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> join(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return innerJoin(entityClass, alias);
    }

    @Override
    public JoinClause<T> innerJoin(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return innerJoin(entityClass, alias);
    }
    
    @Override
    public JoinClause<T> innerJoin(String tableName, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            // 通过表名推断实体类，这里需要根据业务逻辑处理
            // 暂时抛出异常，提示需要使用带entityClass的方法
            throw new UnsupportedOperationException(
                "innerJoin(String tableName, String alias) 方法已废弃，" +
                "请使用 innerJoin(Class<?> entityClass, String alias) 方法，" +
                "或者提供表名对应的实体类信息");
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> leftJoin(Class<?> entityClass, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardQueryBuilder<T>) queryBuilder, "LEFT JOIN", entityClass, alias);
            ((StandardQueryBuilder<T>) queryBuilder).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> leftJoin(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return leftJoin(entityClass, alias);
    }
    
    @Override
    public JoinClause<T> leftJoin(String tableName, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            // 通过表名推断实体类，这里需要根据业务逻辑处理
            // 暂时抛出异常，提示需要使用带entityClass的方法
            throw new UnsupportedOperationException(
                "leftJoin(String tableName, String alias) 方法已废弃，" +
                "请使用 leftJoin(Class<?> entityClass, String alias) 方法，" +
                "或者提供表名对应的实体类信息");
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> rightJoin(Class<?> entityClass, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardQueryBuilder<T>) queryBuilder, "RIGHT JOIN", entityClass, alias);
            ((StandardQueryBuilder<T>) queryBuilder).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> fullJoin(Class<?> entityClass, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardQueryBuilder<T>) queryBuilder, "FULL JOIN", entityClass, alias);
            ((StandardQueryBuilder<T>) queryBuilder).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public JoinClause<T> crossJoin(Class<?> entityClass, String alias) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardQueryBuilder<T>) queryBuilder, "CROSS JOIN", entityClass, alias);
            ((StandardQueryBuilder<T>) queryBuilder).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for join operations");
        }
    }
    
    @Override
    public WhereClause<T> where() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            WhereClause<T> whereClause = new WhereClauseImpl<>((StandardQueryBuilder<T>) queryBuilder);
            ((StandardQueryBuilder<T>) queryBuilder).setWhereClause(whereClause);
            return whereClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for where operations");
        }
    }
    
    @Override
    public WhereClause<T> where(Consumer<WhereClause<T>> whereBuilder) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            WhereClause<T> whereClause = new WhereClauseImpl<>((StandardQueryBuilder<T>) queryBuilder);
            ((StandardQueryBuilder<T>) queryBuilder).setWhereClause(whereClause);
            
            // 应用Consumer中定义的条件
            if (whereBuilder != null) {
                whereBuilder.accept(whereClause);
            }
            
            return whereClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for where operations");
        }
    }
    
    @Override
    public GroupClause<T> groupBy(String... columns) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            GroupClause<T> groupClause = new GroupClauseImpl<>((StandardQueryBuilder<T>) queryBuilder);
            if (columns != null) {
                for (String column : columns) {
                    ((GroupClauseImpl<T>) groupClause).addColumn(column);
                }
            }
            ((StandardQueryBuilder<T>) queryBuilder).setGroupClause(groupClause);
            return groupClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for groupBy operations");
        }
    }
    
    @Override
    public <R> GroupClause<T> groupBy(Columnable<T, R>... columns) {
        if (queryBuilder instanceof StandardQueryBuilder) {
            GroupClause<T> groupClause = new GroupClauseImpl<>((StandardQueryBuilder<T>) queryBuilder);
            if (columns != null) {
                for (Columnable<T, R> column : columns) {
                    String fieldName = com.kishultan.persistence.orm.ColumnabledLambda.getFieldName(column);
                    ((GroupClauseImpl<T>) groupClause).addColumn(fieldName);
                }
            }
            ((StandardQueryBuilder<T>) queryBuilder).setGroupClause(groupClause);
            return groupClause;
        } else {
            throw new UnsupportedOperationException("QueryBuilder must be StandardQueryBuilder for groupBy operations");
        }
    }
    
    // end方法实现已移除，FromClause现在直接继承CommonClause，可以直接调用执行方法
    

    
    /**
     * 生成表别名
     */
    private String generateAlias(Class<?> entityClass) {
        String tableName = EntityUtils.getTableName(entityClass);
        return tableName; // 别名和表名保持一致
    }
    
    // ==================== 新架构方法 ====================
    
    @Override
    public ClauseResult buildClause() {
        if (tableName == null) {
            return new ClauseResult("", new ArrayList<>());
        }
        
        StringBuilder sql = new StringBuilder("FROM ").append(tableName);
        if (tableAlias != null) {
            sql.append(" AS ").append(tableAlias);
        }
        
        // 自动注册别名到QueryBuilder
        if (queryBuilder instanceof StandardQueryBuilder && entityClass != null) {
            ((StandardQueryBuilder<T>) queryBuilder).registerTable(entityClass, tableName, tableAlias != null ? tableAlias : tableName);
        }
        
        return new ClauseResult(sql.toString(), new ArrayList<>());
    }
    
    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
}
