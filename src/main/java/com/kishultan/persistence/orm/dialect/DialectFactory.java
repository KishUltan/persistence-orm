package com.kishultan.persistence.orm.dialect;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库方言工厂
 * 
 * @author Portal Team
 */
public class DialectFactory {
    
    private static final Map<String, DatabaseDialect> DIALECTS = new HashMap<>();
    
    static {
        // 注册支持的数据库方言
        DIALECTS.put("mysql", new MySQLDialect());
        DIALECTS.put("h2", new H2Dialect());
    }
    
    /**
     * 根据数据库类型获取方言
     * @param databaseType 数据库类型
     * @return 数据库方言
     */
    public static DatabaseDialect getDialect(String databaseType) {
        DatabaseDialect dialect = DIALECTS.get(databaseType.toLowerCase());
        if (dialect == null) {
            // 默认使用H2方言
            dialect = DIALECTS.get("h2");
        }
        return dialect;
    }
    
    /**
     * 注册新的数据库方言
     * @param databaseType 数据库类型
     * @param dialect 数据库方言
     */
    public static void registerDialect(String databaseType, DatabaseDialect dialect) {
        DIALECTS.put(databaseType.toLowerCase(), dialect);
    }
} 