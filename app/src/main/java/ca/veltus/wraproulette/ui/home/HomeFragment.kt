package ca.veltus.wraproulette.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.AddMemberDialogBinding
import ca.veltus.wraproulette.databinding.FragmentHomeBinding
import ca.veltus.wraproulette.databinding.OptionsDialogBinding
import ca.veltus.wraproulette.databinding.TimePickerDialogBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.utils.convertDateToDetail
import ca.veltus.wraproulette.utils.onPageSelected
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class HomeFragment : BaseFragment(), MenuProvider {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var adapter: ViewPagerAdapter? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    override val _viewModel by viewModels<HomeViewModel>()
    private val activityCast by lazy { activity as WrapRouletteActivity }
    private lateinit var menuHost: MenuHost

    private var _fabView: View? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val fabView get() = _fabView!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupMenuOptions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = _viewModel
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = adapter

        tabLayoutMediator =
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = ViewPagerAdapter.fragmentTitle[position]
            }
        tabLayoutMediator?.attach()

        _fabView = binding.bidFab

        checkAdminStatus()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.showNoData.collectLatest {
                    if (!it) {
                        setupViewPagerListenerAndToolbar()
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        if (_viewModel.isPoolAdmin.value) {
            menu.findItem(R.id.actionLeavePool).isVisible = false
            menu.findItem(R.id.actionEditPool).isVisible = true
            menu.findItem(R.id.actionAddMember).isVisible = true
            menu.findItem(R.id.actionEditPool).isEnabled = !_viewModel.showNoData.value
            menu.findItem(R.id.actionAddMember).isEnabled = !_viewModel.showNoData.value
        } else {
            menu.findItem(R.id.actionEditPool).isVisible = false
            menu.findItem(R.id.actionAddMember).isVisible = false
            menu.findItem(R.id.actionLeavePool).isVisible = true
            menu.findItem(R.id.actionLeavePool).isEnabled = !_viewModel.showNoData.value
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.actionEditPool -> {
                if (_viewModel.isPoolActive.value || _viewModel.poolEndTime.value == null) _viewModel.navigateToEditPool()
                else _viewModel.postSnackBarMessage(getString(R.string.poolFinishedCantEditMessage))
                true
            }
            R.id.actionAddMember -> {
                if (_viewModel.isPoolActive.value) launchAddMemberDialog()
                else _viewModel.postSnackBarMessage(getString(R.string.poolFinishedCantAddMemberMessage))
                true
            }
            R.id.actionLeavePool -> {
                if ((!_viewModel.isPoolAdmin.value && _viewModel.userBetTime.value == null) || (_viewModel.poolEndTime.value != null && !_viewModel.isPoolActive.value && !_viewModel.isPoolAdmin.value)) {
                    launchConfirmationDialog(null, true)
                } else if (!_viewModel.isBettingOpen.value && _viewModel.isPoolActive.value) {
                    _viewModel.postSnackBarMessage(getString(R.string.bettingLockedPoolActiveCantLeaveMessage))
                } else if (_viewModel.isBettingOpen.value && _viewModel.userBetTime.value != null) {
                    _viewModel.postSnackBarMessage(getString(R.string.clearBetToLeavePoolMessage))
                }
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activityCast.supportActionBar?.subtitle = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        _fabView = null
        binding.viewPager.adapter = null
        adapter = null
        _binding = null
    }

    private fun checkAdminStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.isPoolAdmin.collectLatest {
                    _fabView = when (it) {
                        true -> binding.adminFabLayout
                        false -> binding.bidFab
                    }
                }
            }
        }
    }

    private fun setupMenuOptions() {
        menuHost = requireActivity()
        menuHost.addMenuProvider(
            this@HomeFragment, viewLifecycleOwner, Lifecycle.State.STARTED
        )
    }

    private fun setupViewPagerListenerAndToolbar() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _viewModel.chatList.combine(_viewModel.readChatListItems) { chatList, readList ->
                        Pair(chatList, readList.first)
                    }.collectLatest {
                        when (binding.viewPager.currentItem) {
                            2 -> {
                                binding.tabLayout.getTabAt(2)!!.removeBadge()
                                _viewModel.markMessagesAsRead(false)
                            }
                            else -> {
                                if (it.first.size > it.second.size) {
                                    binding.tabLayout.getTabAt(2)!!.orCreateBadge.number =
                                        it.first.size - it.second.size
                                } else {
                                    binding.tabLayout.getTabAt(2)!!.removeBadge()
                                }
                            }
                        }
                    }
                }
                launch {
                    _viewModel.actionbarTitle.collectLatest {
                        activityCast.supportActionBar?.title = it
                        activityCast.supportActionBar?.subtitle =
                            _viewModel.currentPool.value?.date?.convertDateToDetail() ?: ""
                    }
                }
            }
        }

        binding.viewPager.onPageSelected(viewLifecycleOwner) { position ->
            if (position == 2) {
                binding.tabLayout.getTabAt(2)!!.removeBadge()
                fabView.animate().translationX(400f).alpha(0f).setDuration(200)
                    .setInterpolator(AccelerateInterpolator()).withEndAction {
                        fabView.visibility = View.GONE
                    }.start()

            } else {
                if (fabView.visibility == View.GONE) {
                    fabView.visibility = View.VISIBLE
                    fabView.animate().translationX(0F).alpha(1f).setDuration(200)
                        .setInterpolator(AccelerateInterpolator()).start()
                }
            }
        }
    }

    private fun launchAddMemberDialog() {
        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = AddMemberDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.viewModel = _viewModel
        view.lifecycleOwner = this.viewLifecycleOwner
        builder.apply {
            setView(view.root)
            setNeutralButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.create)) { _, _ -> }
        }

        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (_viewModel.createUpdateTempMember()) {
                dialog.dismiss()
            }
        }
    }

    fun launchBetAndWrapDialog(setWrapTime: Boolean = false) {
        val time = Calendar.getInstance()
        time.set(Calendar.SECOND, 0)
        time.set(Calendar.MILLISECOND, 0)

        val submitButtonText: String
        val titleText: String
        val messageText: String

        when (setWrapTime) {
            true -> {
                submitButtonText = getString(R.string.wrap)
                titleText = getString(R.string.setWrapTimeDialogTitle)
                messageText = getString(R.string.setWrapTimeDialogSubtitle)
            }
            false -> {
                submitButtonText = getString(R.string.bet)
                titleText = getString(R.string.setBetTimeDialogTitle)
                messageText = getString(R.string.setBetTimeDialogSubtitle)
            }
        }

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = TimePickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.apply {
            title.text = titleText
            message.text = messageText
            timePicker.setIs24HourView(true)
            timePicker.hour = time.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = time.get(Calendar.MINUTE)
        }
        builder.apply {
            setView(view.root)
            setNeutralButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(submitButtonText) { _, _ -> }
        }

        if ((_viewModel.userBetTime.value != null && !setWrapTime) || (setWrapTime && _viewModel.poolEndTime.value != null)) {
            builder.setNegativeButton(getString(R.string.clear)) { _, _ -> }
        }

        val dialog = builder.show()

        dialog.apply {
            getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                if (setWrapTime) launchConfirmationDialog(null)
                else _viewModel.setUserPoolBet(null)

                dialog.dismiss()
            }

            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                time.set(Calendar.HOUR_OF_DAY, view.timePicker.hour)
                time.set(Calendar.MINUTE, view.timePicker.minute)

                if (time.time.before(_viewModel.poolStartTime.value)) time.add(Calendar.DATE, 1)
                if (setWrapTime) launchConfirmationDialog(time.time)
                else _viewModel.setUserPoolBet(time.time)

                dialog.dismiss()
            }
        }
    }

    private fun launchConfirmationDialog(time: Date?, isLeavePool: Boolean = false) {
        val format = SimpleDateFormat("HH:mm MMM d, yyyy", Locale.ENGLISH)
        val message: String = if (time == null && !isLeavePool) {
            getString(R.string.clearWrapConfirmDialogSubtitle)
        } else if (time != null && !isLeavePool) {
            getString(R.string.confirmWrapConfirmDialogSubtitle, format.format(time))
        } else {
            getString(R.string.leavePoolConfirmDialogSubtitle)
        }

        val builder = MaterialAlertDialogBuilder(
            activityCast, R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = OptionsDialogBinding.inflate(LayoutInflater.from(requireContext()))
        view.message.text = message
        view.title.text = getString(R.string.areYouSureDialogTitle)
        builder.apply {
            setView(view.root)
            setNeutralButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (!isLeavePool) _viewModel.setWrapTime(time, true)
                else {
                    _viewModel.setShowLoadingValue(true)
                    _viewModel.leavePool()
                }
            }
        }.show()
    }
}
