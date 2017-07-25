package com.inqbarna.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */

public class GroupDecorator extends RecyclerView.ItemDecoration {

    private Rect  mDrawRect;
    private Rect  mDbgRect;
    private Paint mPaint;

    private final boolean DEBUG = false;

    private Paint mDbgPaint;

    public GroupDecorator() {
        mDrawRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        if (DEBUG) {
            mDbgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDbgPaint.setStyle(Paint.Style.STROKE);
            mDbgPaint.setStrokeWidth(1);
            mDbgPaint.setColor(Color.RED);
            mDbgRect = new Rect();
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.setEmpty();
        final GroupIndicator groupIndicator = getGroupIndicator(parent, view);
        if (null != groupIndicator) {
            final GroupAttributes attributes = groupIndicator.attributes();
            outRect.top += attributes.groupMarginTop();
            outRect.bottom += attributes.groupMarginBottom();
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int count = layoutManager.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = layoutManager.getChildAt(i);
            GroupIndicator indicator = getGroupIndicator(parent, child);
            if (null != indicator && indicator.enabled()) {
                layoutManager.getDecoratedBoundsWithMargins(child, mDrawRect);
                if (DEBUG) {
                    mDbgRect.set(mDrawRect);
                }
                final GroupAttributes attributes = indicator.attributes();
                mDrawRect.top += attributes.groupMarginTop();
                mDrawRect.bottom -= attributes.groupMarginBottom();
                mPaint.setColor(attributes.color());
                c.drawRect(mDrawRect, mPaint);

                if (DEBUG) {
                    c.drawRect(mDbgRect, mDbgPaint);
                }
            }
        }
    }

    @Nullable
    private GroupIndicator getGroupIndicator(RecyclerView parent, View child) {
        int adapterPosition = parent.getChildAdapterPosition(child);
        RecyclerView.ViewHolder holder = parent.findViewHolderForAdapterPosition(adapterPosition);
        GroupIndicator indicator = null;
        if (holder instanceof GroupIndicator) {
            indicator = (GroupIndicator) holder;

        }
        return indicator;
    }
}
