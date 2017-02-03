package com.inqbarna.adapters;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 31/1/17
 */

public class TreeBindingAdapter<T extends NestableMarker<T>> extends BindingAdapter {

    private List<TreeNode<T>> mFlattened;
    private List<TreeNode<T>> mTree;

    public TreeBindingAdapter() {
        mFlattened = new ArrayList<>();
        mTree = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
        final boolean wasEmpty = mFlattened.isEmpty();
        mFlattened.clear();
        mTree.clear();
        addItems(items, false);
        notifyDataSetChanged();
    }

    public void addItems(@NonNull List<? extends T> items) {
        addItems(items, true);
    }

    public void addItems(@NonNull List<? extends T> items, boolean notify) {
        int firstIdx = mFlattened.size();
        for (T item : items) {
            final TreeNode<T> node = new TreeNode<>(this, item); // create node, by default closed...
            mFlattened.add(node); // input items, are considered top-level, thus inserted directly to flattened list
            mTree.add(node); // top-level nodes, never can go away
            // wen colapsed mFlattened and mTree are equal
        }
        if (notify) {
            notifyItemRangeInserted(firstIdx, items.size());
        }
    }

    @Override
    protected TypeMarker getDataAt(int position) {
        return mFlattened.get(position).data;
    }

    @Override
    public int getItemCount() {
        return mFlattened.size();
    }

    protected List<? extends T> getToplevelItems() {
        return new DataExtractList<>(mTree);
    }

    protected List<? extends T> getFlattenedItems() {
        return new DataExtractList<>(mFlattened);
    }

    private static class DataExtractList<T extends NestableMarker<T>> extends AbstractList<T> {
        private final List<TreeNode<T>> mSource;

        public DataExtractList(List<TreeNode<T>> source) {
            mSource = source;
        }

        @Override
        public T get(int index) {
            final TreeNode<T> tTreeNode = mSource.get(index);
            return tTreeNode.data;
        }

        @Override
        public int size() {
            return mSource.size();
        }
    }


    private static class TreeNode<T extends NestableMarker<T>> {
        final boolean hasChildren;
        private final TreeNode<T> mParent;
        boolean mOpened;
        final int numChilren;
        final T data;
        private List<TreeNode<T>> mChildNodes;

        private final TreeBindingAdapter<T> mAdapter;

        public TreeNode(TreeBindingAdapter<T> adapter, @NonNull T marker) {
            this(adapter, marker, null);
        }

        public TreeNode(TreeBindingAdapter<T> adapter, @NonNull T marker, TreeNode parent) {
            mParent = parent;
            mAdapter = adapter;
            // closed state by default
            final List<T> children = marker.children();
            numChilren = children.size();
            mChildNodes = new ArrayList<>(numChilren);
            for (T item : children) {
                mChildNodes.add(new TreeNode<>(mAdapter, item, this));
            }
            data = marker;
            hasChildren = numChilren > 0;
            mOpened = false;
        }

        public boolean open(int yourIdxInFlat, boolean notify) {
            if (!mOpened && hasChildren) {
                mAdapter.mFlattened.addAll(yourIdxInFlat + 1, mChildNodes);
                if (notify) {
                    mAdapter.notifyItemRangeInserted(yourIdxInFlat + 1, numChilren);
                }
                mOpened = true;
                return true;
            }
            return false;
        }

        public boolean close(int yourIdxInFlat, boolean notify) {
            if (mOpened && hasChildren) {

                final int numContributingChild = countContributing();

                mAdapter.mFlattened.subList(yourIdxInFlat + 1, yourIdxInFlat + 1 + numContributingChild).clear();
                if (notify) {
                    mAdapter.notifyItemRangeRemoved(yourIdxInFlat + 1, numContributingChild);
                }
                mOpened = false;
                return true;
            }
            return false;
        }

        private int countContributing() {
            int count = 0;
            if (mOpened) {
                count += numChilren;
                for (TreeNode<?> node : mChildNodes) {
                    count += node.countContributing();
                }
            }
            return count;
        }
    }

    public boolean isExpanded(T item) {
        for (int i = 0, sz = mFlattened.size(); i < sz; i++) {
            final TreeNode<T> treeNode = mFlattened.get(i);
            if (itemEqual(treeNode.data, item)) {
                return treeNode.mOpened;
            }
        }
        return false;
    }

    public boolean openAt(int visibleIndex, boolean notify) {
        if (visibleIndex >= 0 && visibleIndex < mFlattened.size()) {
            final TreeNode<T> tTreeNode = mFlattened.get(visibleIndex);
            return tTreeNode.open(visibleIndex, notify);
        }
        return false;
    }

    public boolean open(@NonNull T visibleItem, boolean notify) {
        for (int i = 0, sz = mFlattened.size(); i < sz; i++) {
            if (itemEqual(mFlattened.get(i).data, visibleItem)) {
                return openAt(i, notify);
            }
        }
        return false;
    }

    public boolean closeAt(int visibleIndex, boolean notify) {
        if (visibleIndex >= 0 && visibleIndex < mFlattened.size()) {
            final TreeNode<T> tTreeNode = mFlattened.get(visibleIndex);
            return tTreeNode.close(visibleIndex, notify);
        }
        return false;
    }

    public boolean close(@NonNull T visibleItem, boolean notify) {
        for (int i = 0, sz = mFlattened.size(); i < sz; i++) {
            if (itemEqual(mFlattened.get(i).data, visibleItem)) {
                return closeAt(i, notify);
            }
        }
        return false;
    }


    private boolean itemEqual(@NonNull T aValue, @NonNull T bValue) {
        if (aValue == bValue) {
            return true;
        }

        if (aValue instanceof Comparable) {
            return ((Comparable<T>) aValue).compareTo(bValue) == 0;
        } else {
            return aValue.equals(bValue);
        }
    }

}
