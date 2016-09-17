package com.inqbarna.rxutil.paging;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Observer;

/**
 * Created by david on 17/09/16.
 */
class RequestState<T> {
    public final int pageSize;
    private int mOffset;
    private boolean mCompleted;
    private PageFactory<T> mFactory;

    private Deque<T> mDeque = new LinkedList<>();

    RequestState(int pageSize, PageFactory<T> mFactory) {
        this.pageSize = pageSize;
        this.mFactory = mFactory;
        mOffset = 0;
    }

    int size() {
        return mDeque.size();
    }

    void addValues(List<? extends T> nextValues) {
        for (T t : nextValues) {
            mDeque.offer(t);
        }
    }

    void consume(long amount, Observer<Observable<? extends T>> observable) {
        List<T> values = new ArrayList<>((int) amount);
        while (amount > 0 && !mDeque.isEmpty()) {
            values.add(mDeque.removeFirst());
            amount--;
        }

        mCompleted = amount > 0;

        observable.onNext(Observable.from(values));
        if (mCompleted) {
            observable.onCompleted();
        }
    }

    boolean getCompleted() {
        return mCompleted;
    }

    static class PageRequest {
        final int offset;
        final int size;

        PageRequest(int offset, int size) {
            this.offset = offset;
            this.size = size;
        }
    }

    public PageRequest nextRequest() {
        PageRequest request = new PageRequest(mOffset, pageSize);
        mOffset += pageSize;
        return request;
    }

    public Observable<? extends T> nextObservable() {
        PageRequest pageRequest = nextRequest();
        return mFactory.nextPageObservable(pageRequest.offset, pageRequest.size);
    }
}
