package com.inqbarna.rxutil.paging

import org.reactivestreams.Publisher

interface PageGenerator<out T> {
    fun nextPageObservable(startOffset: Int, pageSize: Int): Publisher<out T>
}