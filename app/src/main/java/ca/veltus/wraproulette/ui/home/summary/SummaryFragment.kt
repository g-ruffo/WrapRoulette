package ca.veltus.wraproulette.ui.home.summary

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.MemberItem
import ca.veltus.wraproulette.data.objects.WinnerMemberItem
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.intToStringOrdinal
import ca.veltus.wraproulette.utils.toMemberItem
import ca.veltus.wraproulette.utils.toWinnerMemberItem
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs

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

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is WinnerMemberItem) {
            launchViewWinnerEmailDialog(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                    _viewModel.poolTotalBets.combine(_viewModel.currentTime) { bets, time ->
                        Pair(bets, time)
                    }.collectLatest {
                        val list = it.first
                        val time = it.second
                        setupBidsRecyclerView(list.toMemberItem()
                            .sortedBy { member -> abs(time.time - member.member.bidTime!!.time) })
                    }
                }
                launch {
                    _viewModel.poolWinningMembers.collectLatest {
                        if (it.isNotEmpty()) {
                            setupWinnersRecyclerView(it.toWinnerMemberItem())
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
            if (scrollY < 100) {
                _viewModel.setIsScrolling()
            } else {
                _viewModel.setIsScrolling(true)
            }
        }
    }

    private fun setupBidsRecyclerView(items: List<MemberItem>) {
        setPositionTextView(items)
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items)
        }
        binding.memberBidsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }

    private fun setPositionTextView(items: List<MemberItem>) {
        for (i in items.indices) {
            if (items[i].member.uid == _viewModel.userAccount.value!!.uid && items[i].member.displayName == _viewModel.userAccount.value!!.displayName) {
                binding.positionTextView.text = intToStringOrdinal(i + 1)
            }
        }
    }

    private fun setupWinnersRecyclerView(items: List<WinnerMemberItem>) {
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items)
            setOnItemClickListener(onItemClick)
        }
        binding.winnersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation)
        }
    }

    private fun launchViewWinnerEmailDialog(memberItem: WinnerMemberItem) {
        val email = memberItem.member.email
        if (email.isNullOrEmpty()) {
            _viewModel.showSnackBar.value = "No email found"
            return
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("${memberItem.member.displayName}'s Email:")
            builder.setMessage(memberItem.member.email)
            builder.setPositiveButton("Close") { _, _ -> }
            builder.show()
        }
    }
}