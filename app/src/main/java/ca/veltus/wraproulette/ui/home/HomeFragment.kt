package ca.veltus.wraproulette.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentHomeBinding
import ca.veltus.wraproulette.ui.home.dialog.BetDialogFragment
import ca.veltus.wraproulette.utils.onPageSelected
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
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
        Log.i(TAG, "onCreateView")

        setupViewPagerListener()

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ViewPagerAdapter.fragmentTitle[position]
        }.attach()


        binding.bidFab.setOnClickListener {
            val dialog = BetDialogFragment(_viewModel.getActivePoolDate())
            dialog.show(requireActivity().supportFragmentManager, "betDialog")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

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
}
