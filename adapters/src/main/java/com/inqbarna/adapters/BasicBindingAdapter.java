package com.inqbarna.adapters;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.inqbarna.common.AdapterSyncList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter {
    public static final int INVALID_IDX = -1;

    @NonNull
    public static <T> DiffCallback<T> identityDiff() {
        return new DiffCallback<T>() {
            @Override
            public boolean areSameEntity(T a, T b) {
                return a == b;
            }

            @Override
            public boolean areContentEquals(T a, T b) {
                return a == b;
            }
        };
    }

    private static final UpdatesHandler MAIN_THREAD_HANDLER = new UpdatesHandler(Looper.getMainLooper());

    private List<T>                 mData;
    private DiffCallback<? super T> diffCallback;

    protected BasicBindingAdapter() {
        this(null);
    }

    public BasicBindingAdapter(ItemBinder binder) {
        setItemBinder(binder);
        mData = new ArrayList<>();
        diffCallback = identityDiff();
    }

    public void setItems(List<? extends T> items) {
        for (T anItem : mData) {
            onRemovingElement(anItem);
        }
        mData.clear();
        if (null != items) {
            mData.addAll(items);
        }
        Timber.d("[SET ITEMS] Notify dataSetChanged");
        notifyDataSetChanged();
    }

    public void setDiffCallback(DiffCallback<? super T> diffCallback) {
        this.diffCallback = diffCallback;
    }

    public Single<List<? extends T>> updateItems(@NonNull List<? extends T> items) {
        return new Updater<>(this, items);
    }

    private void onUpdateFinished(@NonNull DiffUtil.DiffResult diffResult, @NonNull List<? extends T> targetList) {
        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Timber.d("%d Items inserted at %d", count, position);
                mData.addAll(position, targetList.subList(position, position + count));
                notifyItemRangeInserted(addOffsets(position), count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Timber.d("%d Items removed from pos %d", count, position);
                final List<T> toRemove = mData.subList(position, position + count);
                for (T item : toRemove) {
                    onRemovingElement(item);
                }
                toRemove.clear();
                notifyItemRangeRemoved(addOffsets(position), count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Timber.d("Item moved %d --> %d", fromPosition, toPosition);
                final T item = mData.remove(fromPosition);
                mData.add(toPosition, item);
                notifyItemMoved(addOffsets(fromPosition), addOffsets(toPosition));
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Timber.d("%d items changed at position %d", count, position);

                if (null != payload) {
                    List<? extends T> data;
                    if (payload instanceof List) {
                        data = (List<? extends T>) payload;
                    } else {
                        data = Collections.singletonList((T) payload);
                    }

                    if (data.size() != count) {
                        throw new IllegalArgumentException("Payload size is " + data.size() + " but count is: " + count);
                    }


                    final ListIterator<T> replacements = mData.listIterator(position);
                    int targetReplaces = count;
                    final Iterator<? extends T> replacementIter = data.iterator();
                    while (replacements.hasNext() && targetReplaces > 0) {
                        final T next = replacements.next();
                        onRemovingElement(next);
                        replacements.set(replacementIter.next());
                        targetReplaces--;
                    }
                }
                notifyItemRangeChanged(addOffsets(position), count, payload);
            }
        });
    }

    private static class Updater<K extends TypeMarker> extends Single<List<? extends K>> implements Runnable {

        private final List<? extends K>      targetList;
        private final BasicBindingAdapter<K> adapter;
        private final List<K> srcList;
        private final DiffCallback<? super K> diffCallback;
        private DiffUtil.DiffResult diffResult;
        private Disposable mDisposable;

        private final DiffUtil.Callback _Callback = new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return srcList.size();
            }

            @Override
            public int getNewListSize() {
                return targetList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                final K first = Preconditions
                        .checkNotNull(srcList.get(oldItemPosition), "First element is null, comparing positions " + oldItemPosition + " to " + newItemPosition + " on " + this);
                final K second = Preconditions
                        .checkNotNull(targetList.get(newItemPosition), "Second element is null, comparing positions " + oldItemPosition + " to " + newItemPosition + " on " + this);
                return diffCallback.areSameEntity(first, second);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                final K first = Preconditions
                        .checkNotNull(srcList.get(oldItemPosition), "First element is null, comparing contents " + oldItemPosition + " to " + newItemPosition + " on " + this);
                final K second = Preconditions
                        .checkNotNull(targetList.get(newItemPosition), "Second element is null, comparing contents " + oldItemPosition + " to " + newItemPosition + " on " + this);
                return diffCallback.areContentEquals(first, second);
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return targetList.get(newItemPosition);
            }
        };
        private SingleObserver<? super List<? extends K>> mObserver;

        public Updater(@NonNull BasicBindingAdapter<K> adapter, @NonNull List<? extends K> targetList) {
            this.targetList = Preconditions.checkNotNull(targetList, "target list needs to be not null");
            this.adapter = Preconditions.checkNotNull(adapter, "adapter may not be null");
            srcList = adapter.mData;
            diffCallback = adapter.diffCallback;
        }

        @Override
        protected void subscribeActual(SingleObserver<? super List<? extends K>> observer) {
            mDisposable = Disposables.empty();
            this.mObserver = observer;
            this.mObserver.onSubscribe(mDisposable);
            startProcess();
        }

        @Override
        public String toString() {
            final MoreObjects.ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
            stringHelper.add("SrcSize", srcList.size())
                        .add("DstSize", targetList.size());

            return stringHelper.toString();
        }

        private void startProcess() {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }

        @Override
        public void run() {
            diffResult = DiffUtil.calculateDiff(_Callback);
            BasicBindingAdapter.MAIN_THREAD_HANDLER.obtainMessage(UpdatesHandler.RESULTS_FINISHED, this).sendToTarget();
        }

        public void apply() {
            if (null != diffResult) {
                adapter.onUpdateFinished(diffResult, targetList);
                if (!mDisposable.isDisposed()) {
                    mObserver.onSuccess(adapter.mData);
                }
            } else {
                Timber.e("Some unknown error happened while processing");
            }
        }
    }

    protected int addOffsets(int relativePos) {
        return relativePos;
    }

    protected int removeOffsets(int absPos) {
        return absPos;
    }

    protected void onRemovingElement(T item) {
        /* no-op */
    }

    public void addItems(List<? extends T> items) {
        addItems(INVALID_IDX, items);
    }

    public void addItems(int idx, List<? extends T> items) {
        if (null != items) {
            final int start = mData.size();
            boolean invalid = idx == INVALID_IDX;
            mData.addAll(invalid ? start : idx, items);
            notifyItemRangeInserted(invalid ? start : idx, items.size());
        }
    }

    public void removeItem(T item) {
        if (null != item) {
            final int i = mData.indexOf(item);
            if (i >= 0) {
                onRemovingElement(mData.remove(i));
                notifyItemRemoved(i);
            }
        }
    }

    protected List<T> getItemsInner() {
        return mData;
    }

    public List<T> editableList() {
        return new AdapterSyncList<>(mData, this);
    }

    @Override
    public T getDataAt(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Deprecated
    public static class OldBasicItemBinder<T> implements ItemBinder {
        private final T   mHandler;
        private final int mHandlerVar;
        private final int mModelVar;

        public OldBasicItemBinder(T handler, int handlerVar, int modelVar) {
            mHandler = handler;
            mHandlerVar = handlerVar;
            mModelVar = modelVar;
        }

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
            variableBinding.bindValue(mModelVar, dataAtPos);
            variableBinding.bindValue(mHandlerVar, mHandler);
        }
    }

    public interface DiffCallback<T> {
        boolean areSameEntity(T a, T b);
        boolean areContentEquals(T a, T b);
    }

    private static class UpdatesHandler extends Handler {
        static final int RESULTS_FINISHED = 1;
        public UpdatesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESULTS_FINISHED:
                    Updater updater = (Updater) msg.obj;
                    updater.apply();
                    break;
            }
        }
    }

}
