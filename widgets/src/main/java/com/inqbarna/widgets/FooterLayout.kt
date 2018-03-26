package com.inqbarna.widgets

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import timber.log.Timber
import kotlin.properties.Delegates

@CoordinatorLayout.DefaultBehavior(FooterLayout.FooterLayoutBehavior::class)
class FooterLayout : FrameLayout {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initializeComponent(context, attrs, defStyleAttr, defStyleRes)
    }
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initializeComponent(context, attrs, defStyleAttr, 0)
    }

    private fun initializeComponent(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FooterLayout, defStyleAttr, defStyleAttr)
        hidden = typedArray.getBoolean(R.styleable.FooterLayout_footer_hidden, false)
        typedArray.recycle()

        startHidden = hidden
    }

    private var hidden: Boolean by Delegates.observable(false) { _, prev, current ->
        if (prev != current) {
            if (!current && startHidden) {
                startHidden = false
            }
        }
    }
    private var startHidden: Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (startHidden) {
            translationY = measuredHeight.toFloat()
            startHidden = false
        }
    }

    class FooterLayoutBehavior : CoordinatorLayout.Behavior<FooterLayout> {

        constructor()
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            // If we're scrolling down, then steal movement from target
            val measuredHeight = child.measuredHeight
            if (dy < 0 && measuredHeight != 0 && child.isEnabled) {
                val translationY = child.translationY
                if (translationY < measuredHeight) {
                    val displacementY = maxOf(dy, translationY.toInt() - measuredHeight)
                    doFooterDisplacement(child, displacementY)
                    consumed[0] = 0
                    consumed[1] = displacementY
                }
            }
        }

        private fun doFooterDisplacement(child: FooterLayout, displacementY: Int) {
            child.translationY -= displacementY
            val resultingTranslation = child.translationY.toInt()
            if (resultingTranslation == 0) {
                child.hidden = false
            } else if (resultingTranslation == child.measuredHeight) {
                child.hidden = true
            }
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
            val measuredHeight = child.measuredHeight
            if (dyUnconsumed > 0 && measuredHeight != 0 && child.isEnabled) {
                if (child.translationY > 0) {
                    // user is trying to scroll up, and target view is not doing so..., move footer up
                    val displacementY = minOf(dyUnconsumed, child.translationY.toInt())
                    doFooterDisplacement(child, displacementY)
                }
            }
        }

        override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
            Timber.d("Nested fling... velocityY: %f, consumed: %s", velocityY, consumed)
            return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
        }

        override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, velocityX: Float, velocityY: Float): Boolean {
            Timber.d("OnPrefling {velocityY: %f}", velocityY)
            return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        }

        override fun onNestedScrollAccepted(coordinatorLayout: CoordinatorLayout, child: FooterLayout, directTargetChild: View, target: View, axes: Int, type: Int) {
            super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, type: Int) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            Timber.d("OnStart Nested scroll {type: %d}", type)
            return (axes or View.SCROLL_AXIS_VERTICAL) != 0
        }
    }
}