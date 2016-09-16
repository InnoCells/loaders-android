package com.inqbarna.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.inqbarna.iqloaders.paged.PaginatedAdapterDelegate;
import com.inqbarna.iqloaders.paged.PaginatedList;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 14/9/16
 */

public class PaginatedBindingAdapter<T extends TypeMarker> extends BindingAdapter<T> {

    private PaginatedAdapterDelegate<T> mDelegate;

    public PaginatedBindingAdapter() {
        this(null);
    }

    public PaginatedBindingAdapter(@Nullable PaginatedAdapterDelegate.ProgressHintListener listener) {
        mDelegate = new PaginatedAdapterDelegate<>(this, listener);
    }

    @Override
    protected T getDataAt(int position) {
        return mDelegate.getItem(position);
    }

    @Override
    public int getItemCount() {
        return mDelegate.getItemCount();
    }

    public void setLoadingIndicatorHint(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        mDelegate.setLoadingIndicatorHint(loadingListener);
    }

    public T getItem(int position) {
        return mDelegate.getItem(position);
    }

    public void setItems(PaginatedList<T> items) {
        mDelegate.setItems(items);
    }

    public int getLastItemPosition() {
        return mDelegate.getLastItemPosition();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mDelegate.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mDelegate.onDetachedFromRecyclerView(recyclerView);
    }
}
