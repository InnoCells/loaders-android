package com.inqbarna.adapters;

import android.databinding.DataBindingComponent;
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
public abstract class BindingAdapter extends RecyclerView.Adapter<BindingHolder> {
    private ItemBinder mItemBinder;

    private android.databinding.DataBindingComponent mOverrideComponent;

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
        if (null == mOverrideComponent) {
            dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);
        } else {
            dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false, mOverrideComponent);
        }
        return new BindingHolder(dataBinding);
    }

    public void setOverrideComponent(android.databinding.DataBindingComponent overrideComponent) {
        mOverrideComponent = overrideComponent;
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
        void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos);
    }
}
