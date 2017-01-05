package com.inqbarna.adapters;

import android.databinding.ViewDataBinding;

import com.inqbarna.common.AdapterSyncList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter {
    private List<T> mData;

    protected BasicBindingAdapter() {
        this(null);
    }

    public BasicBindingAdapter(ItemBinder binder) {
        setItemBinder(binder);
        mData = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        mData.clear();
        if (null != items) {
            mData.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<? extends T> items) {
        if (null != items) {
            final int start = mData.size();
            mData.addAll(items);
            notifyItemRangeInserted(start, items.size());
        }
    }

    public void removeItem(T item) {
        if (null != item) {
            final int i = mData.indexOf(item);
            if (i >= 0) {
                mData.remove(i);
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
    protected T getDataAt(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * @author David García <david.garcia@inqbarna.com>
     * @version 1.0 16/9/16
     */
    public static class BasicItemBinder<T> implements ItemBinder {
        private final T mHandler;
        private final int mHandlerVar;
        private final int mModelVar;

        public BasicItemBinder(T handler, int handlerVar, int modelVar) {
            mHandler = handler;
            mHandlerVar = handlerVar;
            mModelVar = modelVar;
        }

        @Override
        public void setHandlers(ViewDataBinding dataBinding, int viewType) {
            dataBinding.setVariable(mHandlerVar, mHandler);
        }

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
            variableBinding.bindValue(mModelVar, dataAtPos);
        }
    }
}
