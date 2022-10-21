package ca.veltus.wraproulette.ui.home.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.databinding.FragmentBetDialogBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.FirestoreUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class BetDialogFragment : DialogFragment() {
    companion object {
        private const val TAG = "BetDialogFragment"
    }

    private var _binding: FragmentBetDialogBinding? = null
//    val viewMode by viewModels<HomeViewModel>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBetDialogBinding.inflate(inflater, container, false)

        binding.cancelBetButton.setOnClickListener {
            dismiss()
        }
        binding.placeBetButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, binding.timePickerSpinner.hour)
            calendar.set(Calendar.MINUTE, binding.timePickerSpinner.minute)
            FirestoreUtil.getCurrentUser { user ->
                FirestoreUtil.setUserPoolBet(user.activePool!!, user.uid, Date(calendar.time.time)) {
                    dismiss()
                }
            }
        }

        binding.timePickerSpinner.setIs24HourView(true)
        binding.timePickerSpinner.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.timePickerSpinner.minute = Calendar.getInstance().get(Calendar.MINUTE)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}