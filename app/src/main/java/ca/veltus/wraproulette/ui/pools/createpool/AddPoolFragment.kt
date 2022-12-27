package ca.veltus.wraproulette.ui.pools.createpool

import android.os.Bundle
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

    override val _viewModel by viewModels<PoolsViewModel>()

    private var _binding: FragmentAddPoolBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val activityCast by lazy { activity as WrapRouletteActivity }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        checkForEditPoolArgs()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = _viewModel
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * If pool arguments are null, the user is trying to create a new pool. If not, they are editing
     * an existing pool and the action bar title and message is updated.
     */
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
                        binding.createButton.text = getString(R.string.update)
                        activityCast.supportActionBar!!.title = getString(R.string.editPool)
                    }
                }
            }
        }
    }

    /**
     * Displays a confirmation dialog to the user when the pool admin is trying to delete a pool they
     * created. Once confirmed the pool document is removed from Firestore.
     */
    fun launchDeletePoolAlert() {
        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.message.text = getString(R.string.deletePoolConfirmDialogSubtitle)
        view.title.text = getString(R.string.areYouSureDialogTitle)
        builder.apply {
            setView(view.root)
            setNeutralButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                _viewModel.deletePool()
            }
        }.show()
    }

    /**
     * Displays an alert dialog explaining The Price Is Right Rules to the user.
     */
    fun launchPirInfoDialog() {
        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.message.text = getString(R.string.pirInfoDialogSubtitle)
        view.title.text = getString(R.string.pirInfoDialogTitle)
        builder.apply {
            setView(view.root)
            setNeutralButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
        }.show()
    }

    /**
     * Launches an alert dialog for the user to select either the start or betting lock time for the pool
     * they are creating.
     */
    fun launchTimePickerDialog(isStartTime: Boolean = true, editText: View) {
        editText.temporaryFocus()

        val time = Calendar.getInstance()
        time.set(Calendar.SECOND, 0)
        time.set(Calendar.MILLISECOND, 0)

        val titleText: String
        val messageText: String

        when (isStartTime) {
            true -> {
                titleText = getString(R.string.startTimeDialogTitle)
                messageText = getString(R.string.startTimeDialogSubtitle)
            }
            false -> {
                titleText = getString(R.string.lockBettingDialogTitle)
                messageText = getString(R.string.lockBettingDialogSubtitle)
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
            setNeutralButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.set)) { _, _ -> }
            if (!(editText as MaterialAutoCompleteTextView).text.isNullOrEmpty() && !isStartTime) {
                setNegativeButton(getString(R.string.clear)) { _, _ ->
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

    /**
     * Launches an alert dialog for the user to select the pools date.
     */
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
            setNeutralButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.set)) { _, _ -> }
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

    /**
     * Launches an alert dialog for the user to select either the margin or betting amount for the pool
     * they are creating.
     */
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
            builder.setPositiveButton(getString(R.string.set)) { _, _ ->
                _viewModel.setPoolPriceAndMargin(
                    view.numberPickerMinutes.displayedValues[view.numberPickerMinutes.value], false
                )
            }
                .setNeutralButton(getString(R.string.cancel)) { dialogCancel, _ -> dialogCancel.dismiss() }
        } else {
            val view = BetNumberPickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
            builder.setView(view.root)
            builder.setPositiveButton(getString(R.string.set)) { _, _ ->
                var result = view.numberPickerOnes.value.toString()
                if (view.numberPickerTens.value != 0) result =
                    view.numberPickerTens.value.toString() + result

                _viewModel.setPoolPriceAndMargin(result, true)
            }
                .setNeutralButton(getString(R.string.cancel)) { dialogCancel, _ -> dialogCancel.dismiss() }
        }
        if (!(editText as MaterialAutoCompleteTextView).text.isNullOrEmpty()) {
            builder.setNegativeButton(getString(R.string.clear)) { _, _ ->
                if (isTimeMargin) _viewModel.clearPoolTimeAndAmount(Constants.SET_MARGIN_TIME)
                else _viewModel.clearPoolTimeAndAmount(Constants.SET_BET_AMOUNT)
            }
        }
        val dialog = builder.show()

        dialog.setOnDismissListener { editText.clearFocus() }
    }
}