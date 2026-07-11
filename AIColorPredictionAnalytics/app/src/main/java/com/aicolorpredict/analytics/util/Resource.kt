package com.aicolorpredict.analytics.util

/**
 * Generic UI state wrapper. Used by every ViewModel so the screens have a
 * consistent way to express loading / empty / error / data.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data object Empty : Resource<Nothing>()

    inline fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Loading -> Loading
        is Empty -> Empty
        is Error -> this
        is Success -> Success(transform(data))
    }

    fun getOrNull(): T? = (this as? Success<T>)?.data
}
