package ca.veltus.wraproulette.data.objects

import android.os.Parcelable
import java.io.Serializable
import java.sql.Timestamp
import java.util.*

data class Pool(
    var id: String = UUID.randomUUID().toString(),
    var production: String,
    var password: String,
    var admin: Boolean,
    var date: Timestamp,
    var bet: String?,
    var margin: Timestamp?,
    var lockTime: Timestamp?,
    var startTime: Timestamp,
    var endTime: Timestamp?
    ) : Serializable
