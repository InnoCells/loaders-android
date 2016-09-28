package com.inqbarna.iqloaders.paged;

import android.content.Context;
import android.support.v4.os.OperationCanceledException;

import com.inqbarna.common.paging.PaginatedList;
import com.inqbarna.iqloaders.IQLoader;
import com.inqbarna.iqloaders.IQProvider;
import com.inqbarna.iqloaders.IQProviders;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class IQListLoader<T> extends IQLoader<PaginatedList<T>> {

    private static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_FIRST_PAGE = 1;
    private final int mFirstPage;

    private LoaderPaginatedList<T> mData;
    private int                    mPageSize;

    private static class Request {
        public static final int NO_SPECIAL = -1;
        public static final int RELOAD = -2;
        final int page;
        final int size;

        final int    requestCode;
        final Object payload;

        public Request(int page, int size) {
            this.page = page;
            this.size = size;
            requestCode = NO_SPECIAL;
            payload = false;
        }

        public Request(int requestCode, Object payload) {
            this.requestCode = requestCode;
            this.payload = payload;
            page = 0;
            size = 0;
        }

        public boolean isSpecialRequest() {
            return requestCode > 0;
        }
    }

    private final Deque<Request> mRequests = new LinkedList<>();
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



    protected void onSpecialTreatmentRequested(PaginatedList<T> inOutData, int requestCode, Object payload) {
        /* no-op: override if needed */
    }

    /**
     * Request to be called aside, this will be usefull for list update single item inside it for instance
     * @param requestCode
     * @param payload
     */
    protected void placeSpecialRequest(int requestCode, Object payload) {
        if (requestCode <= 0) {
            throw new IllegalArgumentException("request code needs to be > 0");
        }
        synchronized (mRequests) {
            addRequest(new Request(requestCode, payload));
        }
    }

    protected abstract void onServeSpecialRequest(int requestCode, List<T> inOutData, Object requestPayload);

    protected void requestReload() {
        synchronized (mRequests) {
            mRequests.clear();
            addRequest(new Request(Request.RELOAD, null));
        }
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
            LoaderPaginatedList<T> data = null;
            if (mData != null) {
                data = new LoaderPaginatedList<>(this, mData);
            }


            Request nextRequest;
            synchronized (mRequests) {
                // Shouldn't be null, if it is we're using onContentChange without placing a request
                nextRequest = mRequests.remove();
            }


            while (null != nextRequest) {

                if (isLoadInBackgroundCanceled()) {
                    synchronized (mRequests) {
                        mRequests.offerFirst(nextRequest);
                    }
                    return IQProviders.fromError(new OperationCanceledException("Load was cancelled, result will be ignored"));
                }

                if (nextRequest.isSpecialRequest()) {

                    if (nextRequest.requestCode == Request.RELOAD) {
                        PageProvider<T> listPageProvider = loadPageInBackground(mFirstPage, mPageSize);
                        data = new LoaderPaginatedList<T>(this, listPageProvider);
                    } else {
                        if (null != data) {
                            onServeSpecialRequest(nextRequest.requestCode, data.editableList(), nextRequest.payload);
                        }
                        /* else: just allow special requests to alter previous data set. Request will be silently ignored */
                    }
                } else {
                    PageProvider<T> listPageProvider = loadPageInBackground(nextRequest.page, nextRequest.size);
                    if (null == data) {
                        data = new LoaderPaginatedList<>(this, listPageProvider);
                    } else {
                        data.addPage(listPageProvider);
                    }
                }

                synchronized (mRequests) {
                    nextRequest = mRequests.poll();
                }
            }

            mData = data;
            return IQProviders.<PaginatedList<T>>fromResult(data);
        } catch (Throwable throwable) {
            return IQProviders.fromError(throwable);
        } finally {
            synchronized (mRequests) {
                mLoading = false;
            }
        }
    }

    @Override
    public final void onContentChanged() {
        throw new UnsupportedOperationException("You are not allowed to call this method directly. Sorry man!");
    }

    public abstract PageProvider<T> loadPageInBackground(int page, int pageSize);

	void loadNextPage() {
        synchronized (mRequests) {

            final Iterator<Request> requestIterator = mRequests.descendingIterator();
            Request nextRequest = null;
            while (requestIterator.hasNext()) {
                final Request r = requestIterator.next();
                if (!r.isSpecialRequest()) {
                    nextRequest = new Request(r.page + 1, mPageSize);
                    break;
                }
            }

            if (null == nextRequest) {
                if (null == mData) {
                    nextRequest = new Request(mFirstPage, mPageSize);
                } else {
                    nextRequest = new Request(mData.getLastPage() + 1, mPageSize);
                }
            }

            addRequest(nextRequest);
        }
	}

    private void addRequest(Request nextRequest) {
        synchronized (mRequests) {
            mRequests.offer(nextRequest);

            if (!mLoading) {
                super.onContentChanged();
            }
        }
    }
}