package ca.veltus.wraproulette.authentication.reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "ResetPasswordFrag"
    }

    override val _viewModel: LoginSignupViewModel by activityViewModels()

    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password, container, false)

        binding.viewModel = _viewModel

        firebaseAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

    }
}