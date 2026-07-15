package com.tingfeng.util.java.base.db.module;

/**
 * PAGE clause module for SQL builder.
 * Provides pagination support with page number and page size.
 */
public class PageModule implements SqlModule {

    private static final String TYPE = "PAGE";
    private int pageNumber;
    private int pageSize;
    private int totalCount;

    public PageModule() {
        this.pageNumber = 1;
        this.pageSize = 10;
    }

    public PageModule page(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber > 0 ? pageNumber : 1;
        this.pageSize = pageSize > 0 ? pageSize : 10;
        return this;
    }

    public PageModule pageNumber(int pageNumber) {
        this.pageNumber = pageNumber > 0 ? pageNumber : 1;
        return this;
    }

    public PageModule pageSize(int pageSize) {
        this.pageSize = pageSize > 0 ? pageSize : 10;
        return this;
    }

    public PageModule totalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    /**
     * Calculate the offset based on page number and page size.
     *
     * @return the offset value
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String build() {
        return "LIMIT " + getOffset() + ", " + pageSize;
    }

    @Override
    public boolean isValid() {
        return pageNumber > 0 && pageSize > 0;
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        if (totalCount <= 0) {
            return 0;
        }
        return (totalCount + pageSize - 1) / pageSize;
    }
}
