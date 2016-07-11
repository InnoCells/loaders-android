package com.inqbarna.iqloaders.paged;

import android.content.Context;

import com.inqbarna.iqloaders.IQLoader;
import com.inqbarna.iqloaders.IQProvider;
import com.inqbarna.iqloaders.IQProviders;

import java.util.LinkedList;
import java.util.Queue;

public abstract class IQListLoader<T> extends IQLoader<PaginatedList<T>> {

    private static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_FIRST_PAGE = 1;
    private final int mFirstPage;

    private PaginatedList<T> mData;
    private int mPageSize;

    private static class Request {
        final int page;
        final int size;

        public Request(int page, int size) {
            this.page = page;
            this.size = size;
        }
    }

    private final Queue<Request> mRequests = new LinkedList<>();
    private boolean mLoading;


	public IQListLoader(Context context) {
        this(context, DEFAULT_FIRST_PAGE, DEFAULT_PAGE_SIZE);
    }

    public IQListLoader(Context ctxt, int firstPage, int pageSize) {
        super(ctxt);
        this.mFirstPage = firstPage;
        mPageSize = pageSize;
        mRequests.offer(new Request(mFirstPage, mPageSize));
    }

    @Override
	public final IQProvider<PaginatedList<T>> loadInBackground() {

        synchronized (mRequests) {
            if (mLoading) {
                throw new IllegalStateException("We're loading and loadInBackground was called again! something bad happened");
            }
            mLoading = true;
        }

        try {
            PaginatedList<T> data = null;
            if (mData != null) {
                data = new PaginatedList<>(this, mData);
            }


            Request nextRequest;
            synchronized (mRequests) {
                // first request shouldn't be null!
                nextRequest = mRequests.remove();
            }

            while (null != nextRequest) {
                PageProvider<T> listPageProvider = loadPageInBackground(nextRequest.page, nextRequest.size);
                if (null == data) {
                    data = new PaginatedList<>(this, listPageProvider);
                } else {
                    data.addPage(listPageProvider);
                }

                synchronized (mRequests) {
                    nextRequest = mRequests.poll();
                }
            }

            mData = data;
            return IQProviders.fromResult(data);
        } catch (Throwable throwable) {
            return IQProviders.fromError(throwable);
        } finally {
            synchronized (mRequests) {
                mLoading = false;
            }
        }
    }

	public abstract PageProvider<T> loadPageInBackground(int page, int pageSize);

	void loadNextPage() {
        synchronized (mRequests) {
            Request lastRequest = mRequests.peek();
            Request nextRequest;
            if (null == lastRequest) {
                if (null == mData) {
                    nextRequest = new Request(mFirstPage, mPageSize);
                } else {
                    nextRequest = new Request(mData.getLastPage() + 1, mPageSize);
                }
            } else {
                nextRequest = new Request(lastRequest.page + 1, mPageSize);
            }

            mRequests.offer(nextRequest);

            if (!mLoading) {
                onContentChanged();
            }
        }
	}
}
