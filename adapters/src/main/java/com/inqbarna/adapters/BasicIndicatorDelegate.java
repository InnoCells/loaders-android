package com.inqbarna.adapters;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */

public class BasicIndicatorDelegate implements GroupIndicator {
    private final GroupAttributes mGroupAttributes = new GroupAttributes();
    private boolean mEnabled;

    @Override
    public boolean enabled() {
        return mEnabled;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public GroupAttributes attributes() {
        return mGroupAttributes;
    }
}
