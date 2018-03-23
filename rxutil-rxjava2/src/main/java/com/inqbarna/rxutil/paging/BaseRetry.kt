package com.inqbarna.rxutil.paging

internal abstract class BaseRetry(protected val error: Throwable) : ListenableRetry {
    private var callbacks: RetryCallbacks? = null
    final override fun setCallbacks(callbacks: RetryCallbacks) {
        this.callbacks = callbacks
    }

    final override fun doRetry() {
        performRetry()
        callbacks?.onRetryRequested()
    }

    abstract fun performRetry()
}