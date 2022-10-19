package ca.veltus.wraproulette.ui.home.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentBetDialogBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BetDialogFragment : BaseFragment() {
    companion object {
        private const val TAG = "BetDialogFragment"
    }

    private var _binding: FragmentBetDialogBinding? = null
    override val _viewModel by viewModels<HomeViewModel>()


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



}