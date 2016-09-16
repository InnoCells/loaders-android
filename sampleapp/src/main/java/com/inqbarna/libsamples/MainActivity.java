package com.inqbarna.libsamples;

import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.inqbarna.adapters.BindingAdapter;
import com.inqbarna.adapters.RxPaginatedBindingAdapter;
import com.inqbarna.adapters.TypeMarker;
import com.inqbarna.adapters.VariableBinding;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.ErrorCallback;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.observables.AsyncOnSubscribe;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.progress)
    View progress;

    private ErrorCallback mErrorCallback;
    private PaginatedAdapterDelegate.ProgressHintListener mProgressListener = new PaginatedAdapterDelegate.ProgressHintListener() {
        @Override
        public void setLoadingState(boolean loading) {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    };

    private BindingAdapter.ItemBinder mItemBinder = new BindingAdapter.ItemBinder() {
        @Override
        public void setHandlers(ViewDataBinding dataBinding, int viewType) {
            /* no-op */
        }

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, Object dataAtPos) {
            variableBinding.bindValue(BR.model, dataAtPos);
        }
    };
    private RxPaginatedBindingAdapter<TestVM> mAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient                   mClient;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    public static class TestVM implements TypeMarker {
        public final String value;

        public TestVM(int idx) {
            value = "Cell number: " + idx;
        }

        @Override
        public int getItemType() {
            return R.layout.main_test_item;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        mAdapter = new RxPaginatedBindingAdapter<TestVM>(mErrorCallback, mProgressListener);
        mAdapter.setItemBinder(mItemBinder);
        list.setAdapter(mAdapter);

        mAdapter.setDataStream(createStream());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static class RequestState<T> {
        public final int     pageSize;
        private      int     mOffset;
        private      boolean mCompleted;

        private Deque<T> mDeque = new LinkedList<>();

        public RequestState(int pageSize) {
            this.pageSize = pageSize;
            mOffset = 0;
        }

        public int size() {
            return mDeque.size();
        }

        public void addValues(List<? extends T> nextValues) {
            for (T t : nextValues) {
                mDeque.offer(t);
            }
        }

        public void consume(long amount, Observer<Observable<? extends T>> observable) {
            List<T> values = new ArrayList<>((int)amount);
            while (amount > 0 && !mDeque.isEmpty()) {
                values.add(mDeque.removeFirst());
                amount--;
            }

            mCompleted = amount > 0;

            observable.onNext(Observable.from(values));
            if (mCompleted) {
                observable.onCompleted();
            }
        }

        public boolean getCompleted() {
            return mCompleted;
        }

        public static class PageRequest {
            public final int offset;
            public final int size;

            public PageRequest(int offset, int size) {
                this.offset = offset;
                this.size = size;
            }
        }

        public PageRequest nextRequest() {
            PageRequest request = new PageRequest(mOffset, pageSize);
            mOffset += pageSize;
            return request;
        }
    }

    private Observable<List<TestVM>> createStream() {
        return Observable.create(
                AsyncOnSubscribe.createStateful(
                        new Func0<RequestState<TestVM>>() {
                            @Override
                            public RequestState<TestVM> call() {
                                return new RequestState<>(20);
                            }
                        },
                        new Func3<RequestState<TestVM>, Long, Observer<Observable<? extends TestVM>>, RequestState<TestVM>>() {
                            @Override
                            public RequestState<TestVM> call(RequestState<TestVM> state, Long aLong, Observer<Observable<? extends TestVM>> observableObserver) {

                                // we're in BG!!
                                while (state.size() < aLong) {
                                    final RequestState.PageRequest request = state.nextRequest();
                                    try {
                                        final List<TestVM> nextValues;
                                        nextValues = Observable.range(request.offset, request.size).subscribeOn(Schedulers.io())
                                                               .map(
                                                                       new Func1<Integer, TestVM>() {
                                                                           @Override
                                                                           public TestVM call(Integer integer) {
                                                                               return new TestVM(integer);
                                                                           }
                                                                       }
                                                               )
                                                               .toList().toBlocking().single();
                                        state.addValues(nextValues);
                                        Thread.sleep(200);
                                    } catch (Throwable nse) {
                                        Log.e("BORRAME", "Error doing request: ", nse); // should forward?
                                        break;
                                    }
                                }

                                state.consume(aLong, observableObserver);


                                return state;
                            }
                        }
                )
        ).buffer(20).observeOn(AndroidSchedulers.mainThread());
    }
}
