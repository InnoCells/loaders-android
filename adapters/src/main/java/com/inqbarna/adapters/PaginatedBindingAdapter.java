package com.inqbarna.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.common.paging.PaginatedList;

import java.util.Collection;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 14/9/16
 */

public class PaginatedBindingAdapter<T extends TypeMarker> extends BindingAdapter<T> {

    @Nullable private final PaginatedAdapterDelegate.ProgressHintListener mListener;
    private PaginatedAdapterDelegate<T> mDelegate;

    public PaginatedBindingAdapter() {
        this(null);
    }

    public PaginatedBindingAdapter(@Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        mListener = listener;
    }

    @Override
    protected T getDataAt(int position) {
        return getDelegate().getItem(position);
    }

    @Override
    public int getItemCount() {
        return getDelegate().getItemCount();
    }

    public void setLoadingIndicatorHint(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        getDelegate().setLoadingIndicatorHint(loadingListener);
    }

    public T getItem(int position) {
        return getDelegate().getItem(position);
    }

    public void setItems(PaginatedList<T> items) {
        getDelegate().setItems(items);
    }

    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        getDelegate().addNextPage(pageItems, lastPage);
    }

    public void clear() {
        getDelegate().clear();
    }

    public int getLastItemPosition() {
        return getDelegate().getLastItemPosition();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        getDelegate().onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        getDelegate().onDetachedFromRecyclerView(recyclerView);
    }

    protected final PaginatedAdapterDelegate<T> getDelegate() {
        if (null == mDelegate) {
            mDelegate = createDelegate(mListener);
        }
        return mDelegate;
    }

    protected PaginatedAdapterDelegate<T> createDelegate(@Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        return new PaginatedAdapterDelegate<T>(this, listener);
    }
}
