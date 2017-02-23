package com.inqbarna.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 6/9/16
 */
public abstract class BindingAdapter extends RecyclerView.Adapter<BindingHolder> {
    private final BindingAdapterDelegate mAdapterDelegate;

    protected BindingAdapter() {
        this(null);
    }

    protected BindingAdapter(ItemBinder binder) {
        mAdapterDelegate = new BindingAdapterDelegate();
        if (null != binder) {
            mAdapterDelegate.setItemBinder(binder);
        }
    }

    public void setItemBinder(ItemBinder itemBinder) {
        mAdapterDelegate.setItemBinder(itemBinder);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapterDelegate.onCreateViewHolder(parent, viewType);
    }

    public void setOverrideComponent(android.databinding.DataBindingComponent overrideComponent) {
        mAdapterDelegate.setOverrideComponent(overrideComponent);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        mAdapterDelegate.onBindViewHolder(holder, position, getDataAt(position));
    }

    protected abstract TypeMarker getDataAt(int position);

    @Override
    public int getItemViewType(int position) {
        return getDataAt(position).getItemType();
    }

}
