package com.inqbarna.rxutil.paging

import io.reactivex.disposables.Disposable

internal sealed class DelegateState
internal class Delivering private constructor(val lastDelivered: Boolean, val prefetchTask: Disposable?) : DelegateState() {
    constructor(lastDelivered: Boolean) : this(lastDelivered, null)
    constructor(prefetchTask: Disposable) : this(false, prefetchTask)
    val prefetching: Boolean
        get() = null != prefetchTask

    override fun toString(): String {
        return "Delivering(lastDelivered: $lastDelivered, prefetching: $prefetching)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Delivering

        if (lastDelivered != other.lastDelivered) return false
        if (prefetchTask != other.prefetchTask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastDelivered.hashCode()
        result = 31 * result + (prefetchTask?.hashCode() ?: 0)
        return result
    }
}

internal data class Error(val error: Throwable, val recovery: ListenableRetry? = null) : DelegateState() {
    val hasRecovery: Boolean
        get() = null != recovery

    override fun toString(): String {
        return "Error(hasRecovery: $hasRecovery): ${error.message}"
    }
}

internal data class Loading(val task: Disposable) : DelegateState()
internal object Complete : DelegateState()
internal object None : DelegateState()
