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
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.MemberItem
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import ca.veltus.wraproulette.utils.toMemberItem
import com.bumptech.glide.Glide
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
        setupScrollingListener()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _viewModel.poolTotalBets.collect {
                        setupRecyclerView(it.toMemberItem())
                    }
                }
                launch {
                    _viewModel.poolWinningMember.collectLatest {
                        if (it != null) {
                            binding.winnerMemberItem.member = it
                            Glide.with(this@SummaryFragment).load(
                                FirebaseStorageUtil.pathToReference(it.profilePicturePath!!)).placeholder(
                                R.drawable.ic_baseline_person_24).into(binding.winnerMemberItem.profilePictureImageView)

                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause:")
        _viewModel.setIsScrolling()
    }

    override fun onResume() {
        super.onResume()
        if (binding.summaryScrollView.scrollY > 100) {
            _viewModel.setIsScrolling(true)
        }
    }

    private fun setupScrollingListener() {
        binding.summaryScrollView.setOnScrollChangeListener { _, _, scrollY, _, s ->
            Log.i(TAG, "setupScrollingListener: $s")
            if (scrollY < 100) {
                _viewModel.setIsScrolling()
            } else {
                _viewModel.setIsScrolling(true)
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