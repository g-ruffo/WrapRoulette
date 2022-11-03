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
import ca.veltus.wraproulette.databinding.FragmentAddMemberDialogBinding
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

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ViewPagerAdapter.fragmentTitle[position]
        }.attach()


        binding.bidFab.setOnClickListener {
            launchBetAndWrapDialog()
        }

        binding.bitAdminFab.setOnClickListener {
            launchBetAndWrapDialog()
        }

        binding.setWrapAdminFab.setOnClickListener {
            launchBetAndWrapDialog(true)
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

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.actionEditPool -> {
                if (_viewModel.isPoolActive.value) {
                    _viewModel.navigateToEditPool()
                } else {
                    _viewModel.showSnackBar.value =
                        "Pool has finished, you are unable to edit details."
                }
                true
            }
            R.id.actionAddMember -> {
                if (_viewModel.isPoolActive.value) {
                    launchAddMemberDialog()
                } else {
                    _viewModel.showSnackBar.value =
                        "Pool has finished, you are unable to add anymore members."
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
        Log.i(TAG, "onPause: called")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: called")
    }

    private fun checkAdminStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.isPoolAdmin.collectLatest {
                    when (it) {
                        true -> {
                            fabView = binding.adminFabLayout
                            if (!::menuHost.isInitialized) {
                                menuHost = requireActivity()
                            }
                            menuHost.removeMenuProvider(this@HomeFragment)
                            menuHost.invalidateMenu()
                            menuHost.addMenuProvider(
                                this@HomeFragment, viewLifecycleOwner, Lifecycle.State.STARTED
                            )
                        }
                        false -> {
                            fabView = binding.bidFab
                            if (::menuHost.isInitialized) {
                                menuHost.removeMenuProvider(this@HomeFragment)
                                menuHost.invalidateMenu()
                            }
                        }
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

    private fun launchAddMemberDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = FragmentAddMemberDialogBinding.inflate(LayoutInflater.from(context))
        dialogBinding.viewModel = _viewModel
        builder.setView(dialogBinding.root)
        val dialog: AlertDialog = builder.show()

        dialogBinding.addMemberButton.setOnClickListener {
            if (_viewModel.createNewPoolMember()) {
                dialog.dismiss()
            }
        }
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun launchBetAndWrapDialog(setWrapTime: Boolean = false) {
        val time = Calendar.getInstance()

        val submitButtonText = when (setWrapTime) {
            true -> "Set Wrap"
            false -> "Bet"
        }
        val timePickerListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            time.set(Calendar.HOUR_OF_DAY, hourOfDay)
            time.set(Calendar.MINUTE, minute)
            time.set(Calendar.SECOND, 0)
            time.set(Calendar.MILLISECOND, 0)

            if (time.time.before(_viewModel.poolStartTime.value)) {
                time.add(Calendar.DATE, 1)
            }

            if (setWrapTime) {
                launchConfirmationDialog(time.time)
            } else {
                FirestoreUtil.getCurrentUser { user ->
                    FirestoreUtil.setUserPoolBet(
                        user.activePool!!, user.uid, time.time
                    ) {
                        if (!it.isNullOrEmpty()) {
                            _viewModel.showToast.value = it
                        }
                    }
                }
            }
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            timePickerListener,
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEUTRAL, "Clear"
        ) { _, _ ->
            if (setWrapTime) {
                launchConfirmationDialog(null)
            } else {
                FirestoreUtil.getCurrentUser { user ->
                    FirestoreUtil.setUserPoolBet(
                        user.activePool!!, user.uid, null
                    ) {
                        if (!it.isNullOrEmpty()) {
                            _viewModel.showToast.value = it
                        }
                    }
                }
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
                _viewModel.setWrapTime(time, true)
            }.setNegativeButton("No") { _, _ ->
                _viewModel.setWrapTime(time, false)
            }.show()
    }
}
