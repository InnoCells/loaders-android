package com.inqbarna.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */

public class GroupDecorator extends RecyclerView.ItemDecoration {

    private Rect  mDrawRect;
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
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int count = layoutManager.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = layoutManager.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child);
            RecyclerView.ViewHolder holder = parent.findViewHolderForAdapterPosition(adapterPosition);
            if (holder instanceof GroupIndicator) {
                GroupIndicator indicator = (GroupIndicator) holder;
                if (indicator.enabled()) {
                    layoutManager.getDecoratedBoundsWithMargins(child, mDrawRect);
                    mPaint.setColor(indicator.color());
                    c.drawRect(mDrawRect, mPaint);

                    if (DEBUG) {
                        c.drawRect(mDrawRect, mDbgPaint);
                    }
                }

            }
        }
    }

}
