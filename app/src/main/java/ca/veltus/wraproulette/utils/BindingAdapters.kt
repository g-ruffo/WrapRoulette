package ca.veltus.wraproulette.utils

import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.data.objects.Member
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
            view.text = "No Bet"
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
            view.text = "NA"
        }
    }

    @BindingAdapter("currentTimeFromDateToString")
    @JvmStatic
    fun currentTimeFromDateToString(view: TextView, date: Date?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.text = time
        }
    }

    @BindingAdapter("getTimeStringFromLong")
    @JvmStatic
    fun getTimeStringFromLong(view: TextView, time: Long?) {
        if (time == null) {
            view.text = "Time Not Set"
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
            val dateFormatter = SimpleDateFormat("EEEE MMMM dd, yyyy", Locale.ENGLISH)
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
        value = ["setCurrentTimeTitleIsActive", "setCurrentTimeTitleWinners"], requireAll = true
    )
    @JvmStatic
    fun setCurrentTimeTitle(view: TextView, isActive: Boolean, list: List<Member>) {
        if (isActive) {
            view.text = "Current Time"
        }
        if (!isActive && list.isEmpty()) {
            view.text = "No Winners"
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
}

