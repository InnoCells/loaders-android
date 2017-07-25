package com.inqbarna.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 31/1/17
 */

public class TreeBindingAdapter<T extends NestableMarker<T>> extends BindingAdapter {

    private List<TreeNodeImpl<T>> mFlattened;
    private List<TreeNodeImpl<T>> mTree;

    private static final TreeNodeImpl ROOT = new TreeNodeImpl();

    public TreeBindingAdapter() {
        mFlattened = new ArrayList<>();
        mTree = new ArrayList<>();
    }

    public void setItems(List<? extends T> items) {
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
            final TreeNodeImpl<T> node = new TreeNodeImpl<>(this, item); // create node, by default closed...
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

    protected List<? extends T> getToplevelItemsData() {
        return new DataExtractList<>(mTree);
    }

    protected List<? extends T> getFlattenedItemsData() {
        return new DataExtractList<>(mFlattened);
    }

    protected List<? extends TreeNode<T>> getToplevelItems() {
        return Collections.unmodifiableList(mTree);
    }

    protected List<? extends TreeNode<T>> getFlattenedItems() {
        return Collections.unmodifiableList(mFlattened);
    }

    protected TreeNode<T> commonParent(TreeNode<T> a, TreeNode<T> b) {
        Set<TreeNode<T>> aParents = new HashSet<>();
        Set<TreeNode<T>> bParents = new HashSet<>();
        TreeNode<T> aParent = a;
        TreeNode<T> bParent = b;
        while (true) {
            aParent = aParent != null ? aParent.getParent() : null;
            bParent = bParent != null ? bParent.getParent() : null;
            if (null == aParent && null == bParent) {
                return null; // no common parents
            }
            if (null != aParent && aParent == bParent) {
                return aParent;
            }
            if (null != aParent) {
                if (bParents.contains(aParent)) {
                    return aParent;
                }
                aParents.add(aParent);
            }
            if (null != bParent) {
                if (aParents.contains(bParent)) {
                    return bParent;
                }
                bParents.add(bParent);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Iterable<TreeNode<T>> preOrder() {
        return new Traverser()
                .preOrderTraversal(ROOT)
                .filter(new Predicate<TreeNodeImpl<T>>() {
                    @Override
                    public boolean apply(@Nullable TreeNodeImpl<T> input) {
                        return input != ROOT;
                    }
                });
    }

    protected Iterable<T> depthIterationOverChilds(TreeNode<T> node) {
        if (!(node instanceof TreeNodeImpl)) {
            return ImmutableList.of();
        }

        TreeNodeImpl<T> root = (TreeNodeImpl<T>) node;
        return new Traverser()
                .preOrderTraversal(root)
                .transform(new Function<TreeNodeImpl<T>, T>() {
                    @Nullable
                    @Override
                    public T apply(@Nullable TreeNodeImpl<T> input) {
                        return null != input ? input.getData() : null;
                    }
                });
    }

    protected void applyInDepth(TreeNode<T> node, Function<T, Void> apply) {
        for (T in : depthIterationOverChilds(node)) {
            apply.apply(in);
        }
    }

    private class Traverser extends TreeTraverser<TreeNodeImpl<T>> {
        @Override
        public Iterable<TreeNodeImpl<T>> children(TreeNodeImpl<T> root) {
            if (root == ROOT) {
                return mTree;
            } else {
                return root.mChildNodes;
            }
        }
    }


    private static class DataExtractList<T extends NestableMarker<T>> extends AbstractList<T> {
        private final List<TreeNode<T>> mSource;

        DataExtractList(List<? extends TreeNode<T>> source) {
            mSource = new ArrayList<>(source);
        }

        @Override
        public T get(int index) {
            final TreeNode<T> tTreeNode = mSource.get(index);
            return tTreeNode.getData();
        }

        @Override
        public int size() {
            return mSource.size();
        }
    }

    private static class TreeNodeImpl<T extends NestableMarker<T>> implements TreeNode<T> {
        private boolean hasChildren;
        private final TreeNode<T> mParent;
        boolean mOpened;
        private int numChilren;
        final T data;
        private List<TreeNodeImpl<T>> mChildNodes;
        private List<TreeNodeImpl<T>> mExpandedNodes = Collections.emptyList();

        private final TreeBindingAdapter<T> mAdapter;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TreeNode{");
            sb.append("opened=").append(mOpened);
            sb.append(", children=").append(numChilren);
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }

        private TreeNodeImpl() {
            // invalid node!
            mAdapter = null;
            data = null;
            numChilren = 0;
            mParent = null;
            hasChildren = false;
        }

        public TreeNodeImpl(TreeBindingAdapter<T> adapter, @NonNull T marker) {
            this(adapter, marker, null);
        }

        @Override
        public TreeNode<T> getParent() {
            return mParent;
        }


        @Override
        public boolean isOpened() {
            return mOpened;
        }

        public TreeNodeImpl(TreeBindingAdapter<T> adapter, @NonNull T marker, TreeNode<T> parent) {
            mParent = parent;
            mAdapter = adapter;
            // closed state by default
            computeChildren(marker, true);
            data = marker;
            mOpened = false;
        }

        boolean open(int yourIdxInFlat, boolean notify) {
            if (!mOpened) {
                computeChildren(data, false);
                if (hasChildren) {
                    mExpandedNodes = new ArrayList<>(mChildNodes);
                    mAdapter.mFlattened.addAll(yourIdxInFlat + 1, mExpandedNodes);
                    if (notify) {
                        mAdapter.notifyItemRangeInserted(yourIdxInFlat + 1, numChilren);
                    }
                    mOpened = true;
                    return true;
                }
            }
            return false;
        }

        private void computeChildren(T marker, boolean force) {
            if (force || marker instanceof DynamicNestableMarker) {
                final List<T> children = marker.children();
                numChilren = children.size();
                mChildNodes = new ArrayList<>(numChilren);
                for (T item : children) {
                    mChildNodes.add(new TreeNodeImpl<>(mAdapter, item, this));
                }
                hasChildren = numChilren > 0;
            }
        }

        @Override
        public boolean openToChild(TreeNode<T> child, boolean notify) {
            Deque<TreeNode<T>> fifo = new ArrayDeque<>();
            TreeNode<T> parent = child;
            boolean isChild = false;
            while (null != parent) {
                fifo.push(parent);
                if (this == parent) {
                    isChild = true;
                    break;
                }
                parent = parent.getParent();
            }

            if (!isChild) {
                throw new IllegalArgumentException("The given child item is not a child of this node. Child = " + child + ", parent = " + this);
            }

            boolean allOpened = true;
            while (!fifo.isEmpty()) {
                TreeNode<T> toOpen = fifo.pop();
                toOpen.open(notify);
                allOpened &= toOpen.isOpened(); // we don't use ret value of open() because it return false if it was already open
            }

            return allOpened;
        }

        @Override
        public TreeNode<T> root() {
            if (null == mParent) {
                return this;
            } else {
                return mParent.root();
            }
        }

        private int findIndexOf(TreeNode<T> node) {
            List<TreeNodeImpl<T>> mFlattened1 = mAdapter.mFlattened;
            for (int i = 0, mFlattened1Size = mFlattened1.size(); i < mFlattened1Size; i++) {
                TreeNode<T> n = mFlattened1.get(i);
                if (itemEqual(n.getData(), node.getData())) {
                    return i;
                }
            }
            return -1;
        }

        boolean close(int yourIdxInFlat, boolean notify) {
            if (mOpened && mExpandedNodes.size() > 0) {

                final int numContributingChild = countContributing();

                List<TreeNodeImpl<T>> childNodes = mAdapter.mFlattened.subList(yourIdxInFlat + 1, yourIdxInFlat + 1 + numContributingChild);
                for (TreeNodeImpl<?> tn : childNodes) {
                    tn.mOpened = false;
                    tn.mExpandedNodes = Collections.emptyList();
                }
                childNodes.clear();
                if (notify) {
                    mAdapter.notifyItemRangeRemoved(yourIdxInFlat + 1, numContributingChild);
                }
                mExpandedNodes = Collections.emptyList();
                mOpened = false;
                return true;
            }
            return false;
        }

        private int countContributing() {
            int count = 0;
            if (mOpened) {
                if (numChilren != mExpandedNodes.size()) {
                    throw new ConcurrentModificationException("Cannot change child nodes while node is expanded");
                }
                count += numChilren;
                for (TreeNodeImpl<?> node : mExpandedNodes) {
                    count += node.countContributing();
                }
            }
            return count;
        }

        @Override
        public T getData() {
            return data;
        }

        @Override
        public boolean open(boolean notify) {
            int indexOf = findIndexOf(this);
            if (indexOf >= 0) {
                return open(indexOf, notify);
            }
            return false;
        }

        @Override
        public boolean close(boolean notify) {
            int indexOf = findIndexOf(this);
            if (indexOf >= 0) {
                return close(indexOf, notify);
            }
            return false;
        }

        @Override
        public boolean closeChilds(boolean notify) {
            boolean retVal = false;
            for (TreeNode<?> n : mChildNodes) {
                retVal |= n.close(notify);
            }
            return retVal;
        }

        @Override
        public boolean isChild(TreeNode<T> other, boolean findClosed) {
            TreeNode<T> parent = other.getParent();
            while (null != parent) {
                if (!findClosed && !parent.isOpened())
                    break;

                if (this == parent) {
                    return true;
                }
                parent = parent.getParent();
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TreeNodeImpl<?> treeNode = (TreeNodeImpl<?>) o;

            if (mOpened != treeNode.mOpened) return false;
            if (numChilren != treeNode.numChilren) return false;
            return data.getKey().equals(treeNode.data.getKey());

        }

        @Override
        public int hashCode() {
            int result = (mOpened ? 1 : 0);
            result = 31 * result + numChilren;
            result = 31 * result + data.getKey().hashCode();
            return result;
        }
    }

    public boolean isExpanded(T item) {
        for (int i = 0, sz = mFlattened.size(); i < sz; i++) {
            final TreeNodeImpl<T> treeNode = mFlattened.get(i);
            if (itemEqual(treeNode.data, item)) {
                return treeNode.mOpened;
            }
        }
        return false;
    }

    public boolean openAt(int visibleIndex, boolean notify) {
        if (visibleIndex >= 0 && visibleIndex < mFlattened.size()) {
            final TreeNodeImpl<T> tTreeNode = mFlattened.get(visibleIndex);
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
            final TreeNodeImpl<T> tTreeNode = mFlattened.get(visibleIndex);
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


    private static <T extends NestableMarker<T>> boolean itemEqual(@NonNull T aValue, @NonNull T bValue) {
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
