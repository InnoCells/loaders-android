package com.inqbarna.adapters;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */

public interface GroupIndicator {
    void setEnabled(boolean enabled);
    boolean enabled();
    GroupAttributes attributes();
}
