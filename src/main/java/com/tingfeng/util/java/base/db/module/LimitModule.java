package com.tingfeng.util.java.base.db.module;

/**
 * LIMIT clause module for SQL builder.
 */
public class LimitModule implements SqlModule {

    private static final String TYPE = "LIMIT";
    private Integer limit;
    private Integer offset;

    public LimitModule() {
    }

    public LimitModule limit(int limit) {
        this.limit = limit;
        return this;
    }

    public LimitModule offset(int offset) {
        this.offset = offset;
        return this;
    }

    public LimitModule limit(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        if (limit == null && offset == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("LIMIT ");
        if (offset != null) {
            sb.append(offset).append(", ");
        }
        sb.append(limit != null ? limit : "18446744073709551615");
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return limit != null || offset != null;
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }
}
