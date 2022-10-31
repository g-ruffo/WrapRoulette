package ca.veltus.wraproulette.ui.pools.createpool

import android.app.AlertDialog.BUTTON_NEUTRAL
import android.app.AlertDialog.THEME_HOLO_DARK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAddPoolBinding
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddPoolFragment : BaseFragment() {
    companion object {
        private const val TAG = "AddPoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()

    private lateinit var binding: FragmentAddPoolBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        binding.viewModel = _viewModel

        checkForEditPoolArgs()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.selectDateAutoComplete.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.performClick()
            }
        }
        binding.selectStartTimeAutoComplete.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.performClick()
            }
        }
        binding.selectBettingCloseTimeAutoComplete.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.performClick()
            }
        }
//        binding.selectBetAmountAutoComplete.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                v.performClick()
//            }
//        }

        binding.selectDateAutoComplete.setOnClickListener {
            launchDatePickerDialog()
        }

        binding.selectStartTimeAutoComplete.setOnClickListener {
            launchStartTImePickerDialog()
        }

        binding.selectBettingCloseTimeAutoComplete.setOnClickListener {
            launchBetLockTimePickerDialog()
        }
//        binding.selectBetAmountAutoComplete.setOnClickListener {
//            launchBetAmountNumberDialog()
//        }

    }

    private fun checkForEditPoolArgs() {
        val args = AddPoolFragmentArgs.fromBundle(requireArguments()).poolId
        if (args != null) {
            _viewModel.loadEditPool(args)
            binding.createNewPoolTitle.text = "Edit Pool"
            binding.createButton.text = "Update"
            binding.createNewPoolSubtitle.text = "Make Changes to Your Pool"
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Edit Pool"
        } else {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Create Pool"
        }
    }

    private fun launchBetLockTimePickerDialog() {
        val time = Calendar.getInstance()
        var selectedHour = time.get(Calendar.HOUR_OF_DAY)
        var selectedMinute = time.get(Calendar.MINUTE)

        val timePickerListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                time.set(Calendar.HOUR_OF_DAY, hourOfDay)
                time.set(Calendar.MINUTE, minute)
                time.set(Calendar.SECOND, 0)

                _viewModel.setPoolBetLockTime(Date(time.timeInMillis))
            }

        val dialog = TimePickerDialog(
            requireContext(),
            THEME_HOLO_DARK,
            timePickerListener,
            selectedHour,
            selectedMinute,
            true
        )
        dialog.setButton(BUTTON_NEUTRAL, "Clear") { _, _ ->
            _viewModel.setPoolBetLockTime(null)
        }
        dialog.show()
    }

    private fun launchStartTImePickerDialog() {
        val time = Calendar.getInstance()
        var selectedHour = time.get(Calendar.HOUR_OF_DAY)
        var selectedMinute = time.get(Calendar.MINUTE)

        val timePickerListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                time.set(Calendar.HOUR_OF_DAY, hourOfDay)
                time.set(Calendar.MINUTE, minute)
                time.set(Calendar.SECOND, 0)

                _viewModel.setPoolStartTime(Date(time.timeInMillis))
            }

        TimePickerDialog(
            requireContext(),
            THEME_HOLO_DARK,
            timePickerListener,
            selectedHour,
            selectedMinute,
            true
        ).show()
    }

    // Launch date dialog and listen for its result.
    private fun launchDatePickerDialog() {
        val calendar = Calendar.getInstance()
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH)
        var selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                _viewModel.setPoolDate(
                    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                        calendar.time
                    )
                )
            }

        DatePickerDialog(
            requireContext(),
            THEME_HOLO_DARK,
            datePickerListener,
            selectedYear,
            selectedMonth,
            selectedDay
        ).show()
    }

}