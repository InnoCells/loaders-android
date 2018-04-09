package com.inqbarna.adapters.internal

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 06/04/2018
 */
class DeferredOperation<out T> @JvmOverloads constructor(val type: Type, val data: T? = null) {

    override fun toString(): String {
        return "$type{data=$data}"
    }

    enum class Type {
        Keep, Placeholder
    }
}