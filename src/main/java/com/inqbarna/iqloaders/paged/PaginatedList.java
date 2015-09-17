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
    private IQListLoader.ListLoaderException exception;
    private int lastPage;


    PaginatedList(List<U> list, boolean completed, int lastPage) {
        this.list = list;
        this.completed = completed;
        this.exception = null;
        this.lastPage = lastPage;
    }

    PaginatedList(Exception e) {
        this.exception = new IQListLoader.ListLoaderException(e);
    }

    PaginatedList(PageProvider<U> provider) {
        try {
            Collection<U> elems = provider.get();
            list = new ArrayList<U>(elems);
            lastPage = provider.getCurrentPage();
            completed = provider.isCompleted();
        } catch (Throwable e) {
            exception = new IQListLoader.ListLoaderException(e);
        }
    }

    PaginatedList(PaginatedList<U> other) {
        this.list = other.list;
        this.exception = other.exception;
        this.lastPage = other.lastPage;
        this.completed = other.completed;
    }


    public boolean isCompleted() throws IQListLoader.ListLoaderException {
        if (exception != null) {
            throw exception;
        }
        return completed;
    }

    void setException(IQListLoader.ListLoaderException e) {
        this.exception = e;
    }

    public List<U> getList() throws IQListLoader.ListLoaderException {
        if (exception != null) {
            throw exception;
        }
        return Collections.unmodifiableList(list);
    }

    public boolean hasError() {
        return null != exception;
    }

    public int getLastPage() {
        return lastPage;
    }

    void addPage(PageProvider<U> provider) {

        if (null != exception) {
            return;
        }

        try {
            Collection<U> newElements = provider.get();
            list.addAll(newElements);
            lastPage = provider.getCurrentPage();
            completed = provider.isCompleted();
        } catch (Throwable e) {
            exception = new IQListLoader.ListLoaderException(e);
        }
    }
    public void updateList(List<U> newList) {
        list = newList;
    }
}
