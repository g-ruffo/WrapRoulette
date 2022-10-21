package ca.veltus.wraproulette.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentHomeBinding
import ca.veltus.wraproulette.ui.home.dialog.BetDialogFragment
import ca.veltus.wraproulette.utils.onPageSelected
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    override val _viewModel by viewModels<HomeViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        val adapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ViewPagerAdapter.fragmentTitle[position]
        }.attach()


        binding.bidFab.setOnClickListener {
            var dialog = BetDialogFragment()
            dialog.show(requireActivity().supportFragmentManager, "betDialog")
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        setupViewPagerListener()
    }


    private fun setupViewPagerListener() {
        binding.viewPager.onPageSelected(viewLifecycleOwner) { position ->
            if (position == 2) {
                binding.bidFab.animate()
                    .translationY(400f)
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        binding.bidFab.visibility = View.GONE
                    }
                    .start()

            } else {
                if (binding.bidFab.visibility == View.GONE) {
                    binding.bidFab.visibility = View.VISIBLE
                    binding.bidFab.animate()
                        .translationY(0F)
                        .alpha(1f)
                        .setDuration(200)
                        .setInterpolator(AccelerateInterpolator())
                        .start()
                }
            }
        }
    }

}