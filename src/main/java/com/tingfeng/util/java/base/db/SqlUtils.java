package com.tingfeng.util.java.base.db;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tingfeng.util.java.base.db.module.FromModule;
import com.tingfeng.util.java.base.db.module.LimitModule;
import com.tingfeng.util.java.base.db.module.OrderByModule;
import com.tingfeng.util.java.base.db.module.PageModule;
import com.tingfeng.util.java.base.db.module.SelectModule;
import com.tingfeng.util.java.base.db.module.SqlModule;
import com.tingfeng.util.java.base.db.module.WhereModule;

/**
 * SQL utility class with builder pattern support.
 * Provides SQL validation and parameterized query building.
 */
public class SqlUtils {

    public static final String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
            "char|declare|sitename|net user|xp_cmdshell|;|-|or|+|,|like'|and|exec|execute|insert|create|drop|" +
            "table|from|grant|use|group_concat|column_name|" +
            "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
            "chr|mid|master|truncate|char|declare|or|;|--|+|,|like|//|/|%|#";

    /**
     * Regex patterns for detecting obvious SQL injection patterns.
     */
    private static final Pattern[] INJECTION_PATTERNS = {
            Pattern.compile("(\\bunion\\b.*\\bselect\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bselect\\b.*\\bfrom\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\binsert\\b.*\\binto\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bupdate\\b.*\\bset\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bdelete\\b.*\\bfrom\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bdrop\\b.*\\btable\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(--\\s*$)"),
            Pattern.compile("(/\\*.*\\*/)"),
            Pattern.compile("(\\bexec\\b|\\bexecute\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bxp_cmdshell\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bor\\b.*=.*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\band\\b.*=.*)", Pattern.CASE_INSENSITIVE),
    };

    public SqlUtils() {

    }

    /**
     * Check if string contains SQL keywords.
     * @param str the string to check
     * @return true if SQL keywords found
     */
    public static boolean sqlValidate(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        str = str.toLowerCase();
        String[] badStrs = badStr.split("\\|");
        String[] values = str.split("\\s");
        for (int i = 0; i < badStrs.length; i++) {
            for (String v : values) {
                if (v.equals(badStrs[i])) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Detect obvious SQL injection patterns using regex.
     *
     * @param str the string to check
     * @return true if injection pattern detected
     */
    public static boolean detectInjection(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(str).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate input for SQL injection, combining both keyword and pattern detection.
     *
     * @param str the string to validate
     * @return true if potential injection detected
     */
    public static boolean isSafeInput(String str) {
        return !sqlValidate(str) && !detectInjection(str);
    }

    /**
     * DTO to encapsulate built SQL and its parameters.
     */
    public static class SqlWithParams {
        private final String sql;
        private final List<Object> params;

        public SqlWithParams(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params != null ? new ArrayList<>(params) : new ArrayList<>();
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getParams() {
            return new ArrayList<>(params);
        }

        public Object[] getParamsArray() {
            return params.toArray();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SQL: ").append(sql);
            if (!params.isEmpty()) {
                sb.append("\nParams: ").append(params);
            }
            return sb.toString();
        }
    }

    /**
     * SQL Builder with chain pattern support.
     * Provides fluent API for building parameterized SQL queries.
     */
    public static class SqlBuilder {
        private final SelectModule selectModule;
        private final FromModule fromModule;
        private final WhereModule whereModule;
        private final OrderByModule orderByModule;
        private final LimitModule limitModule;
        private final PageModule pageModule;

        public SqlBuilder() {
            this.selectModule = new SelectModule();
            this.fromModule = new FromModule();
            this.whereModule = new WhereModule();
            this.orderByModule = new OrderByModule();
            this.limitModule = new LimitModule();
            this.pageModule = new PageModule();
        }

        public SelectModule select() {
            return selectModule;
        }

        public FromModule from() {
            return fromModule;
        }

        public WhereModule where() {
            return whereModule;
        }

        public OrderByModule orderBy() {
            return orderByModule;
        }

        public LimitModule limit() {
            return limitModule;
        }

        public PageModule page() {
            return pageModule;
        }

        /**
         * Build the final SQL with parameters.
         *
         * @return SqlWithParams containing SQL and parameter list
         * @throws IllegalStateException if SELECT or FROM is missing
         */
        public SqlWithParams build() {
            if (!selectModule.isValid()) {
                throw new IllegalStateException("SELECT clause is required");
            }
            if (!fromModule.isValid()) {
                throw new IllegalStateException("FROM clause is required");
            }

            List<String> sqlParts = new ArrayList<>();
            List<Object> allParams = new ArrayList<>();

            String selectSql = selectModule.build();
            if (!selectSql.isEmpty()) {
                sqlParts.add(selectSql);
            }

            String fromSql = fromModule.build();
            if (!fromSql.isEmpty()) {
                sqlParts.add(fromSql);
            }

            String whereSql = whereModule.build();
            if (!whereSql.isEmpty()) {
                sqlParts.add(whereSql);
                for (Object param : whereModule.getParams()) {
                    allParams.add(param);
                }
            }

            String orderBySql = orderByModule.build();
            if (!orderBySql.isEmpty()) {
                sqlParts.add(orderBySql);
            }

            String limitSql = limitModule.build();
            if (!limitSql.isEmpty()) {
                sqlParts.add(limitSql);
            }

            String pageSql = pageModule.build();
            if (!pageSql.isEmpty()) {
                sqlParts.add(pageSql);
            }

            String finalSql = String.join(" ", sqlParts);
            return new SqlWithParams(finalSql, allParams);
        }

        /**
         * Reset the builder to initial state.
         *
         * @return this builder for chaining
         */
        public SqlBuilder reset() {
            selectModule.columns();
            fromModule.table(null);
            whereModule.rawCondition(null);
            orderByModule.asc(null);
            limitModule.limit(0);
            pageModule.page(1, 10);
            return this;
        }
    }

    /**
     * Create a new SqlBuilder instance.
     *
     * @return new SqlBuilder
     */
    public static SqlBuilder builder() {
        return new SqlBuilder();
    }
}
