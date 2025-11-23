# Persistence 模块完整功能说明和使用指南

**文档版本**: 1.0  
**最后更新**: 2025年11月22日  
**维护者**: Portal Team

---

## 📋 目录

1. [概述](#概述)
2. [架构设计](#架构设计)
3. [核心组件](#核心组件)
4. [基础功能](#基础功能)
5. [高级功能](#高级功能)
6. [查询构建器](#查询构建器)
7. [性能优化](#性能优化)
8. [最佳实践](#最佳实践)
9. [故障排除](#故障排除)
10. [API参考](#api参考)

---

## 概述

### 什么是 Persistence 模块

Persistence 模块是 Portal 项目的持久化层，提供了完整的对象关系映射（ORM）功能。它基于 SansOrm 库，通过门面模式封装了底层实现细节，为应用程序提供了统一、类型安全、高性能的持久化操作接口。

### 核心特性

- ✅ **统一的持久化API**: 提供一致的 CRUD 操作接口
- ✅ **类型安全**: 支持 Lambda 表达式，编译时类型检查
- ✅ **多数据源支持**: 支持多个数据源的统一管理
- ✅ **事务管理**: 完整的事务支持，线程安全
- ✅ **强大的查询能力**: 支持简单查询、复杂查询、流式查询
- ✅ **性能优化**: 内置查询缓存、性能监控、慢查询日志
- ✅ **灵活的分页**: 支持多种分页策略
- ✅ **数据库方言支持**: 支持 MySQL、H2 等多种数据库

### 设计原则

1. **门面模式**: 隐藏第三方库实现细节
2. **依赖倒置**: 依赖接口而非具体实现
3. **模块化设计**: 清晰的包结构，职责分离
4. **配置驱动**: 通过配置控制行为
5. **线程安全**: 所有核心组件都是线程安全的

---

## 架构设计

### 包结构

```
persistence/
├── PersistenceManager          # 持久化管理器（入口）
├── config/                     # 配置类
│   └── PersistenceConfig       # 持久化配置
├── datasource/                 # 数据源管理
│   ├── DataSourceManager      # 数据源管理器
│   └── DataSourceConfig         # 数据源配置
└── orm/                        # ORM核心
    ├── EntityManager           # 实体管理器
    ├── EntityManagerFactory    # 实体管理器工厂
    ├── EntityTransaction       # 事务接口
    ├── EntityQuery             # 实体查询接口
    ├── QueryCondition          # 查询条件接口
    ├── Columnable              # Lambda表达式接口
    ├── ColumnabledLambda       # Lambda工具类
    ├── PersistenceQueryWrapper # 查询包装器（门面）
    ├── SimpleEntityQuery       # 简单查询实现
    ├── delegate/               # 实现委托
    │   ├── SansOrmEntityManagerFactory
    │   ├── SansOrmEntityTransaction
    │   └── SansOrmFactoryProvider
    ├── dialect/                # 数据库方言
    │   ├── DatabaseDialect
    │   ├── DialectFactory
    │   ├── MySQLDialect
    │   └── H2Dialect
    └── query/                  # 查询构建器
        ├── QueryBuilder        # 查询构建器接口
        ├── SelectClause        # SELECT子句
        ├── WhereClause         # WHERE子句
        ├── JoinClause          # JOIN子句
        ├── AggregateClause     # 聚合函数
        ├── WindowClause        # 窗口函数
        ├── CaseWhenClause      # CASE WHEN表达式
        ├── ExpressionClause    # 表达式函数
        ├── GroupClause         # GROUP BY子句
        ├── HavingClause        # HAVING子句
        ├── OrderClause         # ORDER BY子句
        ├── StreamingQueryBuilder # 流式查询
        ├── cache/              # 查询缓存
        ├── monitor/            # 性能监控
        ├── config/             # 查询配置
        └── impl/               # 实现类
```

### 依赖关系

```
PersistenceManager (上层)
  ↓
EntityManager (核心接口)
  ↓
EntityManagerFactory (工厂接口)
  ↓
SansOrmEntityManagerFactory (具体实现)
```

### 数据流

```
应用程序
  ↓
PersistenceManager.getDefaultManager()
  ↓
EntityManager
  ↓
EntityQuery / QueryBuilder
  ↓
SQL执行
  ↓
结果映射
  ↓
返回实体对象
```

---

## 核心组件

### 1. PersistenceManager

持久化管理器，提供统一的持久化操作入口。

#### 主要方法

```java
// 获取默认的EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 获取指定数据源的EntityManager
EntityManager em = PersistenceManager.getManager("myDataSource");

// 关闭管理器
PersistenceManager.shutdown();
PersistenceManager.shutdown("myDataSource");
PersistenceManager.shutdownAll();

// 检查数据源可用性
boolean available = PersistenceManager.isDefaultDataSourceAvailable();
boolean available = PersistenceManager.isDataSourceAvailable("myDataSource");
```

#### 特性

- **线程安全**: 使用原子引用和并发缓存
- **多数据源支持**: 支持多个数据源的统一管理
- **自动缓存**: 自动缓存 EntityManagerFactory 实例
- **资源管理**: 提供完整的资源关闭方法

### 2. EntityManager

实体管理器，提供实体 CRUD 操作和事务管理。

#### 主要方法

```java
// CRUD操作
<T> T save(T entity);
<T> List<T> saveAll(List<T> entities);
<T> T update(T entity);
<T> void delete(T entity);
<T> void deleteById(Class<T> entityClass, Object id);
<T> T findById(Class<T> entityClass, Object id);

// 查询创建
<T> EntityQuery<T> createQuery(Class<T> entityClass);
<T> QueryBuilder<T> createQueryBuilder(Class<T> entityClass);

// 事务管理
EntityTransaction beginTransaction();
void commit();
void rollback();
boolean isTransactionActive();

// 原生SQL执行
<T> List<T> executeQuery(String sql, Class<T> resultClass, Object... params);
int executeUpdate(String sql, Object... params);
```

#### 特性

- **线程安全**: 使用 ThreadLocal 管理事务状态
- **自动事务**: 支持自动事务管理
- **连接管理**: 自动管理数据库连接
- **异常处理**: 完善的异常处理机制

### 3. EntityQuery

实体查询接口，提供简单的单表查询功能。

#### 主要方法

```java
// 条件查询
QueryCondition<T> where();
EntityQuery<T> where(Consumer<QueryCondition<T>> whereBuilder);

// 字段选择
EntityQuery<T> select(String... columns);
EntityQuery<T> select(Columnable<T, ?>... columns);
EntityQuery<T> selectAll();

// 排序
EntityQuery<T> orderBy(String column, boolean ascending);
EntityQuery<T> orderBy(Columnable<T, ?> column, boolean ascending);

// 分页
EntityQuery<T> limit(int offset, int size);

// 执行查询
List<T> findList();
T findFirst();
long count();
```

### 4. QueryBuilder

查询构建器，提供强大的查询构建能力。

#### 主要功能

- **SELECT子句**: 支持字段选择、聚合函数
- **FROM子句**: 支持表、子查询、JOIN
- **WHERE子句**: 支持复杂条件、子查询
- **GROUP BY**: 支持分组查询
- **HAVING**: 支持分组后过滤
- **ORDER BY**: 支持排序
- **窗口函数**: 支持窗口函数
- **CASE WHEN**: 支持条件表达式
- **表达式函数**: 支持自定义表达式
- **子查询**: 支持子查询
- **流式查询**: 支持大数据量流式处理

### 5. PersistenceQueryWrapper

查询包装器门面类，提供统一的查询接口。

#### 主要特性

- **多种查询策略**: BASIC、JOIN_AGGREGATE、BATCH
- **类型安全**: 支持 Lambda 表达式
- **链式调用**: 流畅的 API
- **分页支持**: 完整的分页功能

---

## 基础功能

### 1. 实体CRUD操作

#### 保存实体

```java
EntityManager em = PersistenceManager.getDefaultManager();

// 保存单个实体
User user = new User();
user.setName("John");
user.setEmail("john@example.com");
user = em.save(user);

// 批量保存
List<User> users = Arrays.asList(user1, user2, user3);
users = em.saveAll(users);
```

#### 更新实体

```java
// 更新实体
user.setName("John Updated");
user = em.update(user);
```

#### 删除实体

```java
// 删除实体
em.delete(user);

// 根据ID删除
em.deleteById(User.class, userId);
```

#### 查找实体

```java
// 根据ID查找
User user = em.findById(User.class, userId);
```

### 2. 简单查询

#### 基础查询

```java
EntityManager em = PersistenceManager.getDefaultManager();
EntityQuery<User> query = em.createQuery(User.class);

// 条件查询
query.where()
    .eq("status", "active")
    .gt("age", 18)
    .like("name", "%john%");

// 排序
query.orderBy("createTime", false);

// 分页
query.limit(0, 10);

// 执行查询
List<User> users = query.findList();
```

#### Lambda表达式查询

```java
EntityQuery<User> query = em.createQuery(User.class);

// 使用Lambda表达式（类型安全）
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%");

query.orderBy(User::getCreateTime, false);
List<User> users = query.findList();
```

#### 条件构建器模式

```java
// 使用Consumer构建条件
query.where(condition -> {
    condition.eq(User::getStatus, "active")
             .and()
             .gt(User::getAge, 18)
             .or()
             .like(User::getName, "%john%");
});

List<User> users = query.findList();
```

### 3. 查询条件

#### 比较条件

```java
query.where()
    .eq("status", "active")      // 等于
    .ne("status", "inactive")    // 不等于
    .gt("age", 18)               // 大于
    .ge("age", 18)               // 大于等于
    .lt("age", 65)               // 小于
    .le("age", 65);              // 小于等于
```

#### 集合条件

```java
// IN条件
query.where().in("status", "active", "pending", "completed");
query.where().in("status", Arrays.asList("active", "pending"));

// NOT IN条件
query.where().notIn("status", "deleted", "archived");
```

#### 字符串条件

```java
query.where()
    .like("name", "%john%")      // 模糊查询
    .isNull("description")        // 空值
    .isNotNull("description");    // 非空值
```

#### 范围条件

```java
query.where()
    .between("age", 18, 65)      // 范围查询
    .notBetween("age", 0, 17);   // 不在范围内
```

#### 逻辑条件

```java
// AND条件
query.where()
    .eq("status", "active")
    .and()
    .gt("age", 18);

// OR条件
query.where()
    .eq("status", "active")
    .or()
    .eq("status", "pending");

// 复杂逻辑
query.where(condition -> {
    condition.eq("status", "active")
             .and(andCondition -> {
                 andCondition.gt("age", 18)
                            .or()
                            .lt("age", 65);
             });
});
```

### 4. 排序和分页

#### 排序

```java
// 单字段排序
query.orderBy("createTime", false);  // 降序
query.orderBy("name", true);         // 升序

// Lambda表达式排序
query.orderBy(User::getCreateTime, false);

// 多字段排序（使用QueryBuilder）
QueryBuilder<User> qb = em.createQueryBuilder(User.class);
qb.selectAll()
  .from(User.class)
  .orderBy().desc(User::getCreateTime).asc(User::getName);
```

#### 分页

```java
// 简单分页
query.limit(0, 10);  // offset=0, size=10

// 使用PersistencePageRequest
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 10);
PersistencePage<User> page = query.findPage(pageRequest);

// 获取分页信息
List<User> data = page.getData();
long total = page.getTotal();
int size = page.getSize();
int pageIndex = page.getPageIndex();
boolean hasNext = page.hasNext();
boolean hasPrevious = page.hasPrevious();
```

### 5. 聚合查询

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 计数
long count = qb.aggregate().count(User::getId).findList().get(0);

// 求和
Number sum = qb.aggregate().sum(User::getAmount).findList().get(0);

// 平均值
Number avg = qb.aggregate().avg(User::getAmount).findList().get(0);

// 最大值
Number max = qb.aggregate().max(User::getAmount).findList().get(0);

// 最小值
Number min = qb.aggregate().min(User::getAmount).findList().get(0);

// 组合聚合
qb.aggregate()
  .count(User::getId, "total")
  .sum(User::getAmount, "total_amount")
  .avg(User::getAmount, "avg_amount");
```

---

## 高级功能

### 1. JOIN查询

#### 基本JOIN

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

qb.selectAll()
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .where()
    .eq("u.status", "active")
    .eq("d.status", "active");

List<User> users = qb.findList();
```

#### 多表JOIN

```java
qb.selectAll()
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .leftJoin(Role.class, "r")
  .onEq(User::getRoleId, Role::getId)
  .where()
    .eq("u.status", "active");
```

#### JOIN类型

```java
// INNER JOIN
qb.from(User.class, "u")
  .innerJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);

// LEFT JOIN
qb.from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);

// RIGHT JOIN
qb.from(User.class, "u")
  .rightJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);
```

### 2. 子查询

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 创建子查询
QueryBuilder<User> subQuery = qb.subquery();
subQuery.select(User.class)
        .from(User.class)
        .where().eq(User::getStatus, "active");

// 使用子查询
qb.selectAll()
  .from(subQuery, "active_users")
  .where().gt("active_users.age", 18);
```

### 3. 窗口函数

```java
QueryBuilder<Order> qb = em.createQueryBuilder(Order.class);

qb.selectAll()
  .window()
    .rowNumber()
    .partitionBy(Order::getUserId)
    .orderBy(Order::getCreateTime, false)
    .as("row_num")
  .from(Order.class)
  .where().eq(Order::getStatus, "completed");
```

### 4. CASE WHEN表达式

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

qb.selectAll()
  .caseWhen(User::getStatus)
    .when("active", "正常")
    .when("inactive", "停用")
    .elseValue("未知")
    .as("status_text")
  .from(User.class);
```

### 5. 表达式函数

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

qb.selectAll()
  .expression()
    .concat(User::getFirstName, " ", User::getLastName)
    .as("full_name")
  .from(User.class);
```

### 6. 分组查询

```java
QueryBuilder<Order> qb = em.createQueryBuilder(Order.class);

qb.select(Order::getUserId)
  .aggregate()
    .count(Order::getId, "order_count")
    .sum(Order::getAmount, "total_amount")
  .from(Order.class)
  .groupBy(Order::getUserId)
  .having()
    .gt("order_count", 10)
    .gt("total_amount", 1000);
```

### 7. 流式查询

流式查询适用于大数据量处理，避免内存溢出。

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 创建流式查询
StreamingQueryBuilder<User> streamingQuery = qb.streaming()
  .selectAll()
  .from(User.class)
  .where().eq(User::getStatus, "active");

// 流式处理
streamingQuery.stream()
  .forEach(user -> {
      // 处理每个用户
      processUser(user);
  });

// 分批处理
streamingQuery.stream()
  .limit(1000)
  .forEach(batch -> {
      // 处理每批数据
      processBatch(batch);
  });
```

---

## 查询构建器

### QueryBuilder 完整示例

```java
EntityManager em = PersistenceManager.getDefaultManager();
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 复杂查询
List<User> users = qb
  .select(User::getId, User::getName, User::getEmail)
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .where(where -> {
      where.eq("u.status", "active")
           .and()
           .gt("u.age", 18)
           .or(orCondition -> {
               orCondition.like("u.name", "%admin%")
                         .or()
                         .like("u.email", "%admin%");
           });
  })
  .groupBy(User::getDepartmentId)
  .having(having -> {
      having.gt("COUNT(u.id)", 10);
  })
  .orderBy().desc(User::getCreateTime).asc(User::getName)
  .limit(0, 20)
  .findList();
```

### PersistenceQueryWrapper 使用

```java
EntityManager em = PersistenceManager.getDefaultManager();
PersistenceQueryWrapper<User> wrapper = 
    new PersistenceQueryWrapper<>(User.class, em);

// 基础查询
List<User> users = wrapper
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%")
    .orderBy(User::getCreateTime, false)
    .findList();

// 分页查询
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 10);
PersistencePage<User> page = wrapper
    .eq(User::getStatus, "active")
    .findPage(pageRequest);

// 复杂查询（使用QueryBuilder）
wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.QUERY_BUILDER)
       .setQueryConfigurer(qb -> {
           qb.selectAll()
             .from(User.class, "u")
             .leftJoin(Department.class, "d")
             .onEq(User::getDepartmentId, Department::getId)
             .where().eq("u.status", "active");
       });

List<User> users = wrapper.findList();
```

---

## 性能优化

### 1. 查询缓存

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 获取查询缓存
QueryCache cache = qb.getQueryCache();

// 启用缓存
CacheConfig cacheConfig = new CacheConfig();
cacheConfig.setEnabled(true);
cacheConfig.setStrategy(CacheStrategy.LRU);
cacheConfig.setMaxSize(1000);
cacheConfig.setTtl(3600); // 1小时

// 使用缓存
List<User> users = qb.selectAll()
                     .from(User.class)
                     .where().eq(User::getStatus, "active")
                     .findList(); // 结果会被缓存
```

### 2. 性能监控

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 获取性能监控器
QueryPerformanceMonitor monitor = qb.getPerformanceMonitor();

// 执行查询
List<User> users = qb.findList();

// 获取性能指标
QueryMetrics metrics = qb.getPerformanceMetrics();
long executionTime = metrics.getExecutionTime();
long rowCount = metrics.getRowCount();
String sql = metrics.getSql();

// 获取统计信息
QueryStatistics stats = monitor.getStatistics();
long totalQueries = stats.getTotalQueries();
long slowQueries = stats.getSlowQueries();
double avgExecutionTime = stats.getAverageExecutionTime();
```

### 3. 慢查询日志

```java
// 配置慢查询日志
PersistenceConfig config = PersistenceConfig.getDevelopmentConfig();
config.setSlowQueryLogging(true);
config.setSlowQueryThreshold(500); // 500ms

// 慢查询会自动记录到日志
```

### 4. 批量操作

```java
// 批量保存
List<User> users = Arrays.asList(user1, user2, user3, ...);
users = em.saveAll(users); // 比循环save更高效

// 批量更新
users = em.updateAll(users);
```

### 5. 流式处理

```java
// 大数据量流式处理
StreamingQueryBuilder<User> streamingQuery = 
    em.createQueryBuilder(User.class).streaming();

streamingQuery.selectAll()
              .from(User.class)
              .where().eq(User::getStatus, "active")
              .stream()
              .forEach(user -> {
                  // 处理每个用户，避免一次性加载所有数据
                  processUser(user);
              });
```

---

## 最佳实践

### 1. 实体类设计

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    // getters and setters
}
```

### 2. 查询构建

```java
// ✅ 推荐：使用Lambda表达式，类型安全
EntityQuery<User> query = em.createQuery(User.class);
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18);

// ❌ 不推荐：使用字符串，容易出错
query.where()
    .eq("status", "active")
    .gt("age", 18);
```

### 3. 事务管理

```java
// ✅ 推荐：使用try-with-resources或显式管理
EntityTransaction tx = em.beginTransaction();
try {
    em.save(user1);
    em.save(user2);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
}

// ✅ 推荐：使用自动事务（简单操作）
User user = em.save(user); // 自动提交
```

### 4. 异常处理

```java
try {
    User user = em.findById(User.class, userId);
    if (user == null) {
        throw new EntityNotFoundException("User not found: " + userId);
    }
    return user;
} catch (Exception e) {
    logger.error("Failed to find user: " + userId, e);
    throw new PersistenceException("Database error", e);
}
```

### 5. 性能优化

```java
// ✅ 推荐：使用批量操作
List<User> users = em.saveAll(userList);

// ❌ 不推荐：循环单个保存
for (User user : userList) {
    em.save(user); // 多次数据库交互
}

// ✅ 推荐：使用分页查询
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 20);
PersistencePage<User> page = query.findPage(pageRequest);

// ❌ 不推荐：一次性查询所有数据
List<User> allUsers = query.findList(); // 可能内存溢出
```

---

## 故障排除

### 常见问题

#### 1. 连接问题

```java
// 检查数据源配置
boolean available = PersistenceManager.isDefaultDataSourceAvailable();
if (!available) {
    // 数据源未配置
}

// 检查数据源名称
String dataSourceName = Defaults.getDataSourceName();
```

#### 2. 事务问题

```java
// 检查事务状态
boolean active = em.isTransactionActive();
if (!active) {
    // 没有活动的事务
}

// 检查事务是否已提交
EntityTransaction tx = em.beginTransaction();
// ... 操作
if (tx.isActive()) {
    tx.commit();
}
```

#### 3. 查询问题

```java
// 获取生成的SQL（调试用）
QueryBuilder<User> qb = em.createQueryBuilder(User.class);
String sql = qb.selectAll()
               .from(User.class)
               .where().eq(User::getStatus, "active")
               .getGeneratedSql();
logger.debug("Generated SQL: {}", sql);
```

#### 4. 性能问题

```java
// 启用性能监控
QueryPerformanceMonitor monitor = qb.getPerformanceMonitor();

// 检查慢查询
QueryStatistics stats = monitor.getStatistics();
List<SlowQueryInfo> slowQueries = stats.getSlowQueries();
for (SlowQueryInfo info : slowQueries) {
    logger.warn("Slow query: {}ms - {}", 
                info.getExecutionTime(), 
                info.getSql());
}
```

---

## API参考

### PersistenceManager

| 方法 | 说明 |
|------|------|
| `getDefaultManager()` | 获取默认EntityManager |
| `getManager(String dataSourceName)` | 获取指定数据源的EntityManager |
| `shutdown()` | 关闭默认管理器 |
| `shutdown(String dataSourceName)` | 关闭指定数据源的管理器 |
| `shutdownAll()` | 关闭所有管理器 |
| `isDefaultDataSourceAvailable()` | 检查默认数据源是否可用 |
| `isDataSourceAvailable(String name)` | 检查指定数据源是否可用 |

### EntityManager

| 方法 | 说明 |
|------|------|
| `save(T entity)` | 保存实体 |
| `saveAll(List<T> entities)` | 批量保存 |
| `update(T entity)` | 更新实体 |
| `delete(T entity)` | 删除实体 |
| `deleteById(Class<T> clazz, Object id)` | 根据ID删除 |
| `findById(Class<T> clazz, Object id)` | 根据ID查找 |
| `createQuery(Class<T> clazz)` | 创建查询 |
| `createQueryBuilder(Class<T> clazz)` | 创建查询构建器 |
| `beginTransaction()` | 开始事务 |
| `executeQuery(String sql, Class<T> clazz, Object... params)` | 执行查询 |
| `executeUpdate(String sql, Object... params)` | 执行更新 |

### EntityQuery

| 方法 | 说明 |
|------|------|
| `where()` | 创建查询条件 |
| `where(Consumer<QueryCondition<T>>)` | 条件构建器 |
| `select(String... columns)` | 选择字段 |
| `select(Columnable<T, ?>... columns)` | 选择字段（Lambda） |
| `selectAll()` | 选择所有字段 |
| `orderBy(String column, boolean ascending)` | 排序 |
| `limit(int offset, int size)` | 分页 |
| `findList()` | 查询列表 |
| `findFirst()` | 查询第一条 |
| `count()` | 计数 |

### QueryBuilder

| 方法 | 说明 |
|------|------|
| `select()` | SELECT子句 |
| `from(Class<T> clazz)` | FROM子句 |
| `where(Consumer<WhereClause<T>>)` | WHERE子句 |
| `leftJoin(Class<?> clazz, String alias)` | LEFT JOIN |
| `innerJoin(Class<?> clazz, String alias)` | INNER JOIN |
| `groupBy(Columnable<T, ?> column)` | GROUP BY |
| `having(Consumer<HavingClause<T>>)` | HAVING子句 |
| `orderBy()` | ORDER BY子句 |
| `aggregate()` | 聚合函数 |
| `window()` | 窗口函数 |
| `caseWhen()` | CASE WHEN表达式 |
| `limit(int offset, int size)` | 分页 |
| `findList()` | 查询列表 |
| `findFirst()` | 查询第一条 |
| `count()` | 计数 |
| `getGeneratedSql()` | 获取生成的SQL |

---

## 总结

Persistence 模块提供了完整、强大、易用的持久化功能：

- ✅ **完整的CRUD操作**: 支持实体的保存、更新、删除、查询
- ✅ **强大的查询能力**: 从简单查询到复杂JOIN查询
- ✅ **类型安全**: Lambda表达式支持，编译时类型检查
- ✅ **性能优化**: 查询缓存、性能监控、慢查询日志
- ✅ **灵活的分页**: 多种分页策略，适应不同场景
- ✅ **流式处理**: 支持大数据量流式处理
- ✅ **事务管理**: 完整的事务支持
- ✅ **多数据源**: 支持多个数据源的统一管理

通过本指南，您应该能够充分利用 Persistence 模块的强大功能，构建高效、可靠的持久化应用。

---

**文档维护**: 如有问题或建议，请联系 Portal Team

