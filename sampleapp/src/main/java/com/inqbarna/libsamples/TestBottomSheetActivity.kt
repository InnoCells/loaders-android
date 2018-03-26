package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.inqbarna.adapters.BasicBindingAdapter
import com.inqbarna.adapters.BasicItemBinder
import com.inqbarna.libsamples.databinding.ActivityBottomSheetBinding
import com.inqbarna.libsamples.vm.TestVM

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 24/03/2018
 */
class TestBottomSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomSheetBinding
    private lateinit var model: TestBottomVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_sheet)
        model = TestBottomVM()
        binding.model = model
    }

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(context, TestBottomSheetActivity::class.java)
        }
    }
}


class TestBottomVM {
    val adapter: RecyclerView.Adapter<*>
        get() = _adapter

    private val _adapter: BasicBindingAdapter<TestVM> = BasicBindingAdapter(BasicItemBinder(BR.model))

    init {
        _adapter.setItems((0..50).map { TestVM(it) })
    }
}


class LinearLayoutBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<LinearLayout>() {
    private var mIsAnimatingOut = false
    private val mInvert: Boolean = false

    protected val isInvert: Boolean
        get() = false

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: LinearLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        return (nestedScrollAxes or ViewCompat.SCROLL_AXIS_VERTICAL) != 0 || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: LinearLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
            dyUnconsumed: Int, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                // User scrolled down and the view is currently visible -> hide the view
                animateOut(child)
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                // User scrolled up and the view is currently not visible -> show the view
                animateIn(child)
            }
        } else {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        }
    }

    protected fun animateOut(view: View) {
        val measuredHeight = view.measuredHeight
        if (measuredHeight == 0) {
            return
        }
        ViewCompat.animate(view).translationY((if (isInvert) -measuredHeight else measuredHeight).toFloat()).alpha(0.0f).setInterpolator(INTERPOLATOR).withLayer()
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = true
                    }

                    override fun onAnimationCancel(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = false
                    }

                    override fun onAnimationEnd(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = false
                        view.visibility = View.INVISIBLE
                    }
                }).start()
    }

    private fun animateIn(linearLayout: LinearLayout) {
        linearLayout.setVisibility(View.VISIBLE)
        ViewCompat.animate(linearLayout).translationY(0f).alpha(1.0f)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start()
    }

    companion object {
        private val INTERPOLATOR = FastOutSlowInInterpolator()
    }
}
