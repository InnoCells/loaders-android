package com.inqbarna.iqloaders.paged;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

public abstract class IQListLoader<T> extends AsyncTaskLoader<PaginatedList<T>> {

    private static final String TAG = "ListLoader";
    private final int firstPage;

    public static final class ListLoaderException extends Exception {
        public ListLoaderException(Throwable throwable) {
            super(throwable.getMessage(), throwable);
        }
    }

    private PaginatedList<T> mData;


	public IQListLoader(Context context) {
		super(context);
        firstPage = 1;
    }

    public IQListLoader(Context ctxt, int firstPage) {
        super(ctxt);
        this.firstPage = firstPage;
    }


    @Override
    public void deliverResult(PaginatedList<T> data) {
        mData = data;

        super.deliverResult(mData);
    }

    @Override
    protected void onStartLoading() {
        if (null != mData) {
            deliverResult(mData);
        }

        if (takeContentChanged() || null == mData) {
            forceLoad();
        }
    }


    @Override
	public final PaginatedList<T> loadInBackground() {
        final int page;
        PaginatedList<T> data = null;
        if (mData == null) {
            page = firstPage;
        } else {
            page = mData.getLastPage() + 1;
            data = new PaginatedList<T>(mData);
        }
        PageProvider<T> listPageProvider = loadInBackground(page);


        if (null == data) {
            data = new PaginatedList<T>(listPageProvider);
        } else {
            data.addPage(listPageProvider);
        }

        return data;
    }

    public void removeItem(T item) {
        try {
            if(mData!=null) {
                List<T> list = new ArrayList<>();
                list.addAll(mData.getList());
                list.remove(item);
                mData.updateList(list);
            }
        } catch (ListLoaderException e) {
            e.printStackTrace();
        }
    }

    public void addItem(T item) {
        try {
            if(mData!=null) {
                List<T> list = new ArrayList<>();
                list.addAll(mData.getList());
                list.add(item);
                mData.updateList(list);
            }
        } catch (ListLoaderException e) {
            e.printStackTrace();
        }
    }
    public void addItem(T item, int pos) {
        try {
            if(mData!=null) {
                List<T> list = new ArrayList<>();
                list.addAll(mData.getList());
                list.add(pos, item);
                mData.updateList(list);
            }
        } catch (ListLoaderException e) {
            e.printStackTrace();
        }
    }

    public void updateItem(T item) {
        try {
            if(mData!=null) {
                List<T> list = new ArrayList<>();
                list.addAll(mData.getList());
                list.set(list.indexOf(item), item);

                mData.updateList(list);
            }
        } catch (ListLoaderException e) {
            e.printStackTrace();
        }
    }
	public abstract PageProvider<T> loadInBackground(int page);

	public void loadNextPage() {
        forceLoad();
	}

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        mData = null;
    }
}
