package ca.veltus.wraproulette.ui.pools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.PoolItem
import ca.veltus.wraproulette.databinding.FragmentPoolsBinding
import ca.veltus.wraproulette.utils.toPoolItem
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PoolsFragment : BaseFragment() {
    companion object {
        private const val TAG = "PoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()

    private lateinit var binding: FragmentPoolsBinding

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is PoolItem) {
            binding.poolsRecyclerView.isClickable = false
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.selectedPoolCardView
                )
            )
            _viewModel.setUsersActivePool(item.pool.docId) {
                binding.poolsRecyclerView.isClickable = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pools, container, false)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.userAccount.zip(_viewModel.pools) { user, pools ->
                    Pair(pools, user)
                }.collect {
                    if (it.second != null) {
                        setupRecyclerView(it.first.toPoolItem(it.second!!))
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(items: List<PoolItem>) {
        val groupieAdapter = GroupieAdapter().apply {
            addAll(items.sortedByDescending { it.pool.startTime })
            setOnItemClickListener(onItemClick)
        }
        binding.poolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }
}