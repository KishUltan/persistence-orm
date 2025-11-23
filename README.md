# Persistence ORM

一个轻量级、类型安全的ORM框架，提供强大的查询构建器、流式查询支持和性能监控功能。

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-1.8+-green.svg)](https://www.oracle.com/java/)

## ✨ 特性

- 🚀 **轻量级**: 基于SansOrm，无复杂依赖
- 🔒 **类型安全**: 支持Lambda表达式，编译时类型检查
- 📊 **强大的查询能力**: 支持复杂查询、JOIN、聚合、窗口函数等
- 🌊 **流式查询**: 支持大数据量流式处理，避免内存溢出
- 📈 **性能监控**: 内置性能监控和慢查询日志
- 💾 **查询缓存**: 支持LRU和TTL缓存策略
- 🔄 **事务管理**: 完整的事务支持，线程安全
- 📦 **多数据源**: 支持多个数据源的统一管理
- 🎯 **零配置**: 开箱即用，无需复杂配置

## 📦 Maven依赖

```xml
<dependency>
    <groupId>com.kishultan</groupId>
    <artifactId>persistence-orm</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 快速开始

### 1. 配置数据源

```java
import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.config.PersistenceDefaults;

// 设置默认数据源名称
PersistenceDefaults.setDataSourceName("default");

// 添加数据源（使用DriverManager，不依赖特定连接池）
DataSource dataSource = new SimpleDataSource(); // 或使用HikariCP、Druid等
dataSource.setUrl("jdbc:mysql://localhost/test");
dataSource.setUser("root");
dataSource.setPassword("password");

DataSourceManager.addLocalDataSource("default", dataSource);
```

### 2. 基本CRUD操作

```java
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.orm.EntityManager;

// 获取EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 保存实体
User user = new User();
user.setName("John");
user.setEmail("john@example.com");
user = em.save(user);

// 查询实体
User found = em.findById(User.class, userId);

// 更新实体
user.setName("John Updated");
user = em.update(user);

// 删除实体
em.delete(user);
```

### 3. 查询构建

```java
import com.kishultan.persistence.orm.EntityQuery;

// 创建查询
EntityQuery<User> query = em.createQuery(User.class);

// 条件查询（类型安全）
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%");

// 排序和分页
query.orderBy(User::getCreateTime, false)
     .limit(0, 10);

// 执行查询
List<User> users = query.findList();
```

### 4. 复杂查询

```java
import com.kishultan.persistence.orm.query.QueryBuilder;

QueryBuilder<User> qb = em.createQueryBuilder(User.class);

List<User> users = qb
    .selectAll()
    .from(User.class, "u")
    .leftJoin(Department.class, "d")
    .onEq(User::getDepartmentId, Department::getId)
    .where(where -> {
        where.eq("u.status", "active")
             .and()
             .gt("u.age", 18);
    })
    .orderBy().desc(User::getCreateTime)
    .limit(0, 20)
    .findList();
```

### 5. 聚合查询

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 计数
long count = qb.aggregate()
    .count(User::getId)
    .findList()
    .get(0);

// 求和
Number sum = qb.aggregate()
    .sum(User::getAmount)
    .findList()
    .get(0);
```

### 6. 流式查询（大数据量处理）

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
        // 处理每个用户，避免内存溢出
        processUser(user);
    });
```

## 📚 完整文档

详细的文档请参考：[Persistence完整指南](docs/PERSISTENCE_COMPLETE_GUIDE.md)

文档包含：
- 架构设计
- 核心组件详解
- 基础功能示例
- 高级功能（JOIN、子查询、窗口函数等）
- 性能优化
- 最佳实践
- 故障排除
- API参考

## 🎯 核心组件

### PersistenceManager
持久化管理器，提供统一的持久化操作入口。

```java
// 获取默认EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 获取指定数据源的EntityManager
EntityManager em = PersistenceManager.getManager("myDataSource");
```

### EntityManager
实体管理器，提供CRUD操作和事务管理。

```java
// CRUD操作
User user = em.save(user);
user = em.update(user);
em.delete(user);
User found = em.findById(User.class, id);

// 事务管理
EntityTransaction tx = em.beginTransaction();
try {
    em.save(user1);
    em.save(user2);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
}
```

### EntityQuery
简单查询接口，适用于单表查询。

```java
EntityQuery<User> query = em.createQuery(User.class);
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .orderBy(User::getCreateTime, false)
    .limit(0, 10);
List<User> users = query.findList();
```

### QueryBuilder
强大的查询构建器，支持复杂查询。

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);
qb.selectAll()
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .where().eq("u.status", "active")
  .groupBy(User::getDepartmentId)
  .orderBy().desc(User::getCreateTime)
  .limit(0, 20);
List<User> users = qb.findList();
```

## 🔧 配置

### 数据源配置

```java
// 设置默认数据源名称
PersistenceDefaults.setDataSourceName("default");

// 添加数据源
DataSourceManager.addLocalDataSource("default", dataSource);

// 设置数据源类型（用于方言支持）
DataSourceManager.addDataSourceFlavor("default", "mysql");
```

### 性能监控

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 获取性能监控器
QueryPerformanceMonitor monitor = qb.getPerformanceMonitor();

// 执行查询后获取指标
List<User> users = qb.findList();
QueryMetrics metrics = qb.getPerformanceMetrics();
long executionTime = metrics.getExecutionTime();
```

### 查询缓存

```java
QueryBuilder<User> qb = em.createQueryBuilder(User.class);

// 获取查询缓存
QueryCache cache = qb.getQueryCache();

// 启用缓存（通过配置）
// 查询结果会自动缓存
List<User> users = qb.findList();
```

## 📊 支持的功能

### 查询功能
- ✅ 简单查询（单表）
- ✅ 复杂查询（多表JOIN）
- ✅ 子查询
- ✅ 聚合函数（COUNT、SUM、AVG、MAX、MIN）
- ✅ 窗口函数（ROW_NUMBER、RANK等）
- ✅ CASE WHEN表达式
- ✅ 表达式函数（CONCAT、SUBSTRING等）
- ✅ 分组查询（GROUP BY、HAVING）
- ✅ 流式查询（大数据量处理）

### 条件支持
- ✅ 比较条件（=、!=、>、>=、<、<=）
- ✅ 集合条件（IN、NOT IN）
- ✅ 字符串条件（LIKE、IS NULL、IS NOT NULL）
- ✅ 范围条件（BETWEEN、NOT BETWEEN）
- ✅ 逻辑条件（AND、OR）
- ✅ 复杂嵌套条件

### 数据库支持
- ✅ MySQL
- ✅ H2（测试）
- ✅ 其他支持JDBC的数据库

## 🧪 测试

```bash
mvn test
```

## 📄 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

- Email: team@kishultan.com
- GitHub: https://github.com/kishultan/persistence-orm

## 📝 更新日志

### 1.0.0-SNAPSHOT
- ✅ 初始版本
- ✅ 完整的CRUD操作
- ✅ 强大的查询构建器
- ✅ 流式查询支持
- ✅ 性能监控
- ✅ 查询缓存
- ✅ 多数据源支持

## 🔗 相关链接

- [完整功能指南](docs/PERSISTENCE_COMPLETE_GUIDE.md)

---

**注意**: 本项目已从 Portal 项目中提取，保持 `com.kishultan.persistence.*` 包名，可独立使用。
