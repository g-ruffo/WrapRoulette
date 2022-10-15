package ca.veltus.wraproulette.ui.pools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.databinding.FragmentPoolsBinding
import ca.veltus.wraproulette.databinding.PoolListItemBinding
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.databinding.BindableItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoolsFragment : BaseFragment() {
    companion object {
        private const val TAG = "PoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore

    private var shouldInitRecyclerView = true
    private lateinit var poolListenerRegistration: ListenerRegistration
    private lateinit var poolSection: Section

    private var _binding: FragmentPoolsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pools, container, false)

        databaseReference = FirebaseFirestore.getInstance()

        binding.viewModel = _viewModel

        poolListenerRegistration = FirestoreUtil.addPoolsListener(requireActivity(), this::updateRecyclerView)

        binding.fab.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToAddPoolFragment()))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(poolListenerRegistration)
        shouldInitRecyclerView = true
        _binding = null
    }

    private fun updateRecyclerView(items: List<BindableItem<PoolListItemBinding>>) {
        fun init() {
            binding.poolsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@PoolsFragment.context)
                adapter = GroupieAdapter().apply {
                    poolSection = Section(items)
                    add(poolSection)
                }
                
            }
            shouldInitRecyclerView = false
        }
        fun updateItems() {

        }
        if (shouldInitRecyclerView){
            init()
        }
        else {
            updateItems()
        }
    }


}