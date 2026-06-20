package com.tingfeng.util.java.base.db.module;

import java.util.ArrayList;
import java.util.List;

/**
 * FROM clause module for SQL builder.
 */
public class FromModule implements SqlModule {

    private static final String TYPE = "FROM";
    private final List<String> tables;
    private final List<Object> params;

    public FromModule() {
        this.tables = new ArrayList<>();
        this.params = new ArrayList<>();
    }

    public FromModule table(String table) {
        if (table != null && !table.trim().isEmpty()) {
            tables.add(table.trim());
        }
        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        if (tables.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FROM ");
        sb.append(String.join(", ", tables));
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return !tables.isEmpty();
    }

    @Override
    public Object[] getParams() {
        return params.toArray();
    }
}
