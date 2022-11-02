package ca.veltus.wraproulette.ui.home.bids

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
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
import ca.veltus.wraproulette.databinding.FragmentBidsBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.FirestoreUtil
import ca.veltus.wraproulette.utils.toMemberItem
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class BidsFragment : BaseFragment() {
    companion object {
        private const val TAG = "BidsFragment"
    }

    private var _binding: FragmentBidsBinding? = null
    override val _viewModel by viewModels<HomeViewModel>(ownerProducer = { requireParentFragment() })

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is MemberItem && _viewModel.isPoolAdmin.value && !item.member.tempMemberUid.isNullOrEmpty() && _viewModel.isPoolActive.value) {
            launchSetMemberBetDialog(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBidsBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel
        Log.i(TAG, "onCreateView: called")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.bids.collect {
                    Log.i(TAG, "onViewCreated: $it")
                    setupRecyclerView(it.toMemberItem(_viewModel.userAccount.value?.uid ?: ""))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: called")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: called")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: called")
        _binding = null
    }

    private fun setupRecyclerView(items: List<MemberItem>) {
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items.sortedByDescending { it.member.bidTime })
            setOnItemClickListener(onItemClick)
        }
        binding.poolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }

    private fun launchSetMemberBetDialog(memberItem: MemberItem) {
        val time = Calendar.getInstance().time
        val timePickerListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            time.hours = hourOfDay
            time.minutes = minute
            time.seconds = 0

            if (time.before(_viewModel.poolStartTime.value)) {
                time.date = time.date + 1
            }
            FirestoreUtil.setUserPoolBet(
                memberItem.member.poolId, memberItem.member.tempMemberUid!!, time
            ) {
                if (!it.isNullOrEmpty()) {
                    _viewModel.showToast.value = it
                }
            }
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            timePickerListener,
            time.hours,
            time.minutes,
            true
        )
        timePickerDialog.setTitle(memberItem.member.displayName)

        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEUTRAL, "Clear"
        ) { _, _ ->
            FirestoreUtil.setUserPoolBet(
                memberItem.member.poolId, memberItem.member.tempMemberUid!!, null
            ) {}
        }
        timePickerDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "Bet"
        ) { _, _ -> }
        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Cancel"
        ) { _, _ -> }
        timePickerDialog.show()
    }
}