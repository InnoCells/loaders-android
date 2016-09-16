package com.inqbarna.rxutil.paging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.common.paging.PaginatedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */

public class RxPagingAdapterDelegate<T> extends PaginatedAdapterDelegate<T> {

    private final Scheduler mScheduler;
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

    public RxPagingAdapterDelegate(RecyclerView.Adapter adapter, @NonNull ErrorCallback errorCallback, @Nullable Scheduler scheduler, @Nullable ProgressHintListener loadingListener) {
        super(adapter, loadingListener);
        mErrorCallback = errorCallback;
        mScheduler = scheduler == null ? createDefaultScheduller() : scheduler;
    }

    private Scheduler createDefaultScheduller() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        return Schedulers.from(executorService);
    }

    @Override
    public void setItems(PaginatedList<T> items) {
        setDataStream(asObservable(items));
    }

    private static <U> Observable<List<? extends U>> asObservable(final PaginatedList<U> items) {
        return Observable.create(
                new Observable.OnSubscribe<List<? extends U>>() {
                    @Override
                    public void call(Subscriber<? super List<? extends U>> subscriber) {
                        final int size = items.size();
                        List<U> data = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            data.add(items.get(i));
                        }
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(data);
                            subscriber.onCompleted();
                        }
                    }
                }
        );
    }

    public void setDataStream(Observable<? extends List<? extends T>> stream) {
        if (null != mActiveSubscription) {
            mActiveSubscription.unsubscribe();
        }
        final PaginatedList<T> items = RxPaginatedList.create(stream.subscribeOn(mScheduler), mCallbacks, mScheduler);
        mActiveSubscription = ((RxPaginatedList<T>)items);
        super.setItems(items);
    }

    @Override
    public void addNextPage(Collection<? extends T> pageItems, boolean lastPage) {
        throw new UnsupportedOperationException("This delegate does not support this operation");
    }
}
