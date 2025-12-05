package com.kishultan.persistence.orm.query;

import com.kishultan.persistence.orm.Columnable;

/**
 * 表达式函数子句接口
 * 提供表达式函数功能，包括时间、字符串、数学函数等
 */
public interface ExpressionClause<T> extends SelectClause<T>, FunctionQuery {
    
    // ==================== 时间函数 ====================
    
    /**
     * 年份提取
     */
    ExpressionClause<T> year(Columnable<T, ?> field, String alias);
    
    /**
     * 月份提取
     */
    ExpressionClause<T> month(Columnable<T, ?> field, String alias);
    
    /**
     * 日期提取
     */
    ExpressionClause<T> day(Columnable<T, ?> field, String alias);
    
    /**
     * 小时提取
     */
    ExpressionClause<T> hour(Columnable<T, ?> field, String alias);
    
    /**
     * 分钟提取
     */
    ExpressionClause<T> minute(Columnable<T, ?> field, String alias);
    
    /**
     * 秒提取
     */
    ExpressionClause<T> second(Columnable<T, ?> field, String alias);
    
    /**
     * 日期加法
     */
    ExpressionClause<T> add(Columnable<T, ?> field, String unit, int value, String alias);
    
    /**
     * 日期减法
     */
    ExpressionClause<T> sub(Columnable<T, ?> field, String unit, int value, String alias);
    
    /**
     * 日期差
     */
    ExpressionClause<T> diff(Columnable<T, ?> field1, Columnable<T, ?> field2, String alias);
    
    /**
     * 当前时间
     */
    ExpressionClause<T> now(String alias);
    
    /**
     * 当前时间戳
     */
    ExpressionClause<T> current(String alias);
    
    // ==================== 字符串函数 ====================
    
    /**
     * 转大写
     */
    ExpressionClause<T> upper(Columnable<T, String> field, String alias);
    
    /**
     * 转小写
     */
    ExpressionClause<T> lower(Columnable<T, String> field, String alias);
    
    /**
     * 修剪
     */
    ExpressionClause<T> trim(Columnable<T, String> field, String alias);
    
    /**
     * 左修剪
     */
    ExpressionClause<T> ltrim(Columnable<T, String> field, String alias);
    
    /**
     * 右修剪
     */
    ExpressionClause<T> rtrim(Columnable<T, String> field, String alias);
    
    /**
     * 字符串长度
     */
    ExpressionClause<T> length(Columnable<T, String> field, String alias);
    
    /**
     * 字符长度
     */
    ExpressionClause<T> charLength(Columnable<T, String> field, String alias);
    
    /**
     * 字符串替换
     */
    ExpressionClause<T> replace(Columnable<T, String> field, String oldStr, String newStr, String alias);
    
    /**
     * 子字符串
     */
    ExpressionClause<T> substring(Columnable<T, String> field, int start, int length, String alias);
    
    /**
     * 字符串连接
     */
    ExpressionClause<T> concat(String alias, Object... values);
    
    /**
     * 字符串位置
     */
    ExpressionClause<T> position(Columnable<T, String> field1, Columnable<T, String> field2, String alias);
    
    // ==================== 数学函数 ====================
    
    /**
     * 绝对值
     */
    ExpressionClause<T> abs(Columnable<T, Number> field, String alias);
    
    /**
     * 向上取整
     */
    ExpressionClause<T> ceil(Columnable<T, Number> field, String alias);
    
    /**
     * 向下取整
     */
    ExpressionClause<T> floor(Columnable<T, Number> field, String alias);
    
    /**
     * 四舍五入
     */
    ExpressionClause<T> round(Columnable<T, Number> field, String alias);
    
    /**
     * 四舍五入（指定小数位）
     */
    ExpressionClause<T> round(Columnable<T, Number> field, int decimals, String alias);
    
    /**
     * 取模
     */
    ExpressionClause<T> mod(Columnable<T, Number> field, int divisor, String alias);
    
    /**
     * 幂运算
     */
    ExpressionClause<T> power(Columnable<T, Number> field, int exponent, String alias);
    
    /**
     * 平方根
     */
    ExpressionClause<T> sqrt(Columnable<T, Number> field, String alias);
    
    /**
     * 加法
     */
    ExpressionClause<T> add(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias);
    
    /**
     * 减法
     */
    ExpressionClause<T> subtract(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias);
    
    /**
     * 乘法
     */
    ExpressionClause<T> multiply(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias);
    
    /**
     * 除法
     */
    ExpressionClause<T> divide(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias);
    
    // ==================== FROM子句 ====================
    
//    /**
//     * 指定FROM子句
//     */
//    FromClause<T> from();
//
//    /**
//     * 指定FROM子句（实体类）
//     */
//    FromClause<T> from(Class<T> entityClass);
//
//    /**
//     * 指定FROM子句（表名）
//     */
//    FromClause<T> from(String tableName);
//
//    /**
//     * 指定FROM子句（表名+别名）
//     */
//    FromClause<T> from(String tableName, String alias);
}
