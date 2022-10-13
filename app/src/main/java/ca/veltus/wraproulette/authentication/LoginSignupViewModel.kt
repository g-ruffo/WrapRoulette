package ca.veltus.wraproulette.authentication

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.authentication.login.LoginFragmentDirections
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.Resource
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSignupViewModel @Inject constructor(private val repository: AuthenticationRepository, app: Application) :
    BaseViewModel(app) {

    companion object {
        const val TAG = "LoginSignupViewModel"
    }

    val username = MutableLiveData<String>()
    val emailAddress = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Resource<FirebaseUser>?>
    get() = _loginFlow

    private val _signupFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signupFlow: StateFlow<Resource<FirebaseUser>?>
        get() = _signupFlow

    val currentUser: FirebaseUser?
    get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading
        val result = repository.signup(name, email, password)
        _signupFlow.value = result
    }

    fun logout() {
        repository.logout()
        _loginFlow.value = null
        _signupFlow.value = null
    }

    // Check to see if entered email is valid and matches correct format. If valid return true.
    fun validateEmail(): Boolean {
        emailAddress.value = emailAddress.value?.trim()
        if (TextUtils.isEmpty(emailAddress.value)) {
            showToast.value = "Please Enter Your Email Address"
            return false
        }
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value)
                .matches()
        ) {
            showToast.value = "Email Address Is Not Valid"
            false
        } else true
    }

    // Check if entered email and password are valid and match correct format. If valid return true.
    fun validateEmailAndPassword(signUp: Boolean = false): Boolean {
        emailAddress.value = emailAddress.value?.trim()
        password.value = password.value?.trim()
        username.value = username.value?.trim()

        if (TextUtils.isEmpty(emailAddress.value)) {
            showToast.value = "Please Enter Your Email Address"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString()).matches()) {
            showToast.value = "Email Address Is Not Valid"
            return false
        }
        return if (TextUtils.isEmpty(password.value)) {
            showToast.value = "Please Enter A Password"
            false
        } else {
            if (signUp) {
                signup(username.value!!, emailAddress.value!!, password.value!!)
            } else {
                login(emailAddress.value!!, password.value!!)
            }
            true
        }
    }

    // Returns email, password and username as a triple.
    fun getEmailPasswordAndName(): Triple<String, String?, String?> {
        return Triple(emailAddress.value!!, password.value, username.value)
    }

    fun navigateBack() {
        navigationCommand.postValue(NavigationCommand.Back)
    }

    fun navigateToForgottenPassword() {
        navigationCommand.postValue(
            NavigationCommand.To(
                LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            )
        )
    }

    fun navigateToSignUp() {
        navigationCommand.postValue(
            NavigationCommand.To(
                LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            )
        )
    }
}