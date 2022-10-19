package ca.veltus.wraproulette.data.objects

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val department: String?,
    val profilePicturePath: String?,
    val activePool: String?,
    val pools: MutableMap<String, Any>? = null
) {
    constructor() : this("", "", "", null, null, null, null)
}