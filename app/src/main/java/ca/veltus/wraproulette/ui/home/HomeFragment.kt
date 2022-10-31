package ca.veltus.wraproulette.ui.home

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentHomeBinding
import ca.veltus.wraproulette.utils.FirestoreUtil
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
    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    override val _viewModel by viewModels<HomeViewModel>()
    private lateinit var menuHost: MenuHost
    private lateinit var fabView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        val adapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = adapter

        menuHost = requireActivity()

        menuHost.addMenuProvider(
            this@HomeFragment, viewLifecycleOwner, Lifecycle.State.STARTED
        )


        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ViewPagerAdapter.fragmentTitle[position]
        }.attach()


        binding.bidFab.setOnClickListener {
            launchStartTimePickerDialog()
        }

        binding.bitAdminFab.setOnClickListener {
            launchStartTimePickerDialog()
        }

        binding.setWrapAdminFab.setOnClickListener {
            launchStartTimePickerDialog(true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        fabView = binding.bidFab
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
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        Log.i(TAG, "onMenuItemSelected: ${menuItem.itemId}")

        return when (menuItem.itemId) {
            R.id.actionEditPool -> {
                _viewModel.navigateToEditPool()
                true
            }
            else -> {
                false
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: called")
    }

    private fun checkAdminStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.isPoolAdmin.collectLatest {
                    Log.i(TAG, "addMenuProvider: called $it")
                    fabView = when (it) {
                        true -> binding.adminFabLayout
                        false -> binding.bidFab
                    }
                }
            }
        }
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
                    _viewModel.actionbarTitle.collect {
                        (activity as AppCompatActivity).supportActionBar?.title = it
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

    private fun launchStartTimePickerDialog(setWrapTime: Boolean = false) {
        val time = Calendar.getInstance().time
        var submitButtonText = "Bet"
        val timePickerListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            time.hours = hourOfDay
            time.minutes = minute
            time.seconds = 0

            if (time.before(_viewModel.poolStartTime.value)) {
                time.date = time.date + 1
            }

            if (setWrapTime) {
                launchConfirmationDialog(time)
            } else {
                FirestoreUtil.getCurrentUser { user ->
                    FirestoreUtil.setUserPoolBet(
                        user.activePool!!, user.uid, time
                    ) {}
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
        if (setWrapTime) {
            submitButtonText = "Set Wrap Time"
            timePickerDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL, "Clear"
            ) { _, _ ->
                launchConfirmationDialog(null)
            }
        }
        timePickerDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, submitButtonText
        ) { _, _ -> }
        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Cancel"
        ) { _, _ -> }
        timePickerDialog.show()
    }

    private fun launchConfirmationDialog(time: Date?) {
        val format = SimpleDateFormat("HH:mm MMM d, yyyy", Locale.ENGLISH)
        var message = ""
        message = if (time == null) {
            "Are you sure you want to clear the set wrap time?"
        } else {
            "Please confirm the selected wrap time is correct: ${format.format(time)}"
        }
        MaterialAlertDialogBuilder(requireContext()).setTitle("Are You Sure?").setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                _viewModel.showToast.value = "true"
                _viewModel.setWrapTime(time, true)
            }.setNegativeButton("No") { _, _ ->
                _viewModel.showToast.value = "false"
                _viewModel.setWrapTime(time, false)
            }.show()
    }
}
