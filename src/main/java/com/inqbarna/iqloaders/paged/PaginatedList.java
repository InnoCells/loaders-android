package com.inqbarna.iqloaders.paged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by David García <david.garcia@inqbarna.com> on 24/11/14.
 */
public class PaginatedList<U> {

    private List<U> list;
    private boolean completed;
    private int lastPage;

    private IQListLoader<U> mLoader;

    public static <T> PaginatedList<T> fromData(Collection<? extends T> fullList) {
        return new PaginatedList<T>(null, new ArrayList<>(fullList), true, 1);
    }

    PaginatedList(IQListLoader<U> loader, List<U> list, boolean completed, int lastPage) {
        mLoader = loader;
        this.list = list;
        this.completed = completed;
        this.lastPage = lastPage;
    }

    PaginatedList(IQListLoader<U> loader, PageProvider<U> provider) throws Throwable {
        mLoader = loader;
        Collection<U> elems = provider.get();
        list = new ArrayList<U>(elems);
        lastPage = provider.getCurrentPage();
        completed = provider.isCompleted();
    }

    PaginatedList(IQListLoader<U> loader, PaginatedList<U> other) {
        mLoader = loader;
        this.list = other.list;
        this.lastPage = other.lastPage;
        this.completed = other.completed;
    }

    public List<U> getList() {
        return null != list ? Collections.unmodifiableList(list) : Collections.<U>emptyList();
    }

    public U get(int location) {
        return null != list ? list.get(location) : null;
    }

    public int size() {
        return null != list ? list.size() : 0;
    }

    public boolean hasMorePages() {
        return !completed;
    }

    public void requestNext() {
        if (null != mLoader && !completed && !mLoader.isReset()) {
            mLoader.loadNextPage();
        }
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
}
