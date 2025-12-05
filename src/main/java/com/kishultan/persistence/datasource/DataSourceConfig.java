package com.kishultan.persistence.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kishultan.persistence.config.PersistenceDefaults;

/**
 * Database configuration utility class.
 * Extracted from DBConfigUtil to avoid dependencies on the util package.
 * This class is designed to replace DBConfigUtil functionality.
 * 
 * Note: XML configuration support has been removed. Use programmatic configuration instead.
 */
public class DataSourceConfig {

    private static boolean dscInitialized = false;
    private static HashMap<String, String> dscDataSourceMap = new HashMap<String, String>();
    private static DataSourceConfig dscSelf = null;
    private static String dscRealPath = "";
    private static String dscInMemoryDataSource = "java:comp/env/jdbc/inmemorydb";
    private static String dscOnlineUserTableName = "IM_ACTIVE_SESSIONS";
    private static boolean dscIsShutdownDefaultDataSource = false;
    private static String dscDataSourceClass = null;
    private static final Logger dscLogger = LoggerFactory.getLogger(DataSourceConfig.class);
    private static List<DataSourceInfo> dscDataSources = new ArrayList<DataSourceInfo>();
    private static List<LocalDataSourceInfo> dscLocalDataSources = new ArrayList<LocalDataSourceInfo>();

    public static class DataSourceInfo {
        public String name = null;
        public String label = null;
        public String flavor = null;
    }
    
    public static class PropertyInfo {
        public String name = null;
        public String value = null;
    }
    
    public static class LocalDataSourceInfo {
        public String driverClassName = null;
        public String url = null;
        public String userName = null;
        public String password = null;
        public String name = null;
        public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    }

    public List<DataSourceInfo> getDataSources() {
        return dscDataSources;
    }
    
    public void setDataSources(List<DataSourceInfo> ds) {
        dscDataSources = ds;
    }
    
    public List<LocalDataSourceInfo> getLocalDataSources() {
        return dscLocalDataSources;
    }
    
    public void setLocalDataSources(List<LocalDataSourceInfo> ds) {
        dscLocalDataSources = ds;
    }

    public String getDefaultDataSourceName() {
        return PersistenceDefaults.getDataSourceName();
    }
    
    public void setDefaultDataSourceName(String dsName) {
        PersistenceDefaults.setDataSourceName(dsName);
        dscLogger.info("Set default data source name to " + dsName);
    }

    public boolean isShutdownDefaultDataSource() {
        return dscIsShutdownDefaultDataSource;
    }

    public void setShutdownDefaultDataSource(boolean shutdown) {
        dscIsShutdownDefaultDataSource = shutdown;
    }

    public String getOnlineUserDataSourceName() {
        return dscInMemoryDataSource;
    }

    public void setOnlineUserDataSourceName(String dataSourceName) {
        dscInMemoryDataSource = dataSourceName;
    }

    public String getOnlineUserTableName() {
        return dscOnlineUserTableName;
    }

    public void setOnlineUserTableName(String tableName) {
        dscOnlineUserTableName = tableName;
    }

    public String getDataSourceClass() {
        return dscDataSourceClass;
    }

    public void setDataSourceClass(String dataSourceClass) {
        dscDataSourceClass = dataSourceClass;
    }

    /**
     * Initialize data source configuration from XML file.
     * 
     * Note: XML configuration support has been removed. This method does nothing.
     * Use programmatic configuration via DataSourceManager instead.
     * 
     * @param dbConfigFile XML configuration file path (ignored)
     * @param webAppRoot Web application root path (ignored)
     */
    public static synchronized void init(String dbConfigFile, String webAppRoot) {
        if (dscInitialized) return;
        
        dscRealPath = webAppRoot;
        dscSelf = new DataSourceConfig();
        dscInitialized = true;
        dscLogger.warn("XML configuration support has been removed. Use programmatic configuration via DataSourceManager instead.");
    }
    
    /**
     * Initialize data source configuration from input stream.
     * 
     * Note: XML configuration support has been removed. This method does nothing.
     * Use programmatic configuration via DataSourceManager instead.
     * 
     * @param fis Input stream (ignored)
     * @param webAppRoot Web application root path (ignored)
     */
    public synchronized static void init(InputStream fis, String webAppRoot) {
        if (dscInitialized) return;
        
        dscRealPath = webAppRoot;
        dscSelf = new DataSourceConfig();
        dscInitialized = true;
        dscLogger.warn("XML configuration support has been removed. Use programmatic configuration via DataSourceManager instead.");
    }

    public static void setDataSource(String webappRoot, LocalDataSourceInfo dsInfo) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        String dataSourceName = dsInfo.name;
        String url = dsInfo.url;
        String username = dsInfo.userName;
        String password = dsInfo.password;
        String driver = dsInfo.driverClassName;
        
        int webRootIndex = url.indexOf("$");
        if (webRootIndex != -1) {
            if (url.substring(webRootIndex).startsWith("${webapp.root}")) {
                url = url.substring(0, webRootIndex) +
                webappRoot.replaceAll("\\\\", "/") +
                url.substring(webRootIndex + 15);
            }
        }

        DataSource ds = null;
        
        if (dscDataSourceClass == null) {
            // Use a simple DataSource implementation that doesn't depend on any connection pool library
            // Users can provide their own connection pool by setting dscDataSourceClass
            ds = createSimpleDataSource(url, username, password, driver);
            dscLogger.info("Created simple DataSource for: {}. " +
                "For production use, consider using a connection pool library (e.g., HikariCP) " +
                "by setting the data source class.", dataSourceName);
        } else {
            // Use the specified DataSource class
            // Note: The DataSource class should be configured by the user before use
            // Properties will be set via setProperty method below
            try {
                ds = (DataSource) Class.forName(dscDataSourceClass).newInstance();
                dscLogger.info("Using custom DataSource class: {}", dscDataSourceClass);
            } catch (Exception e) {
                dscLogger.warn("Failed to instantiate DataSource class: " + dscDataSourceClass, e);
                // Fall back to simple implementation
                ds = createSimpleDataSource(url, username, password, driver);
            }
        }
        
        for (int j=0; j<dsInfo.properties.size(); j++) {
            try {
                PropertyInfo propInfo = dsInfo.properties.get(j);
                setProperty(ds, propInfo.name, propInfo.value, true);
            } catch (Exception e) {
                dscLogger.warn("Error in setting data source property.", e);
            }
        }
        
        DataSourceManager.addLocalDataSource(dataSourceName, ds);
    }

    public static HashMap getDataSourceMap() {
        return dscDataSourceMap;
    }

    public static DataSourceConfig getInstance() {
        return dscSelf;
    }

    /**
     * Save configuration to XML file.
     * 
     * Note: XML configuration support has been removed. This method does nothing.
     * 
     * @throws UnsupportedOperationException always
     */
    public static void save() throws IOException {
        throw new UnsupportedOperationException("XML configuration support has been removed. Use programmatic configuration instead.");
    }
    
    /**
     * Create a simple DataSource implementation using DriverManager.
     * This is a basic implementation that doesn't require any connection pool library.
     * For production use, users should provide their own connection pool implementation.
     */
    private static DataSource createSimpleDataSource(final String url, final String username, 
                                                     final String password, final String driver) {
        // Load driver class if specified
        if (driver != null && !driver.isEmpty()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                dscLogger.warn("JDBC driver class not found: " + driver, e);
            }
        }
        
        // Create a simple DataSource implementation
        return new javax.sql.DataSource() {
            @Override
            public java.sql.Connection getConnection() throws java.sql.SQLException {
                return getConnection(username, password);
            }
            
            @Override
            public java.sql.Connection getConnection(String user, String pass) throws java.sql.SQLException {
                return java.sql.DriverManager.getConnection(url, user, pass);
            }
            
            @Override
            public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {
                return null;
            }
            
            @Override
            public void setLogWriter(java.io.PrintWriter out) throws java.sql.SQLException {
                // Not supported
            }
            
            @Override
            public void setLoginTimeout(int seconds) throws java.sql.SQLException {
                java.sql.DriverManager.setLoginTimeout(seconds);
            }
            
            @Override
            public int getLoginTimeout() throws java.sql.SQLException {
                return java.sql.DriverManager.getLoginTimeout();
            }
            
            @Override
            public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
                throw new java.sql.SQLFeatureNotSupportedException();
            }
            
            @Override
            public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
                if (iface.isInstance(this)) {
                    return iface.cast(this);
                }
                throw new java.sql.SQLException("DataSource is not a wrapper for " + iface.getName());
            }
            
            @Override
            public boolean isWrapperFor(Class<?> iface) throws java.sql.SQLException {
                return iface.isInstance(this);
            }
        };
    }
    
    /**
     * Checks if a string is not null or empty.
     * This method is extracted from StringUtil to avoid dependency on the util package.
     */
    private static boolean isNotNullOrEmpty(String paramValue) {
        return (paramValue != null && paramValue.length() > 0);
    }
    
    /**
     * Sets a property on an object using reflection.
     * This method is extracted from ReflectionUtil to avoid dependency on the util package.
     */
    private static void setProperty(Object obj, String propertyName, String propertyValue, boolean convertString) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            
            if (convertString) {
                Class<?> fieldType = field.getType();
                if (fieldType == String.class) {
                    field.set(obj, propertyValue);
                } else if (fieldType == int.class || fieldType == Integer.class) {
                    field.set(obj, Integer.parseInt(propertyValue));
                } else if (fieldType == long.class || fieldType == Long.class) {
                    field.set(obj, Long.parseLong(propertyValue));
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    field.set(obj, Boolean.parseBoolean(propertyValue));
                } else if (fieldType == double.class || fieldType == Double.class) {
                    field.set(obj, Double.parseDouble(propertyValue));
                } else if (fieldType == float.class || fieldType == Float.class) {
                    field.set(obj, Float.parseFloat(propertyValue));
                } else {
                    field.set(obj, propertyValue);
                }
            } else {
                field.set(obj, propertyValue);
            }
        } catch (Exception e) {
            dscLogger.warn("Error setting property " + propertyName + " on " + obj.getClass().getName(), e);
        }
    }
} 