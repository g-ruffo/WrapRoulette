package ca.veltus.wraproulette.data.objects

import java.io.Serializable

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val department: String?,
    val profilePicturePath: String?,
    val pools: MutableMap<String, Any>? = null
) {
    constructor(): this("", "", "", null, null, null)
}