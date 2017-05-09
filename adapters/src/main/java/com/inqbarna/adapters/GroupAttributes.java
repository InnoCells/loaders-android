package com.inqbarna.adapters;

public class GroupAttributes {
    private int mColor;

    public GroupAttributes() {
        reset();
    }

    public void reset() {
        mColor = Integer.MIN_VALUE;
    }

    public int color() {
        return mColor;
    }

    public GroupAttributes setColor(int color) {
        mColor = color;
        return this;
    }

    public GroupAttributes setTo(GroupAttributes other) {
        mColor = other.mColor;
        return this;
    }
}