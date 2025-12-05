package com.kishultan.persistence.orm.query.impl;

import com.kishultan.persistence.orm.query.*;
import com.kishultan.persistence.orm.Columnable;
import com.kishultan.persistence.orm.ColumnabledLambda;
import com.kishultan.persistence.orm.query.ClauseBuilder;
import com.kishultan.persistence.orm.query.context.ClauseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 表达式函数子句实现类
 */
public class ExpressionClauseImpl<T> extends SelectClauseImpl<T> implements ExpressionClause<T>, ClauseBuilder<T> {
    
    // 表达式函数字段
    private final List<String> expressionFunctions = new ArrayList<>();
    
    public ExpressionClauseImpl(QueryBuilder<T> queryBuilder) {
        super(queryBuilder);
    }
    
    // ==================== 时间函数实现 ====================
    
    @Override
    public ExpressionClause<T> year(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("YEAR(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> month(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("MONTH(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> day(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("DAY(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> hour(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("HOUR(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> minute(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("MINUTE(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> second(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("SECOND(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> add(Columnable<T, ?> field, String unit, int value, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("DATE_ADD(" + qualifiedField + ", INTERVAL " + value + " " + unit + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> sub(Columnable<T, ?> field, String unit, int value, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("DATE_SUB(" + qualifiedField + ", INTERVAL " + value + " " + unit + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> diff(Columnable<T, ?> field1, Columnable<T, ?> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("DATEDIFF(" + qualifiedField1 + ", " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> now(String alias) {
        expressionFunctions.add("NOW() AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> current(String alias) {
        expressionFunctions.add("CURRENT_TIMESTAMP AS " + alias);
        return this;
    }
    
    // ==================== 字符串函数实现 ====================
    
    @Override
    public ExpressionClause<T> upper(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("UPPER(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> lower(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("LOWER(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> trim(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("TRIM(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> ltrim(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("LTRIM(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> rtrim(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("RTRIM(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> length(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("LENGTH(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> charLength(Columnable<T, String> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("CHAR_LENGTH(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> replace(Columnable<T, String> field, String oldStr, String newStr, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("REPLACE(" + qualifiedField + ", '" + oldStr + "', '" + newStr + "') AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> substring(Columnable<T, String> field, int start, int length, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("SUBSTRING(" + qualifiedField + ", " + start + ", " + length + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> concat(String alias, Object... values) {
        StringBuilder concatExpr = new StringBuilder("CONCAT(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                concatExpr.append(", ");
            }
            if (values[i] instanceof String) {
                concatExpr.append("'").append(values[i]).append("'");
            } else {
                concatExpr.append(values[i]);
            }
        }
        concatExpr.append(") AS ").append(alias);
        expressionFunctions.add(concatExpr.toString());
        return this;
    }
    
    @Override
    public ExpressionClause<T> position(Columnable<T, String> field1, Columnable<T, String> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("POSITION(" + qualifiedField1 + " IN " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    // ==================== 数学函数实现 ====================
    
    @Override
    public ExpressionClause<T> abs(Columnable<T, Number> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("ABS(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> ceil(Columnable<T, Number> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("CEIL(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> floor(Columnable<T, Number> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("FLOOR(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> round(Columnable<T, Number> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("ROUND(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> round(Columnable<T, Number> field, int decimals, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("ROUND(" + qualifiedField + ", " + decimals + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> mod(Columnable<T, Number> field, int divisor, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("MOD(" + qualifiedField + ", " + divisor + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> power(Columnable<T, Number> field, int exponent, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("POWER(" + qualifiedField + ", " + exponent + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> sqrt(Columnable<T, Number> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        expressionFunctions.add("SQRT(" + qualifiedField + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> add(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("(" + qualifiedField1 + " + " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> subtract(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("(" + qualifiedField1 + " - " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> multiply(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("(" + qualifiedField1 + " * " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    @Override
    public ExpressionClause<T> divide(Columnable<T, Number> field1, Columnable<T, Number> field2, String alias) {
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField1 = currentTableAlias != null ? currentTableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = currentTableAlias != null ? currentTableAlias + "." + fieldName2 : fieldName2;
        expressionFunctions.add("(" + qualifiedField1 + " / " + qualifiedField2 + ") AS " + alias);
        return this;
    }
    
    // ==================== FROM子句 ====================
    
    /*@Override
    public FromClause<T> from() {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(Class<T> entityClass) {
        String tableName = EntityUtils.getTableName(entityClass);
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, entityClass, tableName, tableName);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(String tableName) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, tableName, tableName);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }
    
    @Override
    public FromClause<T> from(String tableName, String alias) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(queryBuilder, tableName, alias);
        if (queryBuilder instanceof StandardQueryBuilder) {
            ((StandardQueryBuilder<T>) queryBuilder).setFromClause(fromClause);
        }
        return fromClause;
    }*/
    
    // ==================== 构建子句 ====================
    
    @Override
    public ClauseResult buildClause() {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        if (expressionFunctions.isEmpty()) {
            sql.append("*");
        } else {
            for (int i = 0; i < expressionFunctions.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(expressionFunctions.get(i));
            }
        }
        
        return new ClauseResult(sql.toString(), parameters);
    }
    
    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    @Override
    public String toFunctionSql() {
        if (expressionFunctions.isEmpty()) {
            return "1";
        }
        return String.join(", ", expressionFunctions);
    }
    
    // ==================== 辅助方法 ====================
    
    private String getCurrentTableAlias() {
        if (queryBuilder instanceof StandardQueryBuilder) {
            StandardQueryBuilder<T> qb = (StandardQueryBuilder<T>) queryBuilder;
            return qb.getCurrentTableAlias();
        }
        return null;
    }
}
