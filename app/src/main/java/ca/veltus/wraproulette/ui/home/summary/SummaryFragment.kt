package ca.veltus.wraproulette.ui.home.summary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding


class SummaryFragment : Fragment() {
    companion object {
        private const val TAG = "SummaryFragment"
    }

    private var _binding: FragmentSummaryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)

        return binding.root
    }

}