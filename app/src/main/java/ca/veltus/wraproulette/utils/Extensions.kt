package ca.veltus.wraproulette.utils

import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.MemberItem
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.PoolItem
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

fun List<Member>.toMemberItem(): List<MemberItem> {
    return this.map {
        MemberItem(it)
    }
}

fun List<Pool>.toPoolItem(): List<PoolItem> {
    return this.map {
        PoolItem(it)
    }
}


inline fun ViewPager2.onPageSelected(
    lifecycleOwner: LifecycleOwner,
    crossinline listener: (position: Int) -> Unit
) {
    object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) = listener(position)
    }.let {
        LifecycleEventDispatcher(lifecycleOwner,
            onStart = { registerOnPageChangeCallback(it) },
            onStop = { unregisterOnPageChangeCallback(it) })
    }
}
