package com.inqbarna.rxutil.paging;

import com.inqbarna.common.paging.PaginateConfig;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 19/9/16
 */

public class RxPagingConfig extends PaginateConfig {

    final boolean notifyAsInsertions;

    protected RxPagingConfig(Builder builder) {
        super(builder);
        notifyAsInsertions = builder.notifyInsertions;
    }

    public static class Builder extends PaginateConfig.Builder {

        private boolean notifyInsertions = true;

        public Builder disableNotifyAsInsertions() {
            notifyInsertions = false;
            return this;
        }

        public RxPagingConfig build() {
            return new RxPagingConfig(this);
        }
    }
}
