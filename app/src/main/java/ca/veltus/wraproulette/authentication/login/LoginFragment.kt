package ca.veltus.wraproulette.authentication.login

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import ca.veltus.wraproulette.BuildConfig
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.databinding.FragmentLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginFragment"
    }

    override val _viewModel: LoginSignupViewModel by activityViewModels()

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.viewModel = _viewModel

        firebaseAuth = FirebaseAuth.getInstance()

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
        Log.i(TAG, "launchEmailSignIn Clicked")
        if (_viewModel.validateEmailAndPassword()) {
            firebaseAuth.signInWithEmailAndPassword(
                _viewModel.getEmailPasswordAndName().first,
                _viewModel.getEmailPasswordAndName().second!!
            ).addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        firebaseUser = task.result!!.user!!
                        Log.i(TAG, "Login was Successful: $firebaseUser")
                    } else {
                        _viewModel.showSnackBar.value = "Login Failed"
                        Log.e(TAG, "Login not successful: ${task.exception}")
                    }
                }
            )
        }
    }
}