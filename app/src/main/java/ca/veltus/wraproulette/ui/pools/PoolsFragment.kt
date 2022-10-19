package ca.veltus.wraproulette.ui.pools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

        poolListenerRegistration =
            FirestoreUtil.addPoolsListener(requireActivity(), this::updateRecyclerView)

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

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(poolListenerRegistration)
        shouldInitRecyclerView = true
        _binding = null
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
        if (shouldInitRecyclerView) {
            init()
        } else {
            updateItems()
        }
    }
}