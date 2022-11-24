package ca.veltus.wraproulette.ui.pools.joinpool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.DatePickerDialogBinding
import ca.veltus.wraproulette.databinding.FragmentJoinPoolBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import ca.veltus.wraproulette.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class JoinPoolFragment : BaseFragment() {
    companion object {
        private const val TAG = "JoinPoolFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()
    private var _binding: FragmentJoinPoolBinding? = null
    private val activityCast by lazy { activity as WrapRouletteActivity }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_pool, container, false)

        binding.viewModel = _viewModel
        binding.fragment = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Launch date dialog and listen for its result.
    fun launchDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val submitButtonText = "Set"
        val titleText = "Pool Date"
        val messageText = "Set the pools date according to the call sheet"

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = DatePickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.apply {
            title.text = titleText
            message.text = messageText
            datePicker.minDate = calendar.time.time - Constants.YEAR
            datePicker.maxDate = calendar.time.time + Constants.YEAR
        }
        builder.apply {
            setView(view.root)
            setNeutralButton("Close") { dialog, _ -> dialog.dismiss() }
            setPositiveButton(submitButtonText) { dialog, _ -> }
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
    }
}