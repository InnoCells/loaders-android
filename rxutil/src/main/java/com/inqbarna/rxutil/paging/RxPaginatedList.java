package com.inqbarna.rxutil.paging;

import com.inqbarna.common.paging.PaginatedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPaginatedList<U> extends Subscriber<List<? extends U>> implements PaginatedList<U> {
    private final Callbacks mCallbacks;
    private final RxPagingConfig mConfig;
    private List<U> mData;
    private boolean mCompleted;

    interface Callbacks extends RxPagingCallback {
        void onItemsAdded(int startPos, int size);
    }

    public static <T> PaginatedList<T> create(Observable<? extends List<? extends T>> stream, Callbacks callbacks, RxPagingConfig config) {
        return new RxPaginatedList<>(stream, callbacks, config);
    }

    private RxPaginatedList(Observable<? extends List<? extends U>> stream, Callbacks callbacks, RxPagingConfig config) {
        mCallbacks = callbacks;
        mConfig = config;
        mData = new ArrayList<>();
        mCompleted = false;
        stream.subscribe(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        requestNext();
    }

    @Override
    public U get(int location) {
        return null == mData ? null : mData.get(location);
    }

    @Override
    public int size() {
        return null == mData ? 0 : mData.size();
    }

    @Override
    public boolean hasMorePages() {
        return !mCompleted;
    }

    @Override
    public void requestNext() {
        if (!mCompleted) {
            request(1);
            return;
        }
        throw new IllegalStateException("You requested data after completed!");
    }

    @Override
    public void appendPageItems(Collection<? extends U> items, boolean last) {
        throw new UnsupportedOperationException("This is not supported, only internal data flow will be accepted");
    }

    @Override
    public void clear() {
        mData.clear();
        mCompleted = true;
        mCallbacks.onCompleted();
        unsubscribe();
    }

    @Override
    public void onCompleted() {
        mCompleted = true;
        mCallbacks.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        mCompleted = true;
        mCallbacks.onError(e);
    }

    @Override
    public void onNext(List<? extends U> us) {
        final int startSize = mData.size();
        mData.addAll(us);
        mCallbacks.onItemsAdded(startSize, us.size());
    }
}
