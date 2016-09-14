package com.inqbarna.iqloaders.paged;

import com.inqbarna.iqloaders.IQProvider;

import java.util.Collection;

/**
* Created by David García <david.garcia@inqbarna.com> on 24/11/14.
*/
public interface PageProvider<U> extends IQProvider<Collection<U>> {
    public boolean isCompleted();
    public int getCurrentPage();
}
