package ca.veltus.wraproulette.ui.pools.joinpool

import android.app.AlertDialog.THEME_HOLO_DARK
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentJoinPoolBinding
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
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
        val selectedYear = calendar.get(Calendar.YEAR)
        val selectedMonth = calendar.get(Calendar.MONTH)
        val selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

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

        val dialog = DatePickerDialog(
            requireContext(),
            THEME_HOLO_DARK,
            datePickerListener,
            selectedYear,
            selectedMonth,
            selectedDay
        )

        dialog.show()
    }

}