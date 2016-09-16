package com.inqbarna.common.paging;

import java.util.Collection;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */
public interface PaginatedList<U> {
    U get(int location);
    int size();
    boolean hasMorePages();
    void requestNext();
    void appendPageItems(Collection<? extends U> items, boolean last);
    void clear();
}
