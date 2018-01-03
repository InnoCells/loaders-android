package com.inqbarna.adapters;

import android.support.annotation.NonNull;

import com.google.common.base.Equivalence;
import com.google.common.collect.BiMap;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.inqbarna.common.AdapterSyncList;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import timber.log.Timber;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 15/9/16
 */

public class BasicBindingAdapter<T extends TypeMarker> extends BindingAdapter {
    public static final int INVALID_IDX = -1;
    private List<T> mData;
    private Equivalence<? super T> equivalence;

    protected BasicBindingAdapter() {
        this(null);
    }

    public BasicBindingAdapter(ItemBinder binder) {
        setItemBinder(binder);
        mData = new ArrayList<>();
        equivalence = Equivalence.identity();
    }

    public void setEquivalence(Equivalence<? super T> equivalence) {
        this.equivalence = equivalence;
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

    public void updateItems(@NonNull List<? extends T> items) {

        final DiscreteDomain<Integer> domain = DiscreteDomain.integers();


        final BiMap<Integer, Equivalence.Wrapper<? super T>> originalItems = indexItems(mData);
        final BiMap<Integer, Equivalence.Wrapper<? super T>> newItems = indexItems(items);
        final BiMap<Equivalence.Wrapper<? super T>, Integer> inversedOrginals = originalItems.inverse();
        final BiMap<Equivalence.Wrapper<? super T>, Integer> inversedNewItems = newItems.inverse();

        final MapDifference<Equivalence.Wrapper<? super T>, Integer> difference = Maps.difference(inversedOrginals, inversedNewItems);

        final Map<Equivalence.Wrapper<? super T>, MapDifference.ValueDifference<Integer>> differing = difference.entriesDiffering();
        final Map<Equivalence.Wrapper<? super T>, Integer> leftOnly = difference.entriesOnlyOnLeft();
        final Map<Equivalence.Wrapper<? super T>, Integer> rightOnly = difference.entriesOnlyOnRight();

        final RangeSet<Integer> removedIndexes = keyRanges(leftOnly, domain);
        final RangeSet<Integer> addedIndexes = keyRanges(rightOnly, domain);

        for (T anItem : mData) {
            onRemovingElement(anItem);
        }

        mData.clear();
        mData.addAll(items);

        for (Range<Integer> removed : removedIndexes.asRanges()) {
            final ContiguousSet<Integer> indexes = ContiguousSet.create(removed, domain);
            Timber.d("Indexes removed: " + indexes);
            notifyItemRangeRemoved(addOffsets(indexes.first()), indexes.size());
        }

        for (Range<Integer> added : addedIndexes.asRanges()) {
            final ContiguousSet<Integer> indexes = ContiguousSet.create(added, domain);
            Timber.d("Indexes added: " + indexes);
            notifyItemRangeInserted(addOffsets(indexes.first()), indexes.size());
        }

        /*
        // for now assume just remove or add
        for (MapDifference.ValueDifference<Integer> changed : differing.values()) {
            Timber.d("Item moved: %d --> %d", changed.leftValue(), changed.rightValue());
            notifyItemMoved(addOffsets(changed.leftValue()), addOffsets(changed.rightValue()));
        }
        */
    }

    @NonNull
    private RangeSet<Integer> keyRanges(Map<?, Integer> map, DiscreteDomain<Integer> domain) {
        final RangeSet<Integer> indexes = TreeRangeSet.create();
        for (Integer key : map.values()) {
            indexes.add(Range.singleton(key).canonical(domain));
        }
        return indexes;
    }

    protected int addOffsets(int relativePos) {
        return relativePos;
    }

    protected int removeOffsets(int absPos) {
        return absPos;
    }

    private BiMap<Integer, Equivalence.Wrapper<? super T>> indexItems(@NonNull List<? extends T> data) {
        final ImmutableBiMap.Builder<Integer, Equivalence.Wrapper<? super T>> builder = ImmutableBiMap.builder();
        final ListIterator<? extends T> listIterator = data.listIterator();
        while (listIterator.hasNext()) {
            builder.put(listIterator.nextIndex(), equivalence.wrap(listIterator.next()));
        }
        return builder.build();
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
