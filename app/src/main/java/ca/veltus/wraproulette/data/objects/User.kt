package ca.veltus.wraproulette.data.objects

import java.io.Serializable

data class User(
    val uid: String,
    val userName: String,
    val pools: MutableMap<String, Any>? = null
) : Serializable
