package ca.veltus.wraproulette.ui.pools.createpool

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAddPoolBinding
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddPoolFragment : BaseFragment() {
    companion object {
        private const val TAG = "AddPoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore

    private var _binding: FragmentAddPoolBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        databaseReference = FirebaseFirestore.getInstance()

        binding.viewModel = _viewModel

        binding.createButton.setOnClickListener {
            FirestoreUtil.createPool(
                _viewModel.poolProduction.value!!,
                _viewModel.poolPassword.value!!,
                _viewModel.poolDate.value!!
            ) {
                _viewModel.navigateBack()
            }
        }

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

        binding.selectDateAutoComplete.setOnClickListener {
            launchDatePickerDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

//                _viewModel.setPoolDate(calendar.time)
                _viewModel.setPoolDate(
                    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                        calendar.time
                    )
                )
            }

        val dialog = DatePickerDialog(
            requireContext(),
            datePickerListener,
            selectedYear,
            selectedMonth,
            selectedDay
        )

        dialog.show()
    }

}