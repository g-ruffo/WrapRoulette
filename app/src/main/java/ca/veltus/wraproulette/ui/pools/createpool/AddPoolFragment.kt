package ca.veltus.wraproulette.ui.pools.createpool

import android.app.AlertDialog.THEME_HOLO_DARK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAddPoolBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
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
    private val activityCast by lazy { activity as WrapRouletteActivity }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        binding.viewModel = _viewModel
        binding.fragment = this

        checkForEditPoolArgs()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }

    private fun checkForEditPoolArgs() {
        val args = AddPoolFragmentArgs.fromBundle(requireArguments()).poolId
        if (args != null) {
            _viewModel.loadEditPool(args)
            binding.createButton.text = "Update"
            activityCast.supportActionBar!!.title = "Edit Pool"
        } else {
            activityCast.supportActionBar!!.title = "New Pool"
        }
    }

    fun launchDeletePoolAlert() {
        val dialog = AlertDialog.Builder(requireActivity())
        dialog.setTitle("Are You Sure?")
        dialog.setMessage("You are about to delete this pool. Click Yes to continue or No to cancel")
        dialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            _viewModel.deletePool()
        })
        dialog.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        dialog.show()
    }

    fun launchTimePickerDialog(isStartTime: Boolean = true) {
        val time = Calendar.getInstance()

        val timePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            time.set(Calendar.HOUR_OF_DAY, hourOfDay)
            time.set(Calendar.MINUTE, minute)
            time.set(Calendar.SECOND, 0)
            time.set(Calendar.MILLISECOND, 0)

            _viewModel.setPoolTime(Date(time.timeInMillis), isStartTime)
        }

        val dialog = TimePickerDialog(
            requireContext(),
            THEME_HOLO_DARK,
            timePickerListener,
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            true
        )
        dialog.show()
    }

    // Launch date dialog and listen for its result.
    fun launchDatePickerDialog() {
        val calendar = Calendar.getInstance()

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
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE)
        ).show()
    }
}