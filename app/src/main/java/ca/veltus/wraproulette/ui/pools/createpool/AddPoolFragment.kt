package ca.veltus.wraproulette.ui.pools.createpool

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAddPoolBinding
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddPoolFragment : BaseFragment() {
    companion object {
        private const val TAG = "AddPoolsFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore

    private var _binding: FragmentAddPoolBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_pool, container, false)

        databaseReference = FirebaseFirestore.getInstance()

        binding.viewModel = _viewModel

        binding.createButton.setOnClickListener {
            Log.i(TAG, "onCreateView: ${_viewModel.poolProduction.value!!}")
            FirestoreUtil.createPool(_viewModel.poolProduction.value!!, _viewModel.poolPassword.value!!) {
                _viewModel.navigateBack()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}