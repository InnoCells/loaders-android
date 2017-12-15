package com.inqbarna.adapters;

import com.inqbarna.common.AdapterSyncList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter {
    public static final int INVALID_IDX = -1;
    private List<T> mData;

    protected BasicBindingAdapter() {
        this(null);
    }

    public BasicBindingAdapter(ItemBinder binder) {
        setItemBinder(binder);
        mData = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        for (T anItem : mData) {
            onRemovingElement(anItem);
        }
        mData.clear();
        if (null != items) {
            mData.addAll(items);
        }
        notifyDataSetChanged();
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

}
