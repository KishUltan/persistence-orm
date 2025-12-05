package com.kishultan.persistence.orm.query;

/**
 * GROUP BY子句接口
 * 提供GROUP BY子句功能，遵循SQL书写习惯
 */
public interface GroupClause<T> extends CommonClause<T> {
    
    // groupBy方法已移至FromClause，符合SQL语法顺序
    
    /**
     * HAVING子句
     */
    HavingClause<T> having();
    
    // end方法已移除，GroupClause现在直接继承CommonClause，可以直接调用执行方法
}




