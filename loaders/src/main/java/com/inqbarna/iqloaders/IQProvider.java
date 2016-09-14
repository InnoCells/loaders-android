package com.inqbarna.iqloaders;

/**
 * @author Brais Gabín, David García
 */
public interface IQProvider<T> {
    public T get() throws Throwable;
}
