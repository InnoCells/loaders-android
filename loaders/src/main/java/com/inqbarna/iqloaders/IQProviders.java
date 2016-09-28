package com.inqbarna.iqloaders;

import com.inqbarna.iqloaders.paged.PageProvider;

import java.util.Collection;

/**
 * Created by David on 30/09/14.
 */
public class IQProviders {

    public static <T> IQProvider<T> fromError(final Throwable error) {
        return new IQProvider<T>() {
            @Override
            public T get() throws Throwable {
                throw error;
            }
        };
    }

    public static <T> IQProvider<T> fromResult(final T result) {
        return new IQProvider<T>() {
            @Override
            public T get() throws Throwable {
                return result;
            }
        };
    }

    public static <T> PageProvider<T> pageFromResult(final Collection<T> elements, final boolean completed, final int page) {
        return new PageProvider<T>() {
            @Override
            public boolean isCompleted() {
                return completed;
            }

            @Override
            public int getCurrentPage() {
                return page;
            }

            @Override
            public Collection<T> get() throws Throwable {
                return elements;
            }
        };
    }

    public static <T> PageProvider<T> pageFromError(final Throwable error) {
        return new PageProvider<T>() {
            @Override
            public boolean isCompleted() {
                return true;
            }

            @Override
            public int getCurrentPage() {
                return 0;
            }

            @Override
            public Collection<T> get() throws Throwable {
                throw error;
            }
        };
    }
}
