package ca.veltus.wraproulette.authentication.login

import android.content.Intent
import android.os.Bundle
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
import ca.veltus.wraproulette.utils.Constants
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

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        observeLogin()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *  Observe viewModels LoginFlow and if successful navigate to the WrapRouletteActivity.
     *  If it fails, display the issue as a helper text message in the corresponding EditText or display the message in a Snack Bar.
     */
    private fun observeLogin() {
        lifecycleScope.launchWhenStarted {
            _viewModel.loginFlow.collectLatest {
                if (it != null) {
                    when (it) {
                        is Result.Success -> {
                            startActivity(
                                Intent(requireContext(), WrapRouletteActivity::class.java).addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                            requireActivity().finish()
                        }
                        is Result.Loading -> {
//                            Log.i(TAG, "observeSignup: Loading")
                        }
                        is Result.Failure -> {
                            when (it.exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    _viewModel.postErrorHelperText(
                                        Constants.PASSWORD_ERROR,
                                        ErrorMessage.HelperText(it.exception.message.toString())
                                    )
                                }
                                is FirebaseAuthInvalidUserException -> {
                                    _viewModel.postErrorHelperText(
                                        Constants.EMAIL_ERROR,
                                        ErrorMessage.ErrorText(it.exception.message.toString())
                                    )

                                }
                                else -> {
                                    _viewModel.postSnackBarMessage(it.exception.message ?: "")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}