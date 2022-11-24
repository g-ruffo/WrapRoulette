package ca.veltus.wraproulette.utils

import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Pool
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object BindingAdapters {
    @BindingAdapter(
        value = ["emailHelperTextColorSwitcher", "emailTextInputEditTextStatus"], requireAll = true
    )
    @JvmStatic
    fun setEmailHelperTextColor(view: TextInputLayout, text: String?, editText: TextInputEditText) {
        val context = view.context
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!Patterns.EMAIL_ADDRESS.matcher(text.toString())
                    .matches() && !hasFocus && text != "null" && !text.isNullOrEmpty()
            ) {
                view.setHelperTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.warningRed)
                    )
                )
                view.helperText = context.getString(R.string.invalidEmailAddressHelper)

            } else {
                view.setHelperTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.optionalGrey)
                    )
                )
                view.helperText = null
            }
        }
    }

    @BindingAdapter("dateToStringConverter")
    @JvmStatic
    fun dateToStringConverter(view: TextView, date: Date?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.text = time
        } else {
            view.text = "--:--"
        }
    }

    @BindingAdapter("dateToStringDayConverter")
    @JvmStatic
    fun dateToStringDayConverter(view: TextView, date: Date?) {
        if (date != null) {
            val dateFormatter = SimpleDateFormat("EEEE, d MMM yyyy", Locale.ENGLISH)
            val day = dateFormatter.format(date)
            view.text = day
        }
    }

    @BindingAdapter("dateToStringTimeConverter")
    @JvmStatic
    fun dateToStringTimeConverter(view: TextView, date: Date?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.text = time
        } else {
            view.text = "--:--"
        }
    }

    @BindingAdapter(
        value = ["currentTimeFromDateToString", "isPoolActiveTime"], requireAll = false
    )
    @JvmStatic
    fun currentTimeFromDateToString(view: TextView, date: Date?, poolActive: Boolean = true) {
        if (date != null && poolActive) {
            val parsedDate = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.text = time
        } else {
            view.text = "--:--"
        }
    }

    @BindingAdapter("getTimeStringFromLong")
    @JvmStatic
    fun getTimeStringFromLong(view: TextView, time: Long?) {
        if (time == null) {
            view.text = "--:--"
        } else {
            val seconds = (time / 1000) % 60
            val minutes = (time / (1000 * 60) % 60)
            val hours = (time / (1000 * 60 * 60) % 24)

            view.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    @BindingAdapter("convertDateToSummaryTitle")
    @JvmStatic
    fun convertDateToDetail(view: TextView, date: String?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val dateObject = parsedDate.parse(date)
            val convertedDate = dateFormatter.format(dateObject!!)
            view.text = convertedDate
            view.isSelected = true
            view.ellipsize = TextUtils.TruncateAt.MARQUEE
            view.isSingleLine = true
            view.marqueeRepeatLimit = -1
            view.setHorizontallyScrolling(true)
        }
    }

    @BindingAdapter("convertDateToPoolItem")
    @JvmStatic
    fun convertDateToPoolItem(view: TextView, date: String?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val dateObject = parsedDate.parse(date)
            val convertedDate = dateFormatter.format(dateObject!!)
            view.text = convertedDate
            view.isSelected = true
            view.ellipsize = TextUtils.TruncateAt.MARQUEE
            view.isSingleLine = true
            view.marqueeRepeatLimit = -1
            view.setHorizontallyScrolling(true)
        }
    }

    @BindingAdapter(
        value = ["setCurrentTimeTitleIsActive", "setCurrentTimeTitleWinners", "setCurrentTimeTitleWrapTime"],
        requireAll = true
    )
    @JvmStatic
    fun setCurrentTimeTitle(
        view: TextView, isActive: Boolean, list: List<Member>, wrapTime: Date?
    ) {
        if (isActive && wrapTime == null) {
            view.text = "Current Time"
        } else if (!isActive && list.isEmpty() && wrapTime != null) {
            view.text = "No Winners"
        } else if (!isActive && wrapTime == null) {
            view.text = "Error, Wrap Time Not Set"
        } else if (!isActive && list.size < 2) {
            view.text = "Winner"
        } else {
            view.text = "Winners"
        }
    }

    @BindingAdapter(
        value = ["setTempMemberUidVisibility", "setTempMemberBetTimeVisibility"], requireAll = false
    )
    @JvmStatic
    fun setTempMemberButtonVisibility(view: ImageButton, tempMemberUid: String?, betTime: Date?) {
        if (tempMemberUid != null && betTime == null) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE

        }
    }

    @BindingAdapter("getTimeFromDate")
    @JvmStatic
    fun getTimeFromDate(view: AutoCompleteTextView, date: Date?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.setText(time)
        } else {
            view.text.clear()
        }
    }

    @BindingAdapter(
        value = ["calculatePoolPotSizePrice", "calculatePoolPotSizeList"], requireAll = true
    )
    @JvmStatic
    fun calculatePoolPotSize(view: TextView, bidPrice: String?, list: MutableList<Member>) {
        if (!bidPrice.isNullOrEmpty()) {
            val price = bidPrice.toInt()
            val total = "$${(price * list.size)}"
            view.text = total
        } else {
            view.text = "$0"
        }
    }

    @BindingAdapter("android:calculatePoolItemPotSize")
    @JvmStatic
    fun calculatePoolItemPotSize(view: TextView, pool: Pool) {
        val totalBets = mutableListOf<String>()
        pool.bets.forEach { if (it.value != null) totalBets.add(it.key) }
        val formatter = DecimalFormat("$###0.00")
        val bidAmount = pool.betAmount?.toInt() ?: 0
        val total = formatter.format(bidAmount * totalBets.size)
        view.text = "$total"
    }

    @BindingAdapter(
        value = ["calculateTotalPoolBetsList", "calculateTotalPoolBetsUid"], requireAll = true
    )
    @JvmStatic
    fun calculateTotalPoolBets(view: TextView, pools: List<Pool>?, uid: String?) {
        val totalBets = mutableListOf<String>()
        if (pools != null && uid != null) {
            pools.forEach {
                it.bets.forEach { bet ->
                    if (bet.value != null && bet.key == uid) totalBets.add(
                        bet.value.toString()
                    )
                }
            }
        }
        view.text = totalBets.size.toString()
    }

    @BindingAdapter(
        value = ["calculateTotalWinningsList", "calculateTotalWinningsUid"], requireAll = true
    )
    @JvmStatic
    fun calculateTotalWinnings(view: TextView, pools: List<Pool>?, uid: String?) {
        val totalBets = mutableListOf<Int>()
        val formatter = DecimalFormat("$###0.00")
        if (pools != null && uid != null) {
            pools.forEach { pool ->
                pool.winners.forEach { winner ->
                    if (winner.uid == uid && winner.tempMemberUid == null) totalBets.add(
                        winner.winnings ?: 0
                    )
                }
            }
        }

        view.text = formatter.format(totalBets.sum())
    }

    // Use this binding adapter to show and hide the views using boolean variables.
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE) view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE) view.fadeOut()
            }
        }
    }

    @BindingAdapter("setSizeAndEnabled")
    @JvmStatic
    internal fun FloatingActionButton.setSizeAndEnabled(message: String?) {
        this.isEnabled = !(message.isNullOrEmpty() || message.isNullOrBlank())
    }

    @BindingAdapter("error")
    @JvmStatic
    internal fun TextInputLayout.setError(errorMessage: ErrorMessage<String>?) {
        this.isErrorEnabled = errorMessage != null
        this.isHelperTextEnabled = errorMessage != null

        when (errorMessage) {
            is ErrorMessage.HelperText -> {
                helperText = errorMessage.message.takeUnless { it == null }
                boxStrokeWidth = 0
                this.isHelperTextEnabled = true
            }
            is ErrorMessage.ErrorText -> {
                error = errorMessage.message.takeUnless { it == null }
                boxStrokeWidth = 1
                this.isErrorEnabled = true
            }
            else -> {
                boxStrokeWidth = 0
                this.isErrorEnabled = errorMessage != null
                this.isHelperTextEnabled = false
                this.isErrorEnabled = false
            }
        }

        boxStrokeWidthFocused = boxStrokeWidth
    }

    @BindingAdapter(
        value = ["setExpandableFabVisibleState", "setExpandableFabEnabledState"], requireAll = true
    )
    @JvmStatic
    fun setExpandableFabViews(view: FloatingActionButton, visible: Boolean, enabled: Boolean) {
        if (visible && enabled) {
            view.isClickable = true
            view.show()
        } else {
            view.hide()
            view.isClickable = false
        }
    }

    @BindingAdapter(
        value = ["setExpandableFabVisibleTextViewsState", "setExpandableFabEnabledTextViewsState"],
        requireAll = true
    )
    @JvmStatic
    fun setExpandableFabTextViews(view: View, visible: Boolean, enabled: Boolean) {
        if (visible && enabled) {
            view.visibility = View.VISIBLE
            view.isEnabled = true
        } else {
            view.visibility = View.GONE
            view.isEnabled = false
        }
    }

    @BindingAdapter(
        value = ["setExpandableFabState", "setExpandableFabAdminState"], requireAll = true
    )
    @JvmStatic
    fun setExpandableFabState(
        view: ExtendedFloatingActionButton, isClicked: Boolean, isAdmin: Boolean
    ) {
        if (isAdmin) {
            view.visibility = View.VISIBLE
            view.isEnabled = true
            if (isClicked) {
                view.extend()
            } else {
                view.shrink()
            }
        } else {
            view.visibility = View.GONE
            view.isEnabled = false
        }
    }


    @BindingAdapter("isScrolling")
    @JvmStatic
    fun setIsScrolling(view: View, isScrolling: Boolean) {
        if (view.isVisible) {
            if (isScrolling) {
                view.animate().translationX(400f).alpha(0f).setDuration(200)
                    .setInterpolator(AccelerateInterpolator()).start()
            } else {
                view.animate().translationX(0F).alpha(1f).setDuration(200)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
    }

    @BindingAdapter("setPoolItemImage")
    @JvmStatic
    fun setPoolItemImage(view: ImageView, pool: Pool) {
        val context = view.context
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateObject = parsedDate.parse(pool.date)

        if (pool.endTime != null) {
            view.setBackgroundColor(getColor(context, R.color.poolItemCompleteGreen))
            view.setImageDrawable(
                ContextCompat.getDrawable(
                    context, R.drawable.ic_baseline_verified_24
                )
            )
        } else if (pool.startTime == null || (Calendar.getInstance().time.time - dateObject.time) < (Constants.DAY * 2)) {
            view.setBackgroundColor(getColor(context, R.color.poolItemProgressBlue))
            view.setImageDrawable(
                ContextCompat.getDrawable(
                    context, R.drawable.ic_baseline_pending_24
                )
            )
        } else {
            view.setBackgroundColor(getColor(context, R.color.poolItemErrorRed))
            view.setImageDrawable(
                ContextCompat.getDrawable(
                    context, R.drawable.ic_baseline_new_releases_24
                )
            )
        }

    }
}

