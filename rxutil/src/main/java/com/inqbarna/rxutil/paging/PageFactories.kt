@file:JvmName("PageFactories")
package com.inqbarna.rxutil.paging

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 21/03/2018
 */

fun <T> emptyPageFactory(generator: PageGenerator<T>): PageFactory<T> = object : PageFactory<T>, PageGenerator<T> by generator {
    override val initialData: List<T> = emptyList()
}
