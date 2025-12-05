package com.kishultan.persistence.config;

/**
 * 持久化默认配置类
 * 
 * 提供默认数据源名称等配置信息
 * 用于替代原项目中的 com.kishultan.foundation.Defaults
 * 
 * @author Portal Team
 */
public class PersistenceDefaults {
    
    private static String defaultDataSourceName = "default";
    
    /**
     * 获取默认数据源名称
     * 
     * 优先级：
     * 1. 系统属性 persistence.default.datasource
     * 2. 静态配置 defaultDataSourceName
     * 
     * @return 默认数据源名称
     */
    public static String getDataSourceName() {
        String name = System.getProperty("persistence.default.datasource");
        return name != null ? name : defaultDataSourceName;
    }
    
    /**
     * 设置默认数据源名称
     * 
     * @param dataSourceName 数据源名称
     */
    public static void setDataSourceName(String dataSourceName) {
        if (dataSourceName != null && !dataSourceName.trim().isEmpty()) {
            defaultDataSourceName = dataSourceName;
        }
    }
    
    /**
     * 重置为默认值
     */
    public static void reset() {
        defaultDataSourceName = "default";
    }
}


