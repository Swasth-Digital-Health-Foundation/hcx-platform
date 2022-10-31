package org.swasth.apigateway.constants;

/**
 * Order of execution of gateway filters
 */
public enum FilterOrder {
    LOGGING_FILTER(Integer.MIN_VALUE),
    PRE_AUTH_FILTER(Integer.MIN_VALUE+1),
    AUTH_FILTER(Integer.MIN_VALUE+2),
    ;

    private final int order;

    FilterOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
