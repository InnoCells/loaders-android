package com.inqbarna.common.paging;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 19/9/16
 */
public class PaginateConfig {

    protected PaginateConfig(Builder builder) {
        // TODO: 19/9/16 some params?
    }

    public static class Builder {

        public PaginateConfig build() {
            return new PaginateConfig(this);
        }
    }
}
