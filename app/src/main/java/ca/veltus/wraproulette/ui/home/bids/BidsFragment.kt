package ca.veltus.wraproulette.ui.home.bids

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.FragmentBidsBinding
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding


class BidsFragment : Fragment() {
    companion object {
        private const val TAG = "BidsFragment"
    }

    private var _binding: FragmentBidsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBidsBinding.inflate(inflater, container, false)

        return binding.root
    }

}