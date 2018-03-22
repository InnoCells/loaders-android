package com.inqbarna.libsamples;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.inqbarna.adapters.ItemBinder;
import com.inqbarna.adapters.RxPaginatedBindingAdapter;
import com.inqbarna.adapters.TypeMarker;
import com.inqbarna.adapters.VariableBinding;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.PageFactories;
import com.inqbarna.rxutil.paging.PageFactory;
import com.inqbarna.rxutil.paging.RxPagingCallback;
import com.inqbarna.rxutil.paging.RxPagingConfig;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import rx.Observable;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.progress)
    View progress;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private RxPagingCallback mPagingCallback = new RxPagingCallback() {
        @Override
        public void onError(Throwable throwable) {
            Timber.e(throwable, "Error con las páginas!!");
            Toast.makeText(MainActivity.this, "Error detectado", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCompleted() {
            Timber.d("Paginación completada!");
        }
    };


    private PaginatedAdapterDelegate.ProgressHintListener mProgressListener = new PaginatedAdapterDelegate.ProgressHintListener() {
        @Override
        public void setLoadingState(boolean loading) {
            Timber.d("Loading state set to: %s", loading);
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    };

    private ItemBinder mItemBinder = (variableBinding, pos, dataAtPos) -> variableBinding.bindValue(BR.model, dataAtPos);
    private RxPaginatedBindingAdapter<TestVM> mAdapter;

    public static class TestVM implements TypeMarker {
        public final String value;

        public TestVM(int idx) {
            value = "Cell number: " + idx;
        }

        @Override
        public int getItemType() {
            return R.layout.main_test_item;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);




        mAdapter = new RxPaginatedBindingAdapter<>(mPagingCallback, new RxPagingConfig.Builder().build(), mProgressListener);
        mAdapter.setItemBinder(mItemBinder);
        list.setAdapter(mAdapter);

        mAdapter.setDataFactory(createFactory(), 20);
    }

    private List<TestVM> generateFirst(int amount) {
        List<TestVM> result = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            result.add(new TestVM(i));
        }
        return result;
    }

    private PageFactory<TestVM> createFactory() {
        return new PageFactory<TestVM>() {
            @NotNull
            @Override
            public List<? extends TestVM> getInitialData() {
                return generateFirst(133);
            }

            @SuppressLint("CheckResult")
            @NotNull
            @Override
            public Publisher<? extends TestVM> nextPageObservable(int startOffset, int pageSize) {
                Timber.d("[%s] Requested page %d + %d", Thread.currentThread().getName(), startOffset, pageSize);
                int endElem = Math.min(startOffset + pageSize, 600);
                pageSize = endElem - startOffset;
                Timber.d("[%s] Queueing page request %d + %d", Thread.currentThread().getName(), startOffset, pageSize);
                final Flowable<TestVM> shared = RxJavaInterop.toV2Flowable(Observable.range(startOffset, pageSize))
                                                             //                                                         .delaySubscription(1, TimeUnit.SECONDS)
                                                             .subscribeOn(Schedulers.io())
                                                             .map(TestVM::new)
                                                             .share();
                shared.count().subscribe(totalCount -> Timber.d("[%s] Emitted page elements: %d (@offset: %d)", Thread.currentThread().getName(), totalCount, startOffset));
                return shared;
            }
        };
    }
}
