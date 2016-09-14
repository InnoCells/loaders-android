package com.inqbarna.iqloaders.paged;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Ricard on 14/9/15.
 */
public abstract class PaginatedRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final PaginatedAdapterDelegate<T> mPaginatedDelegate;

    public PaginatedRecyclerAdapter() {
        this(null);
    }

    public PaginatedRecyclerAdapter(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        mPaginatedDelegate = new PaginatedAdapterDelegate<>(this, loadingListener);
    }

    public void setLoadingIndicatorHint(@Nullable PaginatedAdapterDelegate.ProgressHintListener loadingListener) {
        mPaginatedDelegate.setLoadingIndicatorHint(loadingListener);
    }

    protected T getItem(int position) {
        return mPaginatedDelegate.getItem(position);
    }

    public void setItems(PaginatedList<T> items) {
        mPaginatedDelegate.setItems(items);
    }

    private int getLastItemPosition() {
        return mPaginatedDelegate.getLastItemPosition();
    }

    @Override
    public int getItemCount() {
        return mPaginatedDelegate.getItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mPaginatedDelegate.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mPaginatedDelegate.onDetachedFromRecyclerView(recyclerView);
    }

}
