package ca.veltus.wraproulette.ui.pools

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
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