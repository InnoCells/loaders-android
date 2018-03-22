package com.inqbarna.rxutil.paging

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val DEBUG: Boolean = true

/**
 * Created by david on 17/09/16.
 */
internal class RequestState<T>(val pageSize: Int, private val pageFactory: PageFactory<T>) : Disposable
{
    private var mOffset: Int = 0
    private var currentRequest: Disposable? = null

    private val unboundedQueue: BlockingQueue<T>
    private val lock: Lock = ReentrantLock()

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
        unboundedQueue = LinkedBlockingQueue(initialData)
        mOffset = initialData.size
    }

    fun blockingNext(): T? {
        try {
            val size = unboundedQueue.size
            return if (size >= pageSize) {
                unboundedQueue.poll()
            } else if (size == 0 && completed) {
                null
            } else {
                if (setupRequestNext()) unboundedQueue.take() else null
            }
        } catch (e: InterruptedException) {
            lock.withLock {
                if (null == error) {
                    error = e
                }
            }
            return null
        }

    }

    private fun setupRequestNext(): Boolean {
        return lock.withLock {
            if (null == currentRequest && !completed && error == null) {
                currentRequest = Flowable.fromPublisher(pageFactory.nextPageObservable(mOffset, pageSize))
                        .doFinally { lock.withLock { currentRequest = null } }
                        .subscribeWith(
                                object : DisposableSubscriber<T>() {
                                    var delivered: Int = 0
                                    override fun onComplete() {
                                        if (delivered == 0) {
                                            completed = true
                                        } else {
                                            if (delivered > pageSize) {
                                                Timber.e("Error paging, delivered %d items out of the %d requested", delivered, pageSize)
                                            } else if (DEBUG) {
                                                Timber.d("Delivered %d items", delivered)
                                            }
                                            mOffset += delivered
                                        }
                                    }

                                    override fun onNext(t: T) {
                                        unboundedQueue.offer(t)
                                        delivered++
                                    }

                                    override fun onError(e: Throwable) {
                                        lock.withLock {
                                            completed = false
                                            if (null == error) {
                                                error = e
                                            }
                                        }
                                    }
                                }
                        )
            }
            currentRequest != null
        }
    }
}
