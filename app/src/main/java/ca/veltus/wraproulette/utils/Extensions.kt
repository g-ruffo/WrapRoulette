package ca.veltus.wraproulette.utils

import ca.veltus.wraproulette.data.objects.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

// Firebase Authentication extension function
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            if (it.exception != null) {
                cont.resumeWithException(it.exception!!)
            } else {
                cont.resume(it.result, null)
            }
        }
    }
}

fun List<Member>.toMemberItem() : List<MemberItem> {
    return this.map {
        MemberItem(it)
    }
}

fun List<Pool>.toPoolItem() : List<PoolItem> {
    return this.map {
        PoolItem(it)
    }
}
