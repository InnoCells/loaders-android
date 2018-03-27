package com.inqbarna.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

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
        val hidden = typedArray.getBoolean(R.styleable.FooterLayout_footer_hidden, false)
        typedArray.recycle()

        showRatio = if (hidden) 0f else 1.0f
        needsUpdate = true
    }

    private var showRatio: Float = 0.0f
        set(v) {
            field = maxOf(0.0f, minOf(v, 1.0f))
            applyTranslation(field)
            if (field == 1.0f) {
                hidden = false
            } else if (field == 0.0f) {
                hidden = true
            }
        }

    private fun applyTranslation(ratio: Float) {
        if (isEnabled) {
            if (measuredHeight != 0) {
                translationY = measuredHeight * (1 - ratio)
            }
        }
    }

    private var hidden: Boolean = false
    private var needsUpdate: Boolean = false
    private var animator: Animator? = null
    var onFooterEnabledListener: OnFooterEnabledListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (needsUpdate) {
            applyTranslation(showRatio)
            needsUpdate = false
        }
    }

    private fun cancelAnimation() {
        animator?.cancel()
        animator = null
    }

    fun dismiss() {
        val initialValue = translationY
        cancelAnimation()
        val animator = ValueAnimator.ofFloat(initialValue, measuredHeight.toFloat())
        animator.addUpdateListener { a -> translationY = a.animatedValue as Float }
        animator.interpolator = LinearInterpolator()
        isEnabled = false
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                translationY = measuredHeight.toFloat()

            }

            override fun onAnimationCancel(animation: Animator?) {
                showRatio = initialValue
                isEnabled = true
            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                this@FooterLayout.animator = animation
            }
        })
        animator.setTarget(this)
        animator.start()
    }

    override fun setEnabled(enabled: Boolean) {
        if (this.isEnabled != enabled) {
            super.setEnabled(enabled)
            applyTranslation(showRatio)
            if (enabled) {
                requestLayout()
            }
            onFooterEnabledListener?.onFooterEnabledState(enabled)
        }
    }

    interface OnFooterEnabledListener {
        fun onFooterEnabledState(enabled: Boolean)
    }

    class FooterLayoutBehavior : CoordinatorLayout.Behavior<FooterLayout> {

        constructor()
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            // If we're scrolling down, then steal movement from target
            val measuredHeight = child.measuredHeight
            if (dy < 0 && measuredHeight != 0) {
                val currentRatio = child.showRatio
                var diffRatio = dy diffAsRatioOf measuredHeight
                if (currentRatio > 0f) {
                    diffRatio = minOf(currentRatio, diffRatio)
                    doFooterDisplacement(child, diffRatio)
                    consumed[0] = 0
                    consumed[1] = diffRatio ratioDiffAsDisplacement measuredHeight
                }
            }
        }



        private fun doFooterDisplacement(child: FooterLayout, diffRatio: Float) {
            child.showRatio -= diffRatio
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
            val measuredHeight = child.measuredHeight
            if (dyUnconsumed > 0 && measuredHeight != 0) {
                val currentRatio = child.showRatio
                if (currentRatio < 1.0f) {
                    // user is trying to scroll up, and target view is not doing so..., move footer up
                    var diffRatio = dyUnconsumed diffAsRatioOf measuredHeight
                    diffRatio = maxOf(diffRatio, currentRatio - 1.0f)
                    doFooterDisplacement(child, diffRatio)
                }
            }
        }

        override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
            return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
        }

        override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, velocityX: Float, velocityY: Float): Boolean {
            return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        }

        override fun onNestedScrollAccepted(coordinatorLayout: CoordinatorLayout, child: FooterLayout, directTargetChild: View, target: View, axes: Int, type: Int) {
            super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, type: Int) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return (axes or View.SCROLL_AXIS_VERTICAL) != 0
        }

        companion object {
            private infix fun Int.diffAsRatioOf(height: Int): Float {
                check(height > 0) { "Invalid height, expected value greater than 0" }
                return (-1.0f * this) / height
            }

            private infix fun Float.ratioDiffAsDisplacement(height: Int): Int {
                return ((this * height) * -1.0).toInt()
            }
        }
    }
}