package com.inqbarna.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 6/9/16
 */
public abstract class BindingAdapter<T extends TypeMarker> extends RecyclerView.Adapter<BindingHolder> {
    private ItemBinder mItemBinder;

    public void setItemBinder(ItemBinder itemBinder) {
        mItemBinder = itemBinder;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (View.NO_ID == viewType) {
            throw new IllegalArgumentException("Unexpected layout resource");
        }
        checkBinder();

        final ViewDataBinding dataBinding;
        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);
        final BindingHolder bindingHolder = new BindingHolder(dataBinding);
        mItemBinder.setHandlers(dataBinding, viewType);
        return bindingHolder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        checkBinder();
        final SafeVariableBinding variableBinding = holder.lockVars();
        mItemBinder.bindVariables(variableBinding, position, getDataAt(position));
        variableBinding.unlockVars();
    }

    protected abstract TypeMarker getDataAt(int position);

    private void checkBinder() {
        if (null == mItemBinder) {
            throw new IllegalStateException("ItemBinder not assigned yet!");
        }
    }

    @Override
    public int getItemViewType(int position) {
        checkBinder();
        return getDataAt(position).getItemType();
    }

    public interface ItemBinder {
        void setHandlers(ViewDataBinding dataBinding, int viewType);
        void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos);
    }
}
