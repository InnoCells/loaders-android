package com.inqbarna.rxutil.paging

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 22/03/2018
 */
internal interface RetryCallbacks {
    fun onRetryRequested()
    fun onRetryAborted()
}