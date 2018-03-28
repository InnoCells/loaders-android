package com.inqbarna.rxutil.paging

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 22/03/2018
 */
interface ErrorHandlingModel {
    fun processError(error: PageErrorAction)
}

interface Retry {
    fun doRetry()
    fun abortRetry()
}

interface PageErrorAction {
    val error: Throwable
    val requestedOffset: Int
    fun abort()
    fun generateRetry(): Retry
}