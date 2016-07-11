package com.inqbarna.iqloaders.paged;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Ricard on 14/9/15.
 */
public abstract class PaginatedRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private static final int DEFAULT_REQUEST_DISTANCE = 5;
    private PaginatedList<T> mList;
    private ProgressHintListener mProgressHintListener;

    private boolean mPageRequested;
    private int mMinRequestDistance;

    public interface ProgressHintListener {
        void setLoadingState(boolean loading);
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final int childCount = recyclerView.getChildCount();
            if (childCount == 0) {
                return;
            }
            final View lastChild = recyclerView.getChildAt(childCount - 1);
            final int lastVisiblePosition = recyclerView.getChildAdapterPosition(lastChild);

            if (null != mList && mList.hasMorePages() && (getLastItemPosition() - lastVisiblePosition) <= mMinRequestDistance) {
                requestNextPage();
            }
        }
    };

    private void requestNextPage() {
        if (!mPageRequested && null != mList) {
            mPageRequested = true;
            if (null != mProgressHintListener) {
                mProgressHintListener.setLoadingState(true);
            }
            mList.requestNext();
        }
    }

    public PaginatedRecyclerAdapter() {
        this(null);
    }

    public PaginatedRecyclerAdapter(@Nullable ProgressHintListener loadingListener) {
        mProgressHintListener = loadingListener;
        mMinRequestDistance = DEFAULT_REQUEST_DISTANCE;
    }

    public void setLoadingIndicatorHint(@Nullable ProgressHintListener loadingListener) {
        mProgressHintListener = loadingListener;
    }

    protected T getItem(int position) {
        return null != mList ? mList.get(position) : null;
    }

    public void setItems(PaginatedList<T> items) {
        mList = items;
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }
        mPageRequested = false;
        notifyDataSetChanged();
    }

    private int getLastItemPosition() {
        return Math.max(0, getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(mScrollListener);
    }
}
