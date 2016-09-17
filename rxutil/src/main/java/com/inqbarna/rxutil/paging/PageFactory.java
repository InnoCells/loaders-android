package com.inqbarna.rxutil.paging;

import rx.Observable;

/**
 * Created by david on 17/09/16.
 */

public interface PageFactory<T> {
    Observable<? extends T> nextPageObservable(int start, int size);
}
