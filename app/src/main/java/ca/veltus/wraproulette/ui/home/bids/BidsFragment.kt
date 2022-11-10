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
import ca.veltus.wraproulette.databinding.FragmentAddMemberDialogBinding
import ca.veltus.wraproulette.databinding.FragmentBidsBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.toMemberItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        if (item is MemberItem && _viewModel.isPoolAdmin.value && !item.member.tempMemberUid.isNullOrEmpty()) {
            if (!_viewModel.isPoolActive.value) _viewModel.showSnackBar.value =
                "This pool has finished, you are unable to make changes at this time."
            else launchTempMemberOptionsDialog(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBidsBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.bids.collect {
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

    private fun launchTempMemberOptionsDialog(memberItem: MemberItem) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Select the BET button to set bid time for ${memberItem.member.displayName}, or click EDIT to modify or delete this member.")
        builder.setPositiveButton("Bet") { _, _ ->
            launchSetMemberBetDialog(memberItem)
        }
        builder.setNegativeButton("Edit") { _, _ ->
            launchUpdateTempMemberDialog(memberItem)
        }
        builder.setNeutralButton("Close") { _, _ -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    private fun launchUpdateTempMemberDialog(memberItem: MemberItem) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = FragmentAddMemberDialogBinding.inflate(LayoutInflater.from(context))
        dialogBinding.viewModel = _viewModel
        dialogBinding.member = memberItem.member
        builder.setView(dialogBinding.root)
        _viewModel.loadTempMemberValues(memberItem.member)
        val dialog: AlertDialog = builder.show()

        dialog.setOnDismissListener { _viewModel.loadTempMemberValues(null) }

        dialogBinding.addMemberButton.setOnClickListener {
            if (_viewModel.createUpdateTempMember(memberItem)) {
                dialog.dismiss()
            }
        }
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.deleteButton.setOnClickListener {
            if (memberItem.member.bidTime == null) {
                launchDeleteMemberConfirmationDialog(memberItem)
            } else if (!_viewModel.isBettingOpen.value && _viewModel.isPoolActive.value) {
                _viewModel.showSnackBar.value =
                    "Betting has locked and pool is still active, you are unable to remove this member at this time."
            } else if (_viewModel.isBettingOpen.value && _viewModel.userBetTime.value != null) {
                _viewModel.showSnackBar.value =
                    "You need to clear your bet before leaving this pool"
                launchSetMemberBetDialog(memberItem)
            } else {
                _viewModel.showSnackBar.value = "You are unable to remove this member at this time"
            }
            dialog.dismiss()
        }
    }

    private fun launchSetMemberBetDialog(memberItem: MemberItem) {
        val time = Calendar.getInstance()
        val timePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            time.set(Calendar.HOUR_OF_DAY, hourOfDay)
            time.set(Calendar.MINUTE, minute)
            time.set(Calendar.SECOND, 0)
            time.set(Calendar.MILLISECOND, 0)

            if (time.time.before(_viewModel.poolStartTime.value)) {
                time.add(Calendar.DATE, 1)
            }

            _viewModel.setMemberPoolBet(
                memberItem.member.poolId, memberItem.member.tempMemberUid!!, time.time
            )

        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            timePickerListener,
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            true
        )
        if (memberItem.member.bidTime != null) {
            timePickerDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL, "Clear"
            ) { _, _ ->
                _viewModel.setMemberPoolBet(
                    memberItem.member.poolId, memberItem.member.tempMemberUid!!, null
                )
            }
        }
        timePickerDialog.setTitle(memberItem.member.displayName)

        timePickerDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "Bet"
        ) { _, _ -> }
        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Cancel"
        ) { _, _ -> }
        timePickerDialog.show()
    }

    private fun launchDeleteMemberConfirmationDialog(memberItem: MemberItem) {
        val message = "Are you sure you want to delete this member for the pool?"
        MaterialAlertDialogBuilder(requireContext()).setTitle("Are You Sure?").setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                _viewModel.deleteTempMember(memberItem)
            }.setNegativeButton("No") { _, _ -> }.show()
    }
}