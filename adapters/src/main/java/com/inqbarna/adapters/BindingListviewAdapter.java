package com.inqbarna.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 23/02/2017
 */

public abstract class BindingListviewAdapter extends BaseAdapter {

    public final BindingAdapterDelegate mBindingAdapterDelegate;

    public BindingListviewAdapter() {
        this(null);
    }

    public BindingListviewAdapter(ItemBinder binder) {
        mBindingAdapterDelegate = new BindingAdapterDelegate();
        if (null != binder) {
            mBindingAdapterDelegate.setItemBinder(binder);
        }
    }

    public abstract TypeMarker getItem(int position);

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        BindingHolder holder = null;
        if (null != convertView) {
            holder = ((BindingHolder) convertView.getTag());
        }
        TypeMarker item = getItem(position);
        if (null == holder) {
            holder = mBindingAdapterDelegate.onCreateViewHolder(parent, item.getItemType());
            holder.itemView.setTag(holder);
        }
        mBindingAdapterDelegate.onBindViewHolder(holder, position, item);
        return holder.itemView;
    }

    @Override
    public final int getItemViewType(int position) {
        // TODO: [DG - 23/02/2017] extend support for mixed types?
        return 0;
    }

    @Override
    public final int getViewTypeCount() {
        // TODO: [DG - 23/02/2017] extend support for mixed types?
        return 1;
    }
}
