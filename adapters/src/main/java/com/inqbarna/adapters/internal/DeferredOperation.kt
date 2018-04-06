package com.inqbarna.adapters.internal

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 06/04/2018
 */
class DeferredOperation<out T> @JvmOverloads constructor(val type: Type, val data: T? = null, var moveFromOffset: Int = 0) {

    override fun toString(): String {
        return "$type{data=$data, offset=$moveFromOffset}"
    }

    enum class Type {
        Unchanged, Add, Remove, Replace, MoveFrom
    }


}