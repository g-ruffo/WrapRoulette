package ca.veltus.wraproulette.ui.home.bids

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentBidsBinding
import ca.veltus.wraproulette.databinding.MemberListItemBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.databinding.BindableItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BidsFragment : BaseFragment() {
    companion object {
        private const val TAG = "BidsFragment"
    }

    private var _binding: FragmentBidsBinding? = null
    override val _viewModel by viewModels<HomeViewModel>()
    private var shouldInitRecyclerView = true
    private lateinit var bidListenerRegistration: ListenerRegistration
    private lateinit var bidSection: Section

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBidsBinding.inflate(inflater, container, false)

        FirestoreUtil.getCurrentUser { user ->
            if (user.activePool == null) {
                return@getCurrentUser
            } else {
                bidListenerRegistration =
                    FirestoreUtil.addBidsListener(
                        requireActivity(),
                        user.activePool!!,
                        this::updateRecyclerView
                    )
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun onDestroy() {
        super.onDestroy()
        FirestoreUtil.removeListener(bidListenerRegistration)
    }


    private fun updateRecyclerView(items: List<BindableItem<MemberListItemBinding>>) {
        fun init() {
            binding.poolsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@BidsFragment.context)
                adapter = GroupieAdapter().apply {
                    bidSection = Section(items)
                    add(bidSection)
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