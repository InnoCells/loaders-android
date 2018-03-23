package com.inqbarna.rxutil.paging

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 21/03/2018
 */

interface PageFactory<out T> : PageGenerator<T> {
    val initialData: List<@JvmWildcard T>
}

