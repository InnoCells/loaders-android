package com.inqbarna.rxutil.paging;

import com.inqbarna.common.paging.PaginatedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPaginatedList<U> extends Subscriber<List<? extends U>> implements PaginatedList<U> {
    private final Callbacks mCallbacks;
    private final Scheduler mScheduler;
    private List<U> mData;
    private boolean mCompleted;

    interface Callbacks extends ErrorCallback {
        void onItemsAdded(int startPos, int size);
    }

    public static <T> PaginatedList<T> create(Observable<? extends List<? extends T>> stream, Callbacks callbacks, Scheduler scheduler) {
        return new RxPaginatedList<>(stream, callbacks, scheduler);
    }

    private RxPaginatedList(Observable<? extends List<? extends U>> stream, Callbacks callbacks, Scheduler scheduler) {
        mCallbacks = callbacks;
        mScheduler = scheduler;
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
//            final Scheduler.Worker worker = mScheduler.createWorker();
//            worker.schedule(
//                    new Action0() {
//                        @Override
//                        public void call() {
//
//                        }
//                    }
//            );
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
        unsubscribe();
    }

    @Override
    public void onCompleted() {
        mCompleted = true;
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
