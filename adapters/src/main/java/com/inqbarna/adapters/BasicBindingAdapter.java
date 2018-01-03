package com.inqbarna.adapters;

import android.support.annotation.NonNull;

import com.google.common.base.Equivalence;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
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
import java.util.Set;
import java.util.SortedMap;

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

        final Set<Equivalence.Wrapper<? super T>> originalItemSet = toSet(mData);
        final Set<Equivalence.Wrapper<? super T>> newItemsSet = toSet(items);



        final SortedMap<Integer, T> originalItems = indexItems(mData);
        final SortedMap<Integer, T> newItems = indexItems(items);
        final MapDifference<Integer, T> difference = Maps.difference(originalItems, newItems, equivalence);
        if (difference.areEqual()) {
            return;
        }

        final DiscreteDomain<Integer> domain = DiscreteDomain.integers();
        final Map<Integer, MapDifference.ValueDifference<T>> differing = difference.entriesDiffering();
        final Map<Integer, T> leftOnly = difference.entriesOnlyOnLeft();
        final Map<Integer, T> rightOnly = difference.entriesOnlyOnRight();

        final RangeSet<Integer> removedIndexes = keyRanges(leftOnly, domain);
        final RangeSet<Integer> differingIndexes = keyRanges(differing, domain);
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

        for (Range<Integer> changed : differingIndexes.asRanges()) {
            final ContiguousSet<Integer> indexes = ContiguousSet.create(changed, domain);
            Timber.d("Indexes changed: " + indexes);
            notifyItemRangeRemoved(addOffsets(indexes.first()), indexes.size());
        }

        for (Range<Integer> added : addedIndexes.asRanges()) {
            final ContiguousSet<Integer> indexes = ContiguousSet.create(added, domain);
            Timber.d("Indexes added: " + indexes);
            notifyItemRangeInserted(addOffsets(indexes.first()), indexes.size());
        }
    }

    protected Set<Equivalence.Wrapper<? super T>> toSet(@NonNull List<? extends T> data) {
        final ImmutableSet.Builder<Equivalence.Wrapper<? super T>> builder = ImmutableSet.builder();
        for (T item : data) {
            builder.add(equivalence.wrap(item));
        }
        return builder.build();
    }

    @NonNull
    private RangeSet<Integer> keyRanges(Map<Integer, ?> map, DiscreteDomain<Integer> domain) {
        final RangeSet<Integer> indexes = TreeRangeSet.create();
        for (Integer key : map.keySet()) {
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

    protected SortedMap<Integer, T> indexItems(@NonNull List<? extends T> data) {
        final ImmutableSortedMap.Builder<Integer, T> builder = ImmutableSortedMap.naturalOrder();
        final ListIterator<? extends T> listIterator = data.listIterator();
        while (listIterator.hasNext()) {
            builder.put(listIterator.nextIndex(), listIterator.next());
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
