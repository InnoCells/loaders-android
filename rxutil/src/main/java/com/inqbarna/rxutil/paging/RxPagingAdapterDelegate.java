package com.inqbarna.rxutil.paging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.common.paging.PaginatedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func3;
import rx.observables.AsyncOnSubscribe;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPagingAdapterDelegate<T> extends PaginatedAdapterDelegate<T> {

    private RxPaginatedList.Callbacks mCallbacks = new RxPaginatedList.Callbacks() {
        @Override
        public void onError(Throwable throwable) {
            mErrorCallback.onError(throwable);
        }

        @Override
        public void onItemsAdded(int startPos, int size) {
            onFinishAddItems(startPos, size);
        }
    };

    private ErrorCallback mErrorCallback;
    private Subscription mActiveSubscription;

    public RxPagingAdapterDelegate(RecyclerView.Adapter adapter, @NonNull ErrorCallback errorCallback, @Nullable ProgressHintListener loadingListener) {
        super(adapter, loadingListener);
        mErrorCallback = errorCallback;
    }

    @Override
    public void setItemsInternal(PaginatedList<T> items, boolean endLoad) {
        setDataFactory(asPageFactory(items), items.size(), items.size(), endLoad);
    }

    private PageFactory<T> asPageFactory(PaginatedList<T> items) {
        final List<T> allItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            allItems.add(items.get(i));
        }

        return new PageFactory<T>() {
            @Override
            public Observable<? extends T> nextPageObservable(int start, int size) {
                List<T> subList = allItems.subList(start, start + Math.min(size, allItems.size()));
                return Observable.from(subList);
            }
        };
    }

    public void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        setDataFactory(factory, displayPageSize, requestPageSize, false);
    }

    private void setDataFactory(PageFactory<T> factory, int displayPageSize, int requestPageSize, boolean endLoad) {
        setDataStream(createStreamObservable(factory, displayPageSize, requestPageSize), endLoad);
    }

    private Observable<? extends List<? extends T>> createStreamObservable(PageFactory<T> factory, int displayPageSize, int requestPageSize) {
        return createStream(factory, displayPageSize, requestPageSize);
    }

    private Observable<List<T>> createStream(final PageFactory<T> factory, final int displayPageSize, final int requestPageSize) {
        return Observable.create(
                AsyncOnSubscribe.createStateful(
                        new Func0<RequestState<T>>() {
                            @Override
                            public RequestState<T> call() {
                                return new RequestState<>(requestPageSize, factory);
                            }
                        },
                        new Func3<RequestState<T>, Long, Observer<Observable<? extends T>>, RequestState<T>>() {
                            @Override
                            public RequestState<T> call(RequestState<T> state, Long aLong, Observer<Observable<? extends T>> observableObserver) {
                                Throwable error = state.getError();
                                if (null != error) {
                                    observableObserver.onError(error);
                                } else if (state.getCompleted()) {
                                    observableObserver.onCompleted();
                                } else {
                                    Observable<? extends T> ob = state.nextObservable();
                                    observableObserver.onNext(ob);
                                }
                                return state;
                            }
                        }
                )
        ).buffer(displayPageSize).observeOn(AndroidSchedulers.mainThread(), 1);
    }

    private void setDataStream(Observable<? extends List<? extends T>> stream, boolean endLoad) {
        if (null != mActiveSubscription) {
            mActiveSubscription.unsubscribe();
        }
        final PaginatedList<T> items = RxPaginatedList.create(stream, mCallbacks);
        mActiveSubscription = ((RxPaginatedList<T>)items);
        beginProgress();
        super.setItemsInternal(items, endLoad);
    }

    @Override
    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        throw new UnsupportedOperationException("This delegate does not support this operation");
    }

    public void setDataFactory(PageFactory<T> factory, int pageSize) {
        setDataFactory(factory, pageSize, pageSize);
    }
}
