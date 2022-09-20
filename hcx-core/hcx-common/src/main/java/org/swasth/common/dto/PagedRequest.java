package org.swasth.common.dto;


public class PagedRequest {

    public PagedRequest(){}

    private static final int DEFAULT_SIZE = 50;

    private int offset;
    private int limit;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit != 0 ? limit : DEFAULT_SIZE;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}