package com.inqbarna.adapters;

import android.support.annotation.Nullable;

import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.ErrorCallback;
import com.inqbarna.rxutil.paging.PageFactory;
import com.inqbarna.rxutil.paging.RxPagingAdapterDelegate;

import java.util.List;

import rx.Observable;
import rx.Scheduler;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPaginatedBindingAdapter<T extends TypeMarker> extends PaginatedBindingAdapter<T> {

    private final ErrorCallback mErrorCallback;

    public RxPaginatedBindingAdapter(ErrorCallback callback, @Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        super(listener);
        mErrorCallback = callback;
    }

    public RxPaginatedBindingAdapter(ErrorCallback callback) {
        this(callback, null);
    }

    @Override
    protected PaginatedAdapterDelegate<T> createDelegate(@Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        return new RxPagingAdapterDelegate<>(this, mErrorCallback, listener);
    }


    public void setDataFactory(PageFactory<T> factory, int pageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, pageSize);
    }

    public void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        ((RxPagingAdapterDelegate<T>)getDelegate()).setDataFactory(factory, displayPageSize, requestPageSize);
    }

}
