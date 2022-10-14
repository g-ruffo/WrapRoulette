package ca.veltus.wraproulette.data.objects

import java.io.Serializable
import java.sql.Timestamp
import java.util.*

data class Bet(
    var id: String = UUID.randomUUID().toString(),
    var userUid: String,
    var poolId: String,
    var betTime: Timestamp
) : Serializable
