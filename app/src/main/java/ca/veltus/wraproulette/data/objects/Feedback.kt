package ca.veltus.wraproulette.data.objects

import androidx.annotation.Keep
import java.util.*

@Keep
data class Feedback(
    val message: String,
    val date: Date,
    val senderUid: String,
    val senderName: String,
    val senderEmail: String,
    var resolved: Boolean = false
) {
    constructor() : this("", Date(0), "", "", "", false)
}
