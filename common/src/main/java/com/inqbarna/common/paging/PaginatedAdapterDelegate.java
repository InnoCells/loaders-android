package com.inqbarna.common.paging;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 14/9/16
 */
public class PaginatedAdapterDelegate<T> {

    public static final int DEFAULT_REQUEST_DISTANCE = 5;
    private final RecyclerView.Adapter mAdapter;
    private       PaginatedList<T>     mList;
    private       ProgressHintListener mProgressHintListener;
    private       int                  mMinRequestDistance;
    private       boolean              mPageRequested;
    private final PaginateConfig       mPaginateConfig;


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

            if (hasMorePages() && (getLastItemPosition() - lastVisiblePosition) <= mMinRequestDistance) {
                innerRequestNext();
            }
        }
    };

    protected boolean hasMorePages() {
        return null != mList && mList.hasMorePages();
    }

    public PaginatedAdapterDelegate(RecyclerView.Adapter adapter, @Nullable ProgressHintListener loadingListener, PaginateConfig paginateConfig) {
        mMinRequestDistance = DEFAULT_REQUEST_DISTANCE;
        mAdapter = adapter;
        mPaginateConfig = paginateConfig;
        mProgressHintListener = loadingListener;
    }

    void requestNextPage() {
        mList.requestNext();
    }

    protected PaginateConfig getPaginateConfig() {
        return mPaginateConfig;
    }

    public T getItem(int position) {
        return null != mList ? mList.get(position) : null;
    }

    public void setItems(PaginatedList<T> items) {
        setItemsInternal(items, true);
    }

    protected void setItemsInternal(PaginatedList<T> items, boolean endLoad) {
        mList = items;
        if (endLoad) {
            if (null != mProgressHintListener) {
                mProgressHintListener.setLoadingState(false);
            }
            mPageRequested = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    protected boolean getPageRequested() {
        return mPageRequested;
    }

    public void clear() {
        if (null != mList) {
            mList.clear();
        }
        mList = null;
        endProgress();
    }

    protected void beginProgress() {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(true);
        }
        mPageRequested = true;
    }

    protected void endProgress() {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }
        mPageRequested = false;
        mAdapter.notifyDataSetChanged();
    }

    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        if (null == mList) {
            throw new IllegalStateException("You need first to initialize the PaginatedList");
        }
        final int initialSize = mList.size();
        mList.appendPageItems(pageItems, lastPage);


        onFinishAddItems(initialSize, pageItems.size());
    }


    protected void onFinishAddItems(int startPos, int size) {
        if (null != mProgressHintListener) {
            mProgressHintListener.setLoadingState(false);
        }

        mPageRequested = false;
        mAdapter.notifyItemRangeInserted(startPos, size);
    }

    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    private void innerRequestNext() {
        if (!mPageRequested && null != mList) {
            mPageRequested = true;
            if (null != mProgressHintListener) {
                mProgressHintListener.setLoadingState(true);
            }
            requestNextPage();
        }
    }

    public void setLoadingIndicatorHint(@Nullable ProgressHintListener loadingListener) {
        mProgressHintListener = loadingListener;
    }

    public int getLastItemPosition() {
        return Math.max(0, getItemCount() - 1);
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mScrollListener);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(mScrollListener);
    }

    /**
     * @author David García <david.garcia@inqbarna.com>
     * @version 1.0 14/9/16
     */
    public interface ProgressHintListener {
        void setLoadingState(boolean loading);
    }

}
