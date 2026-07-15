package com.tingfeng.util.java.base.db.module;

import java.util.ArrayList;
import java.util.List;

/**
 * ORDER BY clause module for SQL builder.
 */
public class OrderByModule implements SqlModule {

    private static final String TYPE = "ORDER BY";
    private final List<String> orderColumns;
    private final List<Object> params;

    public OrderByModule() {
        this.orderColumns = new ArrayList<>();
        this.params = new ArrayList<>();
    }

    /**
     * Add an order by column.
     *
     * @param column the column name
     * @param asc    true for ascending, false for descending
     * @return this instance for chaining
     */
    public OrderByModule addOrder(String column, boolean asc) {
        if (column != null && !column.trim().isEmpty()) {
            orderColumns.add(column.trim() + (asc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * Add an ascending order by column.
     *
     * @param column the column name
     * @return this instance for chaining
     */
    public OrderByModule asc(String column) {
        return addOrder(column, true);
    }

    /**
     * Add a descending order by column.
     *
     * @param column the column name
     * @return this instance for chaining
     */
    public OrderByModule desc(String column) {
        return addOrder(column, false);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        if (orderColumns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ORDER BY ");
        sb.append(String.join(", ", orderColumns));
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return !orderColumns.isEmpty();
    }

    @Override
    public Object[] getParams() {
        return params.toArray();
    }
}
