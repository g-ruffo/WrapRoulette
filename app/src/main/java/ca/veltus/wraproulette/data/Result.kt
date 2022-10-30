package ca.veltus.wraproulette.data

sealed class Result<out R> {
    data class Success<out R>(val result: R): Result<R>()
    data class Failure(val exception: Exception): Result<Nothing>()
    object Loading: Result<Nothing>()

}
