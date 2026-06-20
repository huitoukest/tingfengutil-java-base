package com.tingfeng.util.java.base.db.module;

import java.util.ArrayList;
import java.util.List;

/**
 * WHERE clause module for SQL builder.
 * Supports parameterized queries to prevent SQL injection.
 */
public class WhereModule implements SqlModule {

    private static final String TYPE = "WHERE";
    private final List<String> conditions;
    private final List<Object> params;
    private String logicalOperator;

    public WhereModule() {
        this.conditions = new ArrayList<>();
        this.params = new ArrayList<>();
        this.logicalOperator = "AND";
    }

    public WhereModule and() {
        this.logicalOperator = "AND";
        return this;
    }

    public WhereModule or() {
        this.logicalOperator = "OR";
        return this;
    }

    /**
     * Add a condition with a placeholder for parameterization.
     *
     * @param condition the SQL condition with '?' placeholder
     * @param value     the parameter value
     * @return this instance for chaining
     */
    public WhereModule condition(String condition, Object value) {
        if (condition != null && !condition.trim().isEmpty()) {
            conditions.add(condition.trim());
            if (value != null) {
                params.add(value);
            }
        }
        return this;
    }

    /**
     * Add a raw condition without parameterization.
     *
     * @param condition the raw SQL condition
     * @return this instance for chaining
     */
    public WhereModule rawCondition(String condition) {
        if (condition != null && !condition.trim().isEmpty()) {
            conditions.add(condition.trim());
        }
        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        sb.append(conditions.get(0));
        for (int i = 1; i < conditions.size(); i++) {
            sb.append(" ").append(logicalOperator).append(" ");
            sb.append(conditions.get(i));
        }
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return !conditions.isEmpty();
    }

    @Override
    public Object[] getParams() {
        return params.toArray();
    }

    public List<String> getConditions() {
        return new ArrayList<>(conditions);
    }
}
