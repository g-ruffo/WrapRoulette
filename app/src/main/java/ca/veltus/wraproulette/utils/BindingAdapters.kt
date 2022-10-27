package ca.veltus.wraproulette.utils

import android.content.res.ColorStateList
import android.util.Patterns
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    fun getTimeStringFromLong(view: TextView, time: Long) {
        val seconds = (time / 1000) % 60
        val minutes = (time / (1000 * 60) % 60)
        val hours = (time / (1000 * 60 * 60) % 24)

        view.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @BindingAdapter("convertDateToSummaryTitle")
    @JvmStatic
    fun convertDateToDetail(view: TextView, date: String?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dateFormatter = SimpleDateFormat("EEEE MMM d, yyyy", Locale.ENGLISH)
            val dateObject = parsedDate.parse(date)
            val convertedDate = dateFormatter.format(dateObject!!)
            view.text = convertedDate
        }
    }

    @BindingAdapter("getTimeFromDate")
    @JvmStatic
    fun getTimeFromDate(view: AutoCompleteTextView, date: Date?) {
        if (date != null) {
            val parsedDate = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = parsedDate.format(date)
            view.setText(time)
        }
    }

    @BindingAdapter(
        value = ["calculatePoolPotSizePrice", "calculatePoolPotSizeList"],
        requireAll = true
    )
    @JvmStatic
    fun calculatePoolPotSize(view: TextView, bidPrice: String?, list: MutableList<Member>) {
        if (bidPrice != null) {
            val price = bidPrice.toInt()
            val total = "$${(price * list.size)}"
            view.text = total
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
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }

    @BindingAdapter(
        value = ["setExpandableFabVisibleState", "setExpandableFabEnabledState"],
        requireAll = true
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
        value = ["setExpandableFabState", "setExpandableFabAdminState"],
        requireAll = true
    )
    @JvmStatic
    fun setExpandableFabState(
        view: ExtendedFloatingActionButton,
        isClicked: Boolean,
        isAdmin: Boolean
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
}

