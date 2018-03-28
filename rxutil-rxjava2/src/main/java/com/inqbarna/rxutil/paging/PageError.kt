package com.inqbarna.rxutil.paging

import java.util.concurrent.atomic.AtomicBoolean

internal class PageError(
        override val requestedOffset: Int,
        override val error: Throwable,
        private val abortFun: () -> Unit,
        private val retry: () -> Retry
) : AtomicBoolean(false), PageErrorAction {
    override fun generateRetry(): Retry {
        if (compareAndSet(false, true)) {
            return retry()
        } else {
            throw IllegalStateException("You can only use that instance for generating or aborting an error once!")
        }
    }
    override fun abort() {
        if (compareAndSet(false, true)) {
            abortFun()
        }
    }
}