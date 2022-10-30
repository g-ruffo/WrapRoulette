package ca.veltus.wraproulette.authentication.login

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ca.veltus.wraproulette.BuildConfig
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.databinding.FragmentLoginBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginFragment"
    }

    override val _viewModel: LoginSignupViewModel by activityViewModels()

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.viewModel = _viewModel

        firebaseAuth = FirebaseAuth.getInstance()

        observeLogin()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.signInButton.setOnClickListener {
            launchEmailSignIn()
            Log.i(TAG, "onViewCreated: ${BuildConfig.FIRESTORE_CURRENT_KEY}")
        }

    }

    // If email and password are valid pass login values to Firebase and log success or failure response.
    private fun launchEmailSignIn() {
        _viewModel.validateEmailAndPassword()

    }

    private fun observeLogin() {
        lifecycleScope.launchWhenStarted {
            _viewModel.loginFlow.collectLatest {
                when (it) {
                    is Result.Success -> {
                        Log.i(TAG, "observeLogin = Resource.Success")
                        startActivity(
                            Intent(requireContext(), WrapRouletteActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        requireActivity().finish()
                    }
                    is Result.Loading -> {
                        Log.i(TAG, "observeLogin = Resource.Loading")
                        CircularProgressIndicator(requireContext())
                    }
                    is Result.Failure -> {
                        Log.i(TAG, "observeLogin = Resource.Failure")
                        when (it.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                binding.passwordEditTextLayout.helperText =
                                    it.exception.message.toString()
                                binding.passwordEditTextLayout.setHelperTextColor(
                                    ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.warningRed
                                        )
                                    )
                                )
                            }
                            is FirebaseAuthInvalidUserException -> {
                                binding.emailEditTextLayout.helperText =
                                    it.exception.message.toString()
                                binding.emailEditTextLayout.setHelperTextColor(
                                    ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.warningRed
                                        )
                                    )
                                )
                            }
                            else -> {
                                _viewModel.showToast.value = it.exception.message
                            }
                        }
                    }
                }
            }

        }
    }
}