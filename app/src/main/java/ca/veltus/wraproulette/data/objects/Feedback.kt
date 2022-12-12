package ca.veltus.wraproulette.data.objects

import java.util.*

data class Feedback(
    val message: String,
    val date: Date,
    val senderUid: String,
    val senderName: String,
    val senderEmail: String
) {
    constructor() : this("", Date(0), "", "", "")
}
