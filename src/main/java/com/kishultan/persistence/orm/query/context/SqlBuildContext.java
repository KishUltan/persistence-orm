package com.kishultan.persistence.orm.query.context;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL构建上下文，存储所有子句的信息
 */
public class SqlBuildContext<T> {
    private String selectClause = "";
    private String fromClause = "";
    private String joinClause = "";
    private List<JoinInfo> joinClauses = new ArrayList<>();
    private String whereClause = "";
    private String groupByClause = "";
    private String havingClause = "";
    private String orderByClause = "";
    private String limitClause = "";
    private List<Object> parameters = new ArrayList<>();
    
    // getter和setter方法
    public String getSelectClause() { return selectClause; }
    public void setSelectClause(String selectClause) { this.selectClause = selectClause; }
    
    public String getFromClause() { return fromClause; }
    public void setFromClause(String fromClause) { this.fromClause = fromClause; }
    
    public String getJoinClause() { return joinClause; }
    public void setJoinClause(String joinClause) { this.joinClause = joinClause; }
    
    public List<JoinInfo> getJoinClauses() { return joinClauses; }
    public void setJoinClauses(List<JoinInfo> joinClauses) { this.joinClauses = joinClauses; }
    
    public String getWhereClause() { return whereClause; }
    public void setWhereClause(String whereClause) { this.whereClause = whereClause; }
    
    public String getGroupByClause() { return groupByClause; }
    public void setGroupByClause(String groupByClause) { this.groupByClause = groupByClause; }
    
    public String getHavingClause() { return havingClause; }
    public void setHavingClause(String havingClause) { this.havingClause = havingClause; }
    
    public String getOrderByClause() { return orderByClause; }
    public void setOrderByClause(String orderByClause) { this.orderByClause = orderByClause; }
    
    public String getLimitClause() { return limitClause; }
    public void setLimitClause(String limitClause) { this.limitClause = limitClause; }
    
    public List<Object> getParameters() { return parameters; }
    public void setParameters(List<Object> parameters) { this.parameters = parameters; }
    
    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }
    
    public void addParameters(List<Object> parameters) {
        this.parameters.addAll(parameters);
    }
    
    /**
     * 清空构建上下文
     */
    public void clear() {
        selectClause = "";
        fromClause = "";
        joinClause = "";
        joinClauses.clear();
        whereClause = "";
        groupByClause = "";
        havingClause = "";
        orderByClause = "";
        limitClause = "";
        parameters.clear();
    }
}
