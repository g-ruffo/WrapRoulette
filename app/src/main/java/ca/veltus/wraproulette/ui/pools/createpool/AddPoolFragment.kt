package ca.veltus.wraproulette.ui.pools.createpool

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.*
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import ca.veltus.wraproulette.utils.Constants
import ca.veltus.wraproulette.utils.temporaryFocus
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddPoolFragment : BaseFragment() {
    companion object {
        private const val TAG = "AddPoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()

    private var _binding: FragmentAddPoolBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val activityCast by lazy { activity as WrapRouletteActivity }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        binding.viewModel = _viewModel
        binding.fragment = this

        checkForEditPoolArgs()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i(TAG, "onDestroyView: ")
    }

    private fun checkForEditPoolArgs() {
        val args = AddPoolFragmentArgs.fromBundle(requireArguments()).poolId
        if (args != null) {
            _viewModel.loadEditPool(args)
            binding.subtitleText.text = getString(R.string.editPoolSubtitleText)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.poolDocUid.collectLatest {
                    if (it.isNullOrBlank()) {
                        activityCast.supportActionBar!!.title = getString(R.string.newPool)
                    } else {
                        binding.createButton.text = "Update"
                        activityCast.supportActionBar!!.title = getString(R.string.editPool)
                    }
                }
            }
        }
    }

    fun launchDeletePoolAlert() {
        val message = "You are about to delete this pool. Click Yes to continue or No to cancel."
        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.message.text = message
        view.title.text = "Are You Sure?"
        builder.apply {
            setView(view.root)
            setNeutralButton("No") { dialog, _ -> dialog.dismiss() }
            setPositiveButton("Yes") { _, _ ->
                _viewModel.deletePool()
            }
        }.show()
    }

    fun launchPirInfoDialog() {
        val message =
            "The Price Is Right rules picks a winner that has the closest bet time to the wrap time without going over."

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.message.text = message
        view.title.text = "Price Is Right Rules"
        builder.apply {
            setView(view.root)
            setNeutralButton("Close") { dialog, _ -> dialog.dismiss() }
        }.show()
    }

    fun launchTimePickerDialog(isStartTime: Boolean = true, editText: View) {
        editText.temporaryFocus()

        val time = Calendar.getInstance()
        time.set(Calendar.SECOND, 0)
        time.set(Calendar.MILLISECOND, 0)

        val submitButtonText = "Set"
        val titleText: String
        val messageText: String

        when (isStartTime) {
            true -> {
                titleText = "Start Time"
                messageText = "Set the pools start time according to the call sheet"
            }
            false -> {
                titleText = "Lock Betting"
                messageText = "At this time no additional bets can be made and are final."
            }
        }

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = TimePickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.apply {
            title.text = titleText
            message.text = messageText
            timePicker.setIs24HourView(true)
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        }
        builder.apply {
            setView(view.root)
            setNeutralButton("Close") { dialog, _ -> dialog.dismiss() }
            setPositiveButton(submitButtonText) { _, _ -> }
            if (!(editText as MaterialAutoCompleteTextView).text.isNullOrEmpty() && !isStartTime) {
                setNegativeButton("Clear") { _, _ ->
                    _viewModel.clearPoolTimeAndAmount(Constants.SET_FINAL_BETS)
                }
            }
        }

        val dialog = builder.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            time.set(Calendar.HOUR_OF_DAY, view.timePicker.hour)
            time.set(Calendar.MINUTE, view.timePicker.minute)

            _viewModel.setPoolTime(Date(time.timeInMillis), isStartTime)

            dialog.dismiss()
        }

        dialog.setOnDismissListener { editText.clearFocus() }
    }

    // Launch date dialog and listen for its result.
    fun launchDatePickerDialog(editText: View) {
        editText.temporaryFocus()

        val calendar = Calendar.getInstance()

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = DatePickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.apply {
            datePicker.minDate = calendar.time.time - Constants.YEAR
            datePicker.maxDate = calendar.time.time + Constants.YEAR
        }
        builder.apply {
            setView(view.root)
            setNeutralButton("Close") { dialog, _ -> dialog.dismiss() }
            setPositiveButton("Set") { _, _ -> }
        }

        val dialog = builder.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            view.datePicker.apply {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            _viewModel.setPoolDate(formatter.format(calendar.time))
            dialog.dismiss()
        }

        dialog.setOnDismissListener { editText.clearFocus() }
    }

    fun launchNumberPickerDialog(isTimeMargin: Boolean = false, editText: View) {
        editText.temporaryFocus()

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )

        if (isTimeMargin) {
            val view =
                MarginNumberPickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
            builder.setView(view.root)
            val array: Array<String> =
                resources.getStringArray(R.array.margin_number_picker_entries)
            view.numberPickerMinutes.displayedValues = array
            view.numberPickerMinutes.maxValue = array.size - 1
            builder.setPositiveButton("Set") { _, _ ->
                _viewModel.setPoolPriceAndMargin(
                    view.numberPickerMinutes.displayedValues[view.numberPickerMinutes.value], false
                )
            }.setNeutralButton("Cancel") { dialogCancel, _ -> dialogCancel.dismiss() }
        } else {
            val view = BetNumberPickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
            builder.setView(view.root)
            builder.setPositiveButton("Set") { _, _ ->
                var result = view.numberPickerOnes.value.toString()
                if (view.numberPickerTens.value != 0) result =
                    view.numberPickerTens.value.toString() + result

                _viewModel.setPoolPriceAndMargin(result, true)
            }.setNeutralButton("Cancel") { dialogCancel, _ -> dialogCancel.dismiss() }
        }
        if (!(editText as MaterialAutoCompleteTextView).text.isNullOrEmpty()) {
            builder.setNegativeButton("Clear") { _, _ ->
                if (isTimeMargin) _viewModel.clearPoolTimeAndAmount(Constants.SET_MARGIN_TIME)
                else _viewModel.clearPoolTimeAndAmount(Constants.SET_BET_AMOUNT)
            }
        }
        val dialog = builder.show()

        dialog.setOnDismissListener { editText.clearFocus() }
    }
}