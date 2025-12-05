package com.kishultan.persistence.orm.query;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射器接口
 * 负责将ResultSet的一行数据映射为指定类型的对象
 */
public interface RowMapper<T> {
    /**
     * 将ResultSet的一行数据映射为指定类型的对象
     * @param rs ResultSet
     * @param resultType 结果类型
     * @return 映射后的对象
     * @throws Exception 异常
     */
    T mapRow(ResultSet rs, Class<T> resultType) throws Exception;
}
