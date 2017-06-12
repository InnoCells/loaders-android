package com.inqbarna.adapters;

public class GroupAttributes {
    private int mColor;
    private int mPaddingLeft;


    public GroupAttributes() {
        reset();
    }

    public void reset() {
        mColor = Integer.MIN_VALUE;
        mPaddingLeft = 0;
    }

    public int color() {
        return mColor;
    }

    public int paddingLeft() {
        return mPaddingLeft;
    }

    public GroupAttributes setPaddingLeft(int paddingLeft) {
        mPaddingLeft = paddingLeft;
        return this;
    }

    public GroupAttributes setColor(int color) {
        mColor = color;
        return this;
    }

    public GroupAttributes setTo(GroupAttributes other) {
        mColor = other.mColor;
        mPaddingLeft = other.mPaddingLeft;
        return this;
    }
}