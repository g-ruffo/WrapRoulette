package ca.veltus.wraproulette.authentication.signup

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
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.data.Resource
import ca.veltus.wraproulette.databinding.FragmentSignupBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SignupFragment : BaseFragment() {
    companion object {
        const val TAG = "SignUpFragmentLog"
    }

    override val _viewModel: LoginSignupViewModel by activityViewModels()

    private lateinit var binding: FragmentSignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

        binding.viewModel = _viewModel

        firebaseAuth = FirebaseAuth.getInstance()

        observeSignup()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.signUpButton.setOnClickListener {
            launchEmailSignUp()
        }
    }

    // If entered email and password is valid, create Firebase user and log result.
    private fun launchEmailSignUp() {
        _viewModel.validateEmailAndPassword(true)

    }

    private fun observeSignup() {
        lifecycleScope.launchWhenStarted {
            _viewModel.signupFlow.collectLatest {
                when (it) {
                    is Resource.Success -> {
                        Log.i(TAG, "observeSignup = Resource.Success")
//                        databaseReference = FirebaseFirestore.getInstance()
//                        databaseReference.collection("Users").document(_viewModel.currentUser!!.uid)
//                            .set(_viewModel.buildHashMap())

                        FirestoreUtil.initCurrentUserIfFirstTime(_viewModel.getDepartmentString()) {
                            startActivity(
                                Intent(requireContext(), WrapRouletteActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            requireActivity().finish()
                        }

                    }
                    is Resource.Loading -> {
                        Log.i(TAG, "observeSignup = Resource.Loading")
                        CircularProgressIndicator(requireContext())
                    }
                    is Resource.Failure -> {
                        // If Firebase cannot create account with provided details show helper text with task exception message.
                        Log.i(TAG, "observeSignup = Resource.Failure")
                        when (it.exception) {
                            is FirebaseAuthWeakPasswordException -> {
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
                            is FirebaseAuthUserCollisionException -> {
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
                            else -> _viewModel.showToast.value = it.exception.message
                        }
                    }
                }
            }
        }
    }

}