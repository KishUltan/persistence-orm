package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.DefaultRowMapper;
import com.kishultan.persistence.orm.query.SqlExecutor;
import com.kishultan.persistence.orm.query.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单SQL执行器实现
 */
public class SimpleSqlExecutor implements SqlExecutor {

    private final DefaultRowMapper rowMapper = new DefaultRowMapper();
    private final DataSource dataSource;
    
    public SimpleSqlExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType) {
        return executeQuery(sql,parameters,resultType,this.rowMapper);
    }
    
    @Override
    public <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType, RowMapper<T> mapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, parameters);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                //int rowNum = 1;
                while (rs.next()) {
                    T result = mapper.mapRow(rs, resultType);
                    //if (result != null) {
                        results.add(result);
                    //}
                }
                //按主键合并对象，解决连接查询主表数据重复的问题
                return this.rowMapper.mergeList(results,resultType);
            }
        } catch (Exception e) {
            throw new RuntimeException("执行查询失败: " + sql, e);
        }
    }
    
    @Override
    public <T> T executeAs(String sql, List<Object> parameters, Class<T> resultType) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, parameters);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (T)this.rowMapper.mapRow(rs,resultType);
                }
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("执行count查询失败: " + sql, e);
        }
    }
    
    @Override
    public long executeAsLong(String sql, List<Object> parameters) {
        return executeAs(sql,parameters,Long.class);
    }
    
    @Override
    public int executeUpdate(String sql, List<Object> parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, parameters);
            return stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("执行更新失败: " + sql, e);
        }
    }
    
    @Override
    public int[] executeBatchUpdate(List<String> sqlList, List<List<Object>> parametersList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            int[] results = new int[sqlList.size()];
            
            for (int i = 0; i < sqlList.size(); i++) {
                try (PreparedStatement stmt = connection.prepareStatement(sqlList.get(i))) {
                    setParameters(stmt, parametersList.get(i));
                    results[i] = stmt.executeUpdate();
                }
            }
            
            connection.commit();
            return results;
        } catch (Exception e) {
            throw new RuntimeException("批量更新失败", e);
        }
    }
    
    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
        }
    }
}

