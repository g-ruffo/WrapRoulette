package ca.veltus.wraproulette.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import ca.veltus.wraproulette.data.objects.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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

fun intToStringOrdinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> i.toString() + "th"
        else -> i.toString() + suffixes[i % 10]
    }
}


inline fun ViewPager2.onPageSelected(
    lifecycleOwner: LifecycleOwner, crossinline listener: (position: Int) -> Unit
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
    return (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Integer.MAX_VALUE)
        .any { it -> it.service.className == service.name }
}

fun String.convertDateToDetail() : String {
    return if (this != null) {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        val dateObject = parsedDate.parse(this)
        dateFormatter.format(dateObject!!)
    } else {
        "--:--"
    }
}

fun List<Member>.calculateWinners(
    numberOfBets: Int, bidPrice: Int, margin: String, pIREnabled: Boolean, wrapTime: Date
): List<Member> {
    val totalBetList = mutableListOf<Member>()
    val winnersList = mutableListOf<Member>()
    val marginInt = margin.toInt()
    val memberWinnings = numberOfBets * bidPrice

    if (this.isNotEmpty()) {
        this.forEach { if (it.bidTime != null) totalBetList.add(it) }
        when (pIREnabled) {
            true -> {
                totalBetList.sortBy { wrapTime.time - it.bidTime!!.time }
                totalBetList.removeAll { (wrapTime.time - it.bidTime!!.time) + ((marginInt / 2) * Constants.MINUTE) < 0 }
                val minimumDistance = abs(wrapTime.time - totalBetList[0].bidTime!!.time)
                for (i in totalBetList.indices) {
                    if (abs((wrapTime.time - totalBetList[i].bidTime!!.time)) <= minimumDistance + ((marginInt / 2) * Constants.MINUTE)) {
                        winnersList.add(totalBetList[i])
                    }
                }
            }
            false -> {
                totalBetList.sortBy { abs(wrapTime.time - it.bidTime!!.time) }
                val minimumDistance = abs(wrapTime.time - totalBetList[0].bidTime!!.time)
                for (i in totalBetList.indices) {
                    if (abs((wrapTime.time - totalBetList[i].bidTime!!.time)) <= minimumDistance + ((marginInt / 2) * Constants.MINUTE)) {
                        winnersList.add(totalBetList[i])
                    }
                }
            }
        }
    }

    winnersList.forEach {
        it.winnings = when (numberOfBets) {
            0 -> memberWinnings
            else -> memberWinnings / winnersList.size
        }
    }

    return winnersList
}
