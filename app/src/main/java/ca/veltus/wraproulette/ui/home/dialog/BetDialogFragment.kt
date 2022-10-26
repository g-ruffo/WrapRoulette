package ca.veltus.wraproulette.ui.home.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import ca.veltus.wraproulette.databinding.FragmentBetDialogBinding
import ca.veltus.wraproulette.utils.FirestoreUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class BetDialogFragment(private val poolDate: Date) : DialogFragment() {
    companion object {
        private const val TAG = "BetDialogFragment"
    }

    private var _binding: FragmentBetDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBetDialogBinding.inflate(inflater, container, false)

        binding.timePickerSpinner.setIs24HourView(true)

        binding.cancelBetButton.setOnClickListener {
            dismiss()
        }
        binding.placeBetButton.setOnClickListener {
            val pickedDate = Calendar.getInstance().time
            pickedDate.hours = binding.timePickerSpinner.hour
            pickedDate.minutes = binding.timePickerSpinner.minute

            if (pickedDate.before(poolDate)) {
                pickedDate.date = pickedDate.date + 1
            }

            FirestoreUtil.getCurrentUser { user ->
                FirestoreUtil.setUserPoolBet(
                    user.activePool!!,
                    user.uid,
                    pickedDate
                ) {
                    dismiss()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}