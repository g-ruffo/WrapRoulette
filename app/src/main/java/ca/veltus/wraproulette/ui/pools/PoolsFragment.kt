package ca.veltus.wraproulette.ui.pools

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.objects.PoolItem
import ca.veltus.wraproulette.databinding.FragmentPoolsBinding
import ca.veltus.wraproulette.utils.toPoolItem
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PoolsFragment : BaseFragment() {
    companion object {
        private const val TAG = "PoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_bottom_anim
        )
    }
    private var fabClicked = false

    private lateinit var binding: FragmentPoolsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pools, container, false)

        Log.i(TAG, "onCreateView: called")
        
        binding.viewModel = _viewModel



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: called")
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            _viewModel.pools.collect {
                setupRecyclerView(it.toPoolItem())
            }
        }
        // TODO: Move to ViewModel
        binding.newPoolFab.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToAddPoolFragment()))
        }

        // TODO: Move to ViewModel
        binding.joinPoolFab.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToJoinPoolFragment()))
        }
        // TODO: Move to ViewModel
        binding.expandFab.setOnClickListener {
            onExpandButtonClicked()
        }
    }

    // TODO: Move to ViewModel
    private fun onExpandButtonClicked() {
        setAnimation(fabClicked)
        setFabVisibility(fabClicked)
        setClickable(fabClicked)
        fabClicked = !fabClicked
    }

    // TODO: Move to ViewModel
    private fun setFabVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.newPoolFab.visibility = View.VISIBLE
            binding.joinPoolFab.visibility = View.VISIBLE
        } else {
            binding.newPoolFab.visibility = View.GONE
            binding.joinPoolFab.visibility = View.GONE
        }
    }

    // TODO: Move to ViewModel
    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.newPoolFab.startAnimation(fromBottom)
            binding.joinPoolFab.startAnimation(fromBottom)
            binding.expandFab.startAnimation(rotateOpen)
        } else {
            binding.newPoolFab.startAnimation(toBottom)
            binding.joinPoolFab.startAnimation(toBottom)
            binding.expandFab.startAnimation(rotateClose)
        }
    }

    // TODO: Move to ViewModel
    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.newPoolFab.isClickable = true
            binding.joinPoolFab.isClickable = true
        } else {
            binding.newPoolFab.isClickable = false
            binding.joinPoolFab.isClickable = false
        }
    }

    private fun setupRecyclerView(items: List<PoolItem>) {
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items)
        }
        binding.poolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }
}