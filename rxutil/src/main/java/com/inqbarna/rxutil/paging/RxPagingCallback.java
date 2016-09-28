package com.inqbarna.rxutil.paging;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */
public interface RxPagingCallback {
    void onError(Throwable throwable);

    void onCompleted();
}
