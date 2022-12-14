package ca.veltus.wraproulette.authentication.signup

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
import ca.veltus.wraproulette.databinding.FragmentSignupBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.utils.Constants
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SignupFragment : BaseFragment() {
    companion object {
        const val TAG = "SignUpFragmentLog"
    }

    override val _viewModel: LoginSignupViewModel by activityViewModels()

    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

        observeSignup()

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
     *  Observe viewModels SignupFlow and if successful navigate to the WrapRouletteActivity.
     *  If it fails, display the issue as a helper text message in the corresponding EditText or display the message in a Snack Bar.
     */
    private fun observeSignup() {
        lifecycleScope.launchWhenStarted {
            _viewModel.signupFlow.collectLatest {
                if (it != null) {
                    when (it) {
                        is Result.Success -> {
                            _viewModel.initCurrentUserIfFirstTime {
                                startActivity(
                                    Intent(
                                        requireContext(), WrapRouletteActivity::class.java
                                    ).addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    )
                                )
                                requireActivity().finish()
                            }
                        }

                        is Result.Loading -> {
                            Log.i(TAG, "observeSignup: Loading")
                        }
                        is Result.Failure -> {
                            // If Firebase cannot create account with provided details show helper text with task exception message.
                            when (it.exception) {
                                is FirebaseAuthWeakPasswordException -> {
                                    _viewModel.postErrorHelperText(
                                        Constants.PASSWORD_ERROR,
                                        ErrorMessage.ErrorText(it.exception.message.toString())
                                    )
                                }
                                is FirebaseAuthUserCollisionException -> {
                                    _viewModel.postErrorHelperText(
                                        Constants.EMAIL_ERROR,
                                        ErrorMessage.ErrorText(it.exception.message.toString())
                                    )
                                }
                                else -> _viewModel.postSnackBarMessage(it.exception.message ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}