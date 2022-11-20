package ca.veltus.wraproulette.authentication

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.authentication.login.LoginFragmentDirections
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSignupViewModel @Inject constructor(
    private val repository: AuthenticationRepository, app: Application
) : BaseViewModel(app) {

    companion object {
        const val TAG = "LoginSignupViewModel"
    }

    val username = MutableLiveData<String>()
    val emailAddress = MutableLiveData<String>()
    val department = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginFlow = MutableStateFlow<Result<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Result<FirebaseUser>?>
        get() = _loginFlow

    private val _signupFlow = MutableStateFlow<Result<FirebaseUser>?>(null)
    val signupFlow: StateFlow<Result<FirebaseUser>?>
        get() = _signupFlow

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Result.Success(repository.currentUser!!)
            viewModelScope.launch {
                repository.getCurrentUserProfile().collectLatest {
                    _userAccount.emit(it)
                }
            }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Result.Loading
        val result = repository.login(email, password)
        _loginFlow.value = result
        showLoading.value = false
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Result.Loading
        val result = repository.signup(name, email, password)
        _signupFlow.value = result
        showLoading.value = false
    }

    fun logout() {
        repository.logout()
        _loginFlow.value = null
        _signupFlow.value = null
    }

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.initCurrentUserIfFirstTime(department.value ?: "") {
                if (it == null) onComplete()
                else showToast.postValue(it)
            }
        }
    }

    // Check to see if entered email is valid and matches correct format. If valid return true.
    fun resetPassword() {
        showLoading.value = true
        emailAddress.value = emailAddress.value?.trim()
        if (TextUtils.isEmpty(emailAddress.value)) {
            errorPasswordText.value = ErrorMessage.ErrorText("Please enter your email address")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString())
                .matches()
        ) {
            errorPasswordText.value = ErrorMessage.ErrorText("Email address is not valid")
        } else {
            viewModelScope.launch {
                repository.resetPassword(emailAddress.value!!) {
                    showLoading.value = false
                    if (it.isNullOrEmpty()) {
                        showToast.postValue("Reset link has been sent to your email address.")
                        navigateBack()
                    } else errorEmailText.value = ErrorMessage.ErrorText(it)
                }
            }
        }
    }

    // Check if entered email and password are valid and match correct format. If valid return true.
    fun validateEmailAndPassword(signUp: Boolean = false): Boolean {
        showLoading.value = true
        emailAddress.value = emailAddress.value?.trim()
        password.value = password.value?.trim()
        username.value = username.value?.trim()

        if (TextUtils.isEmpty(emailAddress.value)) {
            errorEmailText.value = ErrorMessage.ErrorText("Please enter your email address")
            showLoading.value = false
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString()).matches()) {
            errorEmailText.value = ErrorMessage.ErrorText("Email address is not a valid format")
            showLoading.value = false
            return false
        }
        if (TextUtils.isEmpty(password.value)) {
            errorPasswordText.value = ErrorMessage.ErrorText("Please enter a password")
            showLoading.value = false
            return false
        }
        if (TextUtils.isEmpty(username.value)) {
            errorNameText.value = ErrorMessage.ErrorText("Please enter your name")
            showLoading.value = false
            return false
        }

        if (TextUtils.isEmpty(department.value)) {
            errorDepartmentText.value = ErrorMessage.ErrorText("Please enter your department")
            showLoading.value = false
            return false
        } else {
            if (signUp) {
                signup(username.value!!, emailAddress.value!!, password.value!!)
            } else {
                login(emailAddress.value!!, password.value!!)
            }
            return true
        }
    }

    fun updateCurrentUser(editName: String?, editDepartment: String?, profilePicturePath: String?) {
        if (editName.isNullOrEmpty()) showToast.value = "Please Enter Your Name"
        else if (editDepartment.isNullOrEmpty()) showToast.value = "Please Enter Your Department"
        else {
            val userFieldMap = mutableMapOf<String, Any>()
            if (!editName.isNullOrEmpty()) userFieldMap["displayName"] = editName.trim()
            if (!editDepartment.isNullOrEmpty()) userFieldMap["department"] = editDepartment.trim()
            if (profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath
            viewModelScope.launch {
                repository.updateCurrentUser(userFieldMap) {
                    if (it.isNullOrEmpty()) {
                        showToast.postValue("Saved Successfully")
                        navigateBack()
                    } else showToast.postValue(it)
                }
            }
        }

    }

    fun navigateToForgottenPassword() {
        navigationCommand.postValue(
            NavigationCommand.To(
                LoginFragmentDirections.actionLoginFragmentToResetPasswordFragment()
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