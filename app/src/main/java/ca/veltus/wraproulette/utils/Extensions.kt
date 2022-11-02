package ca.veltus.wraproulette.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
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

// Animate changing the view visibility
fun View.fadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeIn.alpha = 1f
        }
    })
}

// Animate changing the view visibility
fun View.fadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOut.alpha = 1f
            this@fadeOut.visibility = View.GONE
        }
    })
}


fun List<Member>.toMemberItem(userUid: String = ""): List<MemberItem> {
    return this.map {
        MemberItem(it, userUid)
    }
}

fun List<Member>.toWinnerMemberItem(): List<WinnerMemberItem> {
    return this.map {
        WinnerMemberItem(it)
    }
}

fun List<Pool>.toPoolItem(user: User): List<PoolItem> {
    return this.map {
        PoolItem(it, user)
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

fun getTimeStringFromLong(time: Long): String {
    val seconds = (time / 1000) % 60
    val minutes = (time / (1000 * 60) % 60)
    val hours = (time / (1000 * 60 * 60) % 24)

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
    return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it -> it.service.className == service.name }
}
