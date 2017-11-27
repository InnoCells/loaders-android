package com.inqbarna.rxutil.paging;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observer;

/**
 * Created by david on 17/09/16.
 */
class RequestState<T> {
    public final int pageSize;
    private int mOffset;
    private AtomicBoolean mCompleted;
    private AtomicReference<Throwable> mError;
    private PageFactory<T> mFactory;

    RequestState(int pageSize, PageFactory<T> mFactory) {
        this.pageSize = pageSize;
        this.mFactory = mFactory;
        mOffset = 0;
        mCompleted = new AtomicBoolean(false);
        mError = new AtomicReference<>(null);
    }

    boolean getCompleted() {
        return mCompleted.get();
    }

    Throwable getError() {
        return mError.get();
    }

    Observable<? extends T> nextObservable() {
        int offset = mOffset;
        mOffset += pageSize;
        Observable<? extends T> observable = mFactory.nextPageObservable(offset, pageSize);
        final AtomicInteger counter = new AtomicInteger(pageSize);

        return observable
                .doOnEach(
                        new Observer<T>() {
                            @Override
                            public void onCompleted() {
                                int remaining = counter.get();
                                if (remaining < 0) {
                                    mError.compareAndSet(null,
                                                         new IllegalArgumentException(
                                                                 "Given page observable produced " + (-1 * remaining) + " extra items, only " + pageSize + "expected"));
                                } else if (remaining > 0) {
                                    // Ok, source ended because we have fewer elements!
                                    mCompleted.compareAndSet(false, true);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                mError.compareAndSet(null, e);
                            }

                            @Override
                            public void onNext(T t) {
                                counter.decrementAndGet();
                            }
                        }
                );
    }
}
