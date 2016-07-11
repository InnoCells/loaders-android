package com.inqbarna.iqloaders.paged;

import android.content.Context;

import com.inqbarna.iqloaders.IQLoader;
import com.inqbarna.iqloaders.IQProvider;
import com.inqbarna.iqloaders.IQProviders;

public abstract class IQListLoader<T> extends IQLoader<PaginatedList<T>> {

    private static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_FIRST_PAGE = 1;
    private final int mFirstPage;


    public static final class ListLoaderException extends Exception {
        public ListLoaderException(Throwable throwable) {
            super(throwable.getMessage(), throwable);
        }
    }

    private PaginatedList<T> mData;
    private int mPageSize;


	public IQListLoader(Context context) {
		super(context);
        mFirstPage = DEFAULT_FIRST_PAGE;
        mPageSize = DEFAULT_PAGE_SIZE;
    }

    public IQListLoader(Context ctxt, int firstPage, int pageSize) {
        super(ctxt);
        this.mFirstPage = firstPage;
        mPageSize = pageSize;
    }

    protected void setPageSize(int pageSize) {
        mPageSize = pageSize;
    }

    @Override
	public final IQProvider<PaginatedList<T>> loadInBackground() {
        final int page;
        PaginatedList<T> data = null;
        if (mData == null) {
            page = mFirstPage;
        } else {
            page = mData.getLastPage() + 1;
            data = new PaginatedList<>(this, mData);
        }
        try {

            PageProvider<T> listPageProvider = loadPageInBackground(page, mPageSize);


            if (null == data) {
                data = new PaginatedList<>(this, listPageProvider);
            } else {
                data.addPage(listPageProvider);
            }

            mData = data;
            return IQProviders.fromResult(data);
        } catch (Throwable throwable) {
            return IQProviders.fromError(throwable);
        }
    }

	public abstract PageProvider<T> loadPageInBackground(int page, int pageSize);

	void loadNextPage() {
        onContentChanged();
	}

    protected void resetPages() {
        mData = null;
    }
}
