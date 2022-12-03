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
import ca.veltus.wraproulette.data.objects.MemberBidItem
import ca.veltus.wraproulette.data.objects.WinnerMemberItem
import ca.veltus.wraproulette.databinding.FragmentSummaryBinding
import ca.veltus.wraproulette.databinding.OptionsDialogBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.Constants.ADMIN_DIALOG
import ca.veltus.wraproulette.utils.Constants.BID_DIALOG
import ca.veltus.wraproulette.utils.Constants.MARGIN_DIALOG
import ca.veltus.wraproulette.utils.Constants.PIR_DIALOG
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import ca.veltus.wraproulette.utils.intToStringOrdinal
import ca.veltus.wraproulette.utils.toMemberBidItem
import ca.veltus.wraproulette.utils.toWinnerMemberItem
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
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
    private val activityCast by lazy { activity as WrapRouletteActivity }

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
        binding.fragment = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupScrollingListener()

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _viewModel.poolBetsList.collectLatest {
                        if (!it.isNullOrEmpty()) setupBidsRecyclerView(it.toMemberBidItem())
                    }
                }
                launch {
                    _viewModel.poolWinningMembers.collectLatest {
                        setupWinnersRecyclerView(it.toWinnerMemberItem())
                    }
                }
                launch {
                    _viewModel.poolAdminProfileImage.collectLatest {
                        if (!it.isNullOrEmpty()) Glide.with(requireContext()).asBitmap()
                            .load(FirebaseStorageUtil.pathToReference(it))
                            .placeholder(R.drawable.no_profile_image_member)
                            .into(binding.adminImage)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i(TAG, "onDestroyView: called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
        if (binding.summaryScrollView.scrollY > 100) {
            _viewModel.setIsScrolling(true)
        }

        if (_viewModel.poolAdminProfileImage != null) {
            Glide.with(requireContext()).asBitmap()
                .load(FirebaseStorageUtil.pathToReference(_viewModel.poolAdminProfileImage.value!!))
                .placeholder(R.drawable.no_profile_image_member).into(binding.adminImage)
        }
    }

    fun showPoolDetailDialog(dialogValue: Int) {
        if (_viewModel.currentPool.value == null) {
            _viewModel.showToast.value = "The pool has not loaded yet."
        } else {
            var title = ""
            var description = ""
            when (dialogValue) {
                BID_DIALOG -> {
                    title = getString(R.string.bidAmountDialogTitle)
                    description = getString(
                        R.string.bidAmountDialogMessage, _viewModel.currentPool.value?.betAmount
                    )
                }
                ADMIN_DIALOG -> {
                    title = getString(R.string.poolAdminDialogTitle)
                    description = getString(
                        R.string.poolAdminDialogMessage, _viewModel.currentPool.value?.adminName
                    )
                }
                MARGIN_DIALOG -> {
                    title = getString(R.string.bettingMarginDialogTitle)
                    description = getString(
                        R.string.bettingMarginDialogPoolMessage,
                        _viewModel.currentPool.value?.margin
                    )
                }
                PIR_DIALOG -> {
                    title = getString(R.string.priceIsRightRules)
                    description = getString(
                        R.string.priceIsRightRulesDialogMessage,
                        _viewModel.currentPool.value?.pIRRulesEnabled.toString()
                    )
                }
            }
            val builder = MaterialAlertDialogBuilder(
                activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
            )
            val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
            view.message.text = description
            view.title.text = title

            builder.apply {
                setView(view.root)
                setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            }.show()
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

    private fun setupBidsRecyclerView(items: List<MemberBidItem>) {
        setPositionTextView(items)
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items)
        }
        binding.memberBidsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation)
        }
    }

    private fun setPositionTextView(items: List<MemberBidItem>) {
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