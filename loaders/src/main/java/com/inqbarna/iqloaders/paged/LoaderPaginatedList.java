package com.inqbarna.iqloaders.paged;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.inqbarna.common.paging.PaginatedList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by David García <david.garcia@inqbarna.com> on 24/11/14.
 */
public class LoaderPaginatedList<U> implements PaginatedList<U> {

    private List<U> list;
    private boolean completed;
    private int lastPage;

    private IQListLoader<U> mLoader;

    public static <T> PaginatedList<T> fromData(Collection<? extends T> fullList) {
        return new LoaderPaginatedList<T>(null, new ArrayList<>(fullList), true, 1);
    }

    LoaderPaginatedList(IQListLoader<U> loader, List<U> list, boolean completed, int lastPage) {
        mLoader = loader;
        this.list = list;
        this.completed = completed;
        this.lastPage = lastPage;
    }

    LoaderPaginatedList(IQListLoader<U> loader, PageProvider<U> provider) throws Throwable {
        mLoader = loader;
        Collection<U> elems = provider.get();
        list = new ArrayList<U>(elems);
        lastPage = provider.getCurrentPage();
        completed = provider.isCompleted();
    }

    LoaderPaginatedList(IQListLoader<U> loader, LoaderPaginatedList<U> other) {
        mLoader = loader;
        this.list = other.list;
        this.lastPage = other.lastPage;
        this.completed = other.completed;
    }

    @Override
    public U get(int location) {
        return null != list ? list.get(location) : null;
    }

    @Override
    public int size() {
        return null != list ? list.size() : 0;
    }

    @Override
    public boolean hasMorePages() {
        return !completed;
    }

    @Override
    public void requestNext() {
        if (null != mLoader && !completed && !mLoader.isReset()) {
            mLoader.loadNextPage();
        }
    }

    @Override
    public void appendPageItems(Collection<? extends U> items, boolean last) {
        list.addAll(items);
        lastPage++;
        completed = last;
    }

    @Override
    public void clear() {
        list.clear();
        completed = true;
        mLoader = null;
    }

    int getLastPage() {
        return lastPage;
    }

    void addPage(PageProvider<U> provider) throws Throwable {
        Collection<U> newElements = provider.get();
        list.addAll(newElements);
        lastPage = provider.getCurrentPage();
        completed = provider.isCompleted();
    }

    List<U> editableList() {
        return new AbstractList<U>() {
            @Override
            public U get(int index) {
                return list.get(index);
            }

            @Override
            public int size() {
                return list != null ? list.size() : 0;
            }

            @Override
            public U remove(int index) {
                return list.remove(index);
            }

            @Override
            public void add(int index, U element) {
                list.add(index, element);
            }

            @Override
            public U set(int index, U element) {
                return list.set(index, element);
            }
        };
    }

    @Override
    public List<U> editableList(@Nullable RecyclerView.Adapter callbackAdapter) {
        return editableList();
    }
}
