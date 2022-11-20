package ca.veltus.wraproulette.authentication.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.databinding.FragmentLoginBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.viewModel = _viewModel

        observeLogin()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun observeLogin() {
        lifecycleScope.launchWhenStarted {
            _viewModel.loginFlow.collectLatest {
                when (it) {
                    is Result.Success -> {
                        Log.i(TAG, "observeLogin = Resource.Success")
                        startActivity(
                            Intent(requireContext(), WrapRouletteActivity::class.java).addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            )
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
                                _viewModel.errorPasswordText.value =
                                    ErrorMessage.HelperText(it.exception.message.toString())
                            }
                            is FirebaseAuthInvalidUserException -> {
                                _viewModel.errorEmailText.value =
                                    ErrorMessage.ErrorText(it.exception.message.toString())
                            }
                            else -> {
                                _viewModel.showSnackBar.value = it.exception.message
                            }
                        }
                    }
                }
            }

        }
    }
}