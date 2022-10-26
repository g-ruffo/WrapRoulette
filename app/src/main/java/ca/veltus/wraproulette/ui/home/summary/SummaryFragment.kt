package ca.veltus.wraproulette.ui.home.summary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.MemberItem
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.toMemberItem
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SummaryFragment : BaseFragment() {
    companion object {
        private const val TAG = "SummaryFragment"
    }

    private var _binding: FragmentSummaryBinding? = null
    override val _viewModel by viewModels<HomeViewModel>(ownerProducer = { requireParentFragment() })

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.poolTotalBets.collect {
                    Log.i(TAG, "onViewCreated: ${it}")
                    setupRecyclerView(it.toMemberItem())
                }
            }
        }
    }


    private fun setupRecyclerView(items: List<MemberItem>) {
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items.sortedBy { it.member.bidTime })
        }
        binding.memberBidsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }
}