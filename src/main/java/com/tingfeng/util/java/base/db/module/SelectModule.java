package com.tingfeng.util.java.base.db.module;

import java.util.ArrayList;
import java.util.List;

/**
 * SELECT clause module for SQL builder.
 */
public class SelectModule implements SqlModule {

    private static final String TYPE = "SELECT";
    private final List<String> columns;
    private boolean distinct;

    public SelectModule() {
        this.columns = new ArrayList<>();
        this.distinct = false;
    }

    public SelectModule columns(String... cols) {
        for (String col : cols) {
            if (col != null && !col.trim().isEmpty()) {
                columns.add(col.trim());
            }
        }
        return this;
    }

    public SelectModule distinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        if (columns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (distinct) {
            sb.append("DISTINCT ");
        }
        sb.append(String.join(", ", columns));
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return !columns.isEmpty();
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }
}
