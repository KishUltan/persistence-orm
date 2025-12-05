package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.query.context.ClauseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * GROUP BY子句实现
 */
public class GroupClauseImpl<T> extends AbstractClause<T> implements GroupClause<T>, ClauseBuilder<T> {
    
    private final List<String> groupColumns = new ArrayList<>();
    
    public GroupClauseImpl(StandardQueryBuilder<T> queryBuilder) {
        super(queryBuilder);
    }
    
    // groupBy方法实现已移除，现在通过FromClause直接调用addColumn方法
    
    /**
     * 添加分组字段（供FromClause直接调用）
     */
    public void addColumn(String column) {
        groupColumns.add(column);
    }
    
    @Override
    public HavingClause<T> having() {
        HavingClauseImpl<T> havingClause = new HavingClauseImpl<T>((StandardQueryBuilder<T>) queryBuilder);
        ((StandardQueryBuilder<T>) queryBuilder).setHavingClause(havingClause);
        return havingClause;
    }
    

    
    @Override
    public ClauseResult buildClause() {
        if (groupColumns.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }

        StringBuilder sql = new StringBuilder("GROUP BY ");
        for (int i = 0; i < groupColumns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(groupColumns.get(i));
        }

        return new ClauseResult(sql.toString(), new ArrayList<>());
    }
    
    @Override
    public String getClauseSql() {
        ClauseResult result = buildClause();
        return result.getSql();
    }
}
