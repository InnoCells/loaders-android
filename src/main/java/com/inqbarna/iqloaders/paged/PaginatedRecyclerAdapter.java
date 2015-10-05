package com.inqbarna.iqloaders.paged;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricard on 14/9/15.
 */
public abstract class PaginatedRecyclerAdapter<T, VH extends BindableViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private static final int PROGRESS_TYPE = 0;
    protected static final int ITEM_TYPE = 1;
    private List<T> items;
    private boolean completed;
    private int numExtraTypes;
    private final Context context;
    private OnLastItemShowedListener listener;

    public interface OnLastItemShowedListener {
        void onLastItemShowed();
    }

    public void setOnLastItemShowedListener(OnLastItemShowedListener listener) {
        this.listener = listener;
    }

    public PaginatedRecyclerAdapter(Context ctxt) {
        this(ctxt, null, false);
    }

    public PaginatedRecyclerAdapter(Context context, List<T> items) {
        this(context, items, false);
    }

    public PaginatedRecyclerAdapter(Context context, List<T> items, boolean completed) {
        this.context = context;
        this.items = items;
        this.completed = completed;
        numExtraTypes = 0;
    }

    public void updateData(List<T> items, boolean completed) {
        updateData(items, completed, true);
    }

    protected void updateData(List<T> items, boolean completed, boolean withNotify) {
        this.items = items;
        this.completed = completed;
        if (withNotify)
            notifyDataSetChanged();
    }

    protected boolean isComplete() {
        return completed;
    }

    protected void setNumExtraTypes(int numExtraTypes) {
        this.numExtraTypes = numExtraTypes;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        // switch type
        switch (viewType) {
            case PROGRESS_TYPE:
                return getLastElemHolder(parent);
            default:
                return getHolderForType(parent, viewType);
        }

    }

    protected abstract VH getLastElemHolder(ViewGroup parent);

    protected abstract VH getHolderForType(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        int itemViewType = holder.getItemViewType();
        switch (itemViewType) {
            case PROGRESS_TYPE:
                if (listener != null) {
                    listener.onLastItemShowed();
                }
                break;
            case ITEM_TYPE:
                holder.bindTo(getItem(position));
                break;
        }
    }

    public T getItem(int position) {
        return items.get(position);
    }

    protected List<T> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (!completed && position == getItemCount() + numExtraTypes - (getItemCount() - getActualElementCount())) {
            return PROGRESS_TYPE;
        } else {
            return getItemTypeForPos(position);
        }
    }

    protected int getItemTypeForPos(int pos) {
        return ITEM_TYPE;
    }

    @Override
    public int getItemCount() {
        int i = getActualElementCount() + (completed ? 0 : 1);
        return i;
    }

    public int getActualElementCount() {
        if (null == items)
            return 0;
        return items.size();
    }

    public void removeItem(int pos) {
        List<T> list = new ArrayList<>();
        list.addAll(items);
        list.remove(pos);
        items = list;
        notifyDataSetChanged();
    }
    public void removeItem(T item) {
        List<T> list = new ArrayList<>();
        list.addAll(items);
        list.remove(item);
        items = list;
        notifyDataSetChanged();
    }

    public void updateItem(T item) {
        List<T> list = new ArrayList<>();
        list.addAll(items);
        list.set(list.indexOf(item), item);
        items = list;
        notifyDataSetChanged();

    }

    public void addItem(T item) {
        List<T> list = new ArrayList<>();
        list.addAll(items);
        list.add(item);

        items = list;
        notifyDataSetChanged();
    }

    public void addItem(T item, int pos) {
        List<T> list = new ArrayList<>();
        list.addAll(items);
        list.add(pos, item);

        items = list;
        notifyDataSetChanged();
    }
}
