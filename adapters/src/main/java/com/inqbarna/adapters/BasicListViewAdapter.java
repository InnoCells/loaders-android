package com.inqbarna.adapters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 23/02/2017
 */

public class BasicListViewAdapter<T extends TypeMarker> extends BindingListviewAdapter {
    private List<T> mData;

    public BasicListViewAdapter(int modelVar) {
        super(new BasicItemBinder(modelVar));
        mData = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        mData.clear();

        if (null != items) {
            mData.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
