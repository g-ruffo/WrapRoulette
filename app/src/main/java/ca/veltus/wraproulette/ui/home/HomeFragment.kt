package ca.veltus.wraproulette.ui.home

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
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
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class HomeFragment : BaseFragment(), MenuProvider {
    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    override val _viewModel by viewModels<HomeViewModel>()

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
            launchStartTImePickerDialog()
        }

        binding.bitAdminFab.setOnClickListener {
            launchStartTImePickerDialog()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupViewPagerListener()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: called")
    }


    private fun setupViewPagerListener() {
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
                binding.viewPager.onPageSelected(viewLifecycleOwner) { position ->
                    launch {
                        _viewModel.isPoolAdmin.collectLatest {
                            if (it) {
                                activity?.addMenuProvider(
                                    this@HomeFragment,
                                    viewLifecycleOwner,
                                    Lifecycle.State.RESUMED
                                )
                                Log.i(TAG, "addMenuProvider: called")
                            }
                            val fabView = when (it) {
                                true -> binding.adminFabLayout
                                false -> binding.bidFab
                            }
                            if (position == 2) {
                                binding.tabLayout.getTabAt(2)!!.removeBadge()
                                fabView.animate().translationX(400f).alpha(0f)
                                    .setDuration(200).setInterpolator(AccelerateInterpolator())
                                    .withEndAction {
                                        fabView.visibility = View.GONE
                                    }.start()

                            } else {
                                if (fabView.visibility == View.GONE) {
                                    fabView.visibility = View.VISIBLE
                                    fabView.animate().translationX(0F).alpha(1f)
                                        .setDuration(200)
                                        .setInterpolator(AccelerateInterpolator()).start()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchStartTImePickerDialog() {
        val time = Calendar.getInstance().time
        val timePickerListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                time.hours = hourOfDay
                time.minutes = minute
                time.seconds = 0

                if (time.before(_viewModel.poolStartTime.value)) {
                    time.date = time.date + 1
                }

                FirestoreUtil.getCurrentUser { user ->
                    FirestoreUtil.setUserPoolBet(
                        user.activePool!!,
                        user.uid,
                        time
                    ) {
                    }
                }
            }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_DARK,
            timePickerListener,
            time.hours,
            time.minutes,
            true
        )
        timePickerDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            "Bet"
        ) { _, _ -> }

        timePickerDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Cancel"
        ) { dialog, which ->
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss()
            }
        }
        timePickerDialog.show()
    }
}
