package com.kishultan.persistence.orm.query.utils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 实体工具类，用于处理JPA注解解析
 */
public class EntityUtils {
    
    private static final Map<Class<?>, String> tableNameCache = new HashMap<>();
    
    /**
     * 获取实体类对应的表名
     * 优先级：@Table(name="xxx") > @Entity(name="xxx") > 类名转下划线
     */
    public static String getTableName(Class<?> entityClass) {
        // 从缓存中获取
        if (tableNameCache.containsKey(entityClass)) {
            return tableNameCache.get(entityClass);
        }
        
        String tableName = null;
        
        // 1. 首先检查 @Table 注解
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            tableName = tableAnnotation.name();
        }
        
        // 2. 如果没有 @Table，检查 @Entity 注解
        if (tableName == null) {
            Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
            if (entityAnnotation != null && !entityAnnotation.name().isEmpty()) {
                tableName = entityAnnotation.name();
            }
        }
        
        // 3. 如果都没有，使用类名转换为下划线形式
        if (tableName == null) {
            tableName = camelCaseToUnderscore(entityClass.getSimpleName());
        }
        
        // 缓存结果
        tableNameCache.put(entityClass, tableName);
        
        return tableName;
    }
    
    /**
     * 获取字段对应的列名
     * 优先级：@Column(name="xxx") > 字段名转下划线
     */
    public static String getColumnName(Field field) {
        // 检查 @Column 注解
        javax.persistence.Column columnAnnotation = field.getAnnotation(javax.persistence.Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        
        // 使用字段名转换为下划线形式
        return camelCaseToUnderscore(field.getName());
    }
    
    /**
     * 获取实体类的所有列名
     * 返回所有字段对应的列名数组
     * 遵循JPA规范，跳过关联字段和不持久化字段
     */
    public static String[] getColumnNames(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        List<String> validColumnNames = new ArrayList<>();
        
        for (Field field : fields) {
            // 跳过关联字段
            if (isAssociationField(field)) {
                continue;
            }
            
            // 跳过不持久化字段
            if (isTransientField(field)) {
                continue;
            }
            
            // 跳过只读字段（insertable=false, updatable=false）
            if (isReadOnlyField(field)) {
                continue;
            }
            
            // 添加有效的列名
            validColumnNames.add(getColumnName(field));
        }
        
        return validColumnNames.toArray(new String[0]);
    }
    
    /**
     * 检查字段是否是关联字段
     */
    private static boolean isAssociationField(Field field) {
        // 检查JPA关联注解
        return field.isAnnotationPresent(javax.persistence.OneToMany.class) ||
               field.isAnnotationPresent(javax.persistence.ManyToOne.class) ||
               field.isAnnotationPresent(javax.persistence.OneToOne.class) ||
               field.isAnnotationPresent(javax.persistence.ManyToMany.class) ||
               field.isAnnotationPresent(javax.persistence.JoinColumn.class) ||
               field.isAnnotationPresent(javax.persistence.JoinTable.class);
    }
    
    /**
     * 检查字段是否是不持久化字段
     */
    private static boolean isTransientField(Field field) {
        return field.isAnnotationPresent(javax.persistence.Transient.class);
    }
    
    /**
     * 检查字段是否是只读字段
     */
    private static boolean isReadOnlyField(Field field) {
        javax.persistence.Column columnAnnotation = field.getAnnotation(javax.persistence.Column.class);
        if (columnAnnotation != null) {
            return !columnAnnotation.insertable() || !columnAnnotation.updatable();
        }
        return false;
    }
    
    /**
     * 驼峰命名转下划线命名
     * 例如：OrderItem -> order_item
     */
    private static String camelCaseToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 生成默认的表别名
     * 直接使用表名作为别名
     */
    public static String generateDefaultAlias(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return "t";
        }
        
        // 直接使用表名作为别名
        return tableName;
    }
    
    /**
     * 清除缓存（主要用于测试）
     */
    public static void clearCache() {
        tableNameCache.clear();
    }
    
    // ==================== 主键相关方法 ====================
    
    /**
     * 获取实体类的主键字段名
     * 优先级：@Column(name="xxx") > 字段名
     * 如果没有@Id注解，返回null
     */
    public static String getPrimaryKeyField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                return getColumnName(field);
            }
        }
        return null; // 没有主键也允许
    }
    
    /**
     * 获取实体类的主键字段名（带默认值）
     * 如果没有@Id注解，返回默认值"id"
     */
    public static String getPrimaryKeyFieldOrDefault(Class<?> entityClass) {
        String pkField = getPrimaryKeyField(entityClass);
        return pkField != null ? pkField : "id";
    }
    
    /**
     * 获取实体类的主键字段对象
     * 用于需要访问字段其他属性的场景
     */
    public static Field getPrimaryKeyFieldObject(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                return field;
            }
        }
        return null;
    }
}
