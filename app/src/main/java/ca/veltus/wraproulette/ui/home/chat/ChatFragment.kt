package ca.veltus.wraproulette.ui.home.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.FragmentChatBinding
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding

class ChatFragment : Fragment() {
    companion object {
        private const val TAG = "ChatFragment"
    }

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }



}