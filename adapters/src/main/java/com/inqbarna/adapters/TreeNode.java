package com.inqbarna.adapters;

/**
 * Created by headhunter on 3/02/17.
 */

public interface TreeNode<T extends NestableMarker<T>> {
    TreeNode<T> getParent();

    boolean isOpened();

    T getData();

    boolean open(boolean notify);

    boolean close(boolean notify);

    boolean closeChilds(boolean notify);

    boolean isChild(TreeNode<T> other, boolean findClosed);
}
