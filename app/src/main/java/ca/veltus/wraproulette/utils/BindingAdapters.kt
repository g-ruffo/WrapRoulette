package ca.veltus.wraproulette.utils

import android.content.res.ColorStateList
import android.util.Patterns
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.data.objects.Member
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

}

