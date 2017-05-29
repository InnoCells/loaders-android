package com.inqbarna.adapters;

/**
 * Created by jmartinez on 29/05/17.
 */

public interface DynamicNestableMarker<T extends NestableMarker<T>> extends NestableMarker<T> {
}
