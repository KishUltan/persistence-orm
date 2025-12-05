package com.kishultan.persistence.orm.query;

import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class DefaultRowMapper<T> implements RowMapper<T> {

    private final Map<String, TableMeta> aliasMapping = new HashMap<>();

    public void register(Class<?> clazz) {
        String tableName = toTableName(clazz);
        String alias = tableName;
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    public void register(Class<?> clazz, String alias) {
        String tableName = toTableName(clazz);
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    public void register(String tableName, String alias, Class<?> clazz) {
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    /**
     * 将当前 ResultSet 所在行映射为对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, Class<T> resultType) throws Exception {

        if (List.class.isAssignableFrom(resultType)) {
            throw new IllegalArgumentException("resultType cannot be List type.");
        }

        // 1️⃣ 简单类型 如果是基础类型或常见简单类型，直接取第一列
        if (isSimpleType(resultType)) {
            Object val = rs.getObject(1); // 默认取第一列
            return (T) convertValue(val, resultType);
        }

        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        // 2️⃣ Map 类型
        if (Map.class.isAssignableFrom(resultType)) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                String colName = meta.getColumnLabel(i);
                Object val = rs.getObject(i);
                rowMap.put(colName, val);
            }
            return (T) rowMap;
        }

        // 3️⃣ 实体类
        Set<String> visited = new HashSet<>(); // 防止循环引用
        return buildEntity(rs, resultType, colCount, meta, visited);
    }

    /**
     * 合并列表，按主键去重
     */
    public <T> List<T> mergeList(List<T> rawList, Class<T> resultType) throws Exception {
        Map<Object, T> mergedMap = new LinkedHashMap<>();

        Field idField = getPkField(resultType);
        if (idField == null) {
            throw new IllegalStateException("No @Id field found for class " + resultType.getName());
        }
        idField.setAccessible(true);

        for (T obj : rawList) {
            Object idVal = idField.get(obj);

            if (idVal == null) {
                mergedMap.put(UUID.randomUUID(), obj);
                continue;
            }

            if (mergedMap.containsKey(idVal)) {
                merge(mergedMap.get(idVal), obj, new HashSet<>());
            } else {
                mergedMap.put(idVal, obj);
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 合并两个对象（递归），避免循环引用
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(T existing, T incoming, Set<String> visited) throws Exception {
        if (existing == null) return incoming;
        if (incoming == null) return existing;

        Class<?> clazz = existing.getClass();
        Field pk = getPkField(clazz);
        Object pkVal = pk != null ? pk.get(existing) : null;
        String visitedKey = clazz.getName() + ":" + pkVal;
        if (visited.contains(visitedKey)) {
            return existing;
        }
        visited.add(visitedKey);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            Object oldVal = field.get(existing);
            Object newVal = field.get(incoming);

            if (newVal == null) continue;

            // 集合类型（OneToMany）
            if (Collection.class.isAssignableFrom(field.getType())) {
                Collection<Object> oldCol = (Collection<Object>) oldVal;
                Collection<Object> newCol = (Collection<Object>) newVal;
                if (newCol != null && !newCol.isEmpty()) {
                    if (oldCol == null) {
                        oldCol = new ArrayList<>();
                        field.set(existing, oldCol);
                    }
                    // 按主键去重
                    for (Object item : newCol) {
                        Object itemId = getEntityId(item);
                        if (itemId == null || oldCol.stream().noneMatch(o -> Objects.equals(getEntityId(o), itemId))) {
                            oldCol.add(item);
                        }
                    }
                }
            }
            // 嵌套实体（OneToOne / ManyToOne）
            else if (isEntity(field.getType())) {
                Object mergedChild = merge(oldVal, newVal, visited);
                field.set(existing, mergedChild);
            }
            // 普通字段
            else {
                field.set(existing, newVal);
            }
        }

        return existing;
    }

    private boolean isEntity(Class<?> clazz) {
        return aliasMapping.values().stream()
                .anyMatch(meta -> meta.entityClass.equals(clazz));
    }

    @SuppressWarnings("unchecked")
    private <T> T buildEntity(ResultSet rs, Class<T> rootClass,
                              int colCount, ResultSetMetaData meta,
                              Set<String> visited) throws Exception {

        TableMeta rootMeta = findMetaByClass(rootClass);
        if (rootMeta == null) {
            throw new IllegalStateException("Class not registered: " + rootClass.getName());
        }

        Object pkValue = null;
        Map<String, Object> values = new HashMap<>();

        for (int i = 1; i <= colCount; i++) {
            String label = meta.getColumnLabel(i);
            String alias, field;
            if (label.contains("__")) {
                String[] parts = label.split("__", 2);
                alias = parts[0]; field = parts[1];
            } else {
                alias = rootMeta.tableName; field = label;
            }

            TableMeta tm = resolveTableMeta(alias, rootClass);
            if (tm == null || !tm.entityClass.equals(rootClass)) continue;

            Object val = rs.getObject(i);
            values.put(field, val);
            if (field.equalsIgnoreCase(tm.pkField)) pkValue = val;
        }

        if (pkValue == null) return null;

        String visitedKey = rootMeta.tableName + ":" + pkValue;
        if (visited.contains(visitedKey)) {
            return (T) createProxyObject(rootClass, pkValue);
        }
        visited.add(visitedKey);

        T instance = rootClass.getDeclaredConstructor().newInstance();
        populate(instance, values);

        for (Field field : rootClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            TableMeta childMeta = findMetaByClass(fieldType);

            if (childMeta != null) {
                Object child = buildEntity(rs, fieldType, colCount, meta, visited);
                if (child != null) setField(instance, field, child);

            } else if (Collection.class.isAssignableFrom(fieldType)) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> elemType = (Class<?>) listType.getActualTypeArguments()[0];
                TableMeta elemMeta = findMetaByClass(elemType);
                if (elemMeta != null) {
                    Object child = buildEntity(rs, elemType, colCount, meta, visited);
                    if (child != null) {
                        Collection<Object> coll = (Collection<Object>) getField(instance, field);
                        if (coll == null) {
                            coll = new ArrayList<>();
                            setField(instance, field, coll);
                        }
                        Object childId = getEntityId(child);
                        if (childId == null || coll.stream().noneMatch(o -> Objects.equals(getEntityId(o), childId))) {
                            coll.add(child);
                        }
                    }
                }
            }
        }

        visited.remove(visitedKey);
        return instance;
    }

    private void populate(Object instance, Map<String, Object> values) throws Exception {
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String fieldName = camelCase(e.getKey());
            try {
                Field f = instance.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object convertedValue = convertValue(e.getValue(), f.getType());
                f.set(instance, convertedValue);
            } catch (NoSuchFieldException ignore) {}
        }
    }

    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == java.util.Date.class
                || clazz == java.time.LocalDate.class
                || clazz == java.time.LocalDateTime.class
                || clazz == java.time.LocalTime.class;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        if (value instanceof java.sql.Date && targetType == java.time.LocalDate.class) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.sql.Timestamp && targetType == java.time.LocalDateTime.class) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Time && targetType == java.time.LocalTime.class) {
            return ((java.sql.Time) value).toLocalTime();
        }
        if (value instanceof String) {
            String str = (String) value;
            if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(str);
            if (targetType == Long.class || targetType == long.class) return Long.parseLong(str);
            if (targetType == Double.class || targetType == double.class) return Double.parseDouble(str);
            if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(str);
        }
        return value;
    }

    private void setField(Object obj, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(obj, value);
    }

    private Object getField(Object obj, Field field) throws Exception {
        field.setAccessible(true);
        return field.get(obj);
    }

    private String camelCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                up = true;
            } else if (up) {
                sb.append(Character.toUpperCase(c));
                up = false;
            } else {
                sb.append(c);
            }
        }
        // 处理 ID → Id
        if (sb.toString().endsWith("ID")) {
            return sb.substring(0, sb.length() - 2) + "Id";
        }
        return sb.toString();
    }

    private TableMeta findMetaByClass(Class<?> clazz) {
        return aliasMapping.values().stream()
                .filter(m -> m.entityClass.equals(clazz))
                .findFirst().orElse(null);
    }

    private static String toTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (table.name() != null && !table.name().isEmpty()) {
                return table.name();
            }
        }
        return camelToUnderline(clazz.getSimpleName());
    }

    private static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private static Field getPkField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    private static Object getEntityId(Object entity) {
        if (entity == null) return null;
        Field pk = getPkField(entity.getClass());
        if (pk == null) return null;
        try {
            return pk.get(entity);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static class TableMeta {
        String tableName;
        String pkField;
        Class<?> entityClass;
        TableMeta(String tableName, String pkField, Class<?> entityClass) {
            this.tableName = tableName;
            this.pkField = pkField;
            this.entityClass = entityClass;
        }
    }

    private TableMeta resolveTableMeta(String alias, Class<?> targetClass) {
        TableMeta tm = aliasMapping.get(alias);
        if (tm != null) return tm;
        String tableName = toTableName(targetClass);
        Field pk = getPkField(targetClass);
        if (pk == null) {
            throw new IllegalStateException("No @Id field found for class " + targetClass.getName());
        }
        return new TableMeta(tableName, pk.getName(), targetClass);
    }

    private Object createProxyObject(Class<?> targetClass, Object idValue) {
        try {
            Object proxy = targetClass.getDeclaredConstructor().newInstance();
            Field pk = getPkField(targetClass);
            if (pk != null) {
                pk.setAccessible(true);
                pk.set(proxy, idValue);
            }
            return proxy;
        } catch (Exception e) {
            return null;
        }
    }

    private Collection<Object> createProxyList(Class<?> elementType, Object parent) {
        Object proxy = createProxyObject(elementType, null);
        List<Object> list = new ArrayList<>();
        if (proxy != null) list.add(proxy);
        return list;
    }
}
