package com.inqbarna.rxutil.paging

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val DEBUG: Boolean = false

/**
 * Created by david on 17/09/16.
 */
internal class RequestState<T>(val pageSize: Int, private val pageFactory: PageFactory<T>) : Disposable
{
    private var mOffset: Int = 0
    private var currentRequest: Disposable? = null

    private val unboundedQueue: Queue<T>
    private val lock: Lock = ReentrantLock()
    private val itemsCondition: Condition = lock.newCondition()

    private val delegateDisposable = Disposables.fromAction {
        lock.withLock {
            currentRequest?.dispose()
            currentRequest = null
        }
    }

    override fun isDisposed(): Boolean = delegateDisposable.isDisposed
    override fun dispose() = delegateDisposable.dispose()

    var completed: Boolean = false
        get() = lock.withLock { field }
        private set(value) {
            lock.withLock {
                field = value
            }
        }

    var error: Throwable? = null
        get() = lock.withLock { field }
        private set(value) {
            lock.withLock { field = value }
        }

    init {
        val initialData = pageFactory.initialData
        unboundedQueue = LinkedList(initialData)
        mOffset = initialData.size
    }

    fun blockingNext(): T? {

        lock.withLock {
            try {
                val size = unboundedQueue.size
                if (size < pageSize) {
                    debug("Remaining only %d items", size)
                    setupRequestNext()
                }

                return if (size > 0) {
                    unboundedQueue.poll()
                } else {
                    while (!completed && null == error && hasActiveRequest()) {
                        debug("Awaiting for elements to arrive")
                        itemsCondition.await()
                    }

                    if (unboundedQueue.size == 0) {
                        null
                    } else {
                        unboundedQueue.poll()
                    }
                }
            } catch (e: InterruptedException) {
                if (null == error) {
                    error = e
                }
                debug("Blocking next error exit...")
                return null
            }
        }
    }

    private fun hasActiveRequest(): Boolean = lock.withLock { null != currentRequest }

    private fun setupRequestNext(): Boolean {
        return lock.withLock {
            if (null == currentRequest && !completed && error == null) {
                currentRequest = Flowable.fromPublisher(pageFactory.nextPageObservable(mOffset, pageSize))
                        .doFinally {
                            lock.withLock {
                                currentRequest = null
                                itemsCondition.signalAll()
                                debug("Cleanup previous request")
                            }
                        }
                        .subscribeWith(
                                object : DisposableSubscriber<T>() {
                                    var delivered: Int = 0
                                    override fun onComplete() {
                                        lock.withLock {
                                            if (delivered == 0) {
                                                completed = true
                                            } else {
                                                if (delivered > pageSize) {
                                                    Timber.e("Error paging, delivered %d items out of the %d requested", delivered, pageSize)
                                                } else {
                                                    debug("Delivered %d items", delivered)
                                                }
                                                mOffset += delivered
                                            }
                                            debug("Request completed...")
                                            dispose()
                                        }
                                    }

                                    override fun onNext(t: T) {
                                        lock.withLock {
                                            unboundedQueue.offer(t)
                                            debug("Received new item %s", t)
                                            delivered++
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        lock.withLock {
                                            completed = false
                                            if (null == error) {
                                                error = e
                                            }
                                            debug("Error!!")
                                            dispose()
                                        }
                                    }
                                }
                        )
            } else {
                debug("Request 'requested' but not started!! (hasRequest: %s, complete: %s, error: %s)", currentRequest != null, completed, error != null)
            }
            currentRequest != null
        }
    }
}

@SuppressLint("TimberArgCount")
private fun debug(fmt: String, vararg args: Any?) {
    if (DEBUG) {
        Timber.d("[%s] $fmt", Thread.currentThread().name, *args)
    }
}
