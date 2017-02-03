package com.inqbarna.adapters;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 31/1/17
 */

public interface NestableMarker<T extends NestableMarker<T>> extends TypeMarker {
    @NonNull List<T> children();
}
