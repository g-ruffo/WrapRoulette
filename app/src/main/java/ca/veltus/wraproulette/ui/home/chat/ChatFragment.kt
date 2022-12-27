package ca.veltus.wraproulette.ui.home.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.MessageItemFrom
import ca.veltus.wraproulette.data.objects.MessageItemTo
import ca.veltus.wraproulette.databinding.FragmentChatBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.ui.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : BaseFragment() {

    override val _viewModel by viewModels<HomeViewModel>(ownerProducer = { requireParentFragment() })

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val activityCast by lazy { activity as WrapRouletteActivity }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.chatList.collect {
                    setupRecyclerView(it)
                }
            }
        }
        binding.sendMessageButton.setOnClickListener {
            activityCast.hideKeyboard(it)
            _viewModel.sendChatMessage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView(items: List<Message>) {
        val groupieAdapter = GroupieAdapter()
        items.forEach {
            // Check if the message is from or to the user and add the corresponding item to the adapter.
            if (it.senderUid == FirebaseAuth.getInstance().currentUser!!.uid) {
                groupieAdapter.add(MessageItemFrom(it))
            } else {
                groupieAdapter.add(MessageItemTo(it))
            }
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
            // Scroll to the bottom of the recycler view after each item is added.
            scrollToPosition(items.size - 1)
        }
    }
}