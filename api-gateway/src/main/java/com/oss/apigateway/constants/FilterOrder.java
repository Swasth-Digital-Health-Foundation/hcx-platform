package com.oss.apigateway.constants;

/**
 * Order of execution of gateway filters
 */
public enum FilterOrder {
    CORRELATION_ID_FILTER(Integer.MIN_VALUE),
    LOGGING_FILTER(Integer.MIN_VALUE+1),
    PRE_AUTH_FILTER(Integer.MIN_VALUE+2),
    AUTH_FILTER(Integer.MIN_VALUE+3),
    ;

    private final int order;

    FilterOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
