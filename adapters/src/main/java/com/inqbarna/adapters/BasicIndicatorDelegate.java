package com.inqbarna.adapters;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */

public class BasicIndicatorDelegate implements GroupIndicator {
    private int     mColor;
    private boolean mEnabled;

    @Override
    public int color() {
        return mColor;
    }

    @Override
    public boolean enabled() {
        return mEnabled;
    }

    @Override
    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
}
