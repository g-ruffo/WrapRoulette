package ca.veltus.wraproulette.ui.home.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.MessageItemFrom
import ca.veltus.wraproulette.data.objects.MessageItemTo
import ca.veltus.wraproulette.databinding.FragmentChatBinding
import ca.veltus.wraproulette.ui.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : BaseFragment() {
    companion object {
        private const val TAG = "ChatFragment"
    }

    private lateinit var binding: FragmentChatBinding
    override val _viewModel by viewModels<HomeViewModel>(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        Log.i(TAG, "onViewCreated: Called")
        lifecycleScope.launch {
            Log.i(TAG, "lifecycleScope: Called")
            _viewModel._chatList.collect {
                Log.i(TAG, "onViewCreated: $it")
                setupRecyclerView(it)
            }
        }

        binding.sendMessageButton.setOnClickListener {
            _viewModel.sendChatMessage {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        _viewModel.getChatData()
    }

    private fun setupRecyclerView(items: List<Message>) {
        val groupieAdapter = GroupieAdapter()
        items.forEach {
            if (it.senderUid == FirebaseAuth.getInstance().currentUser!!.uid) {
                groupieAdapter.add(MessageItemFrom(it))
            } else {
                groupieAdapter.add(MessageItemTo(it))
            }
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
            scrollToPosition(items.size - 1)
        }
    }

}