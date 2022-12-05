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
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import ca.veltus.wraproulette.utils.network.ConnectivityObserver
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSignupViewModel @Inject constructor(
    private val repository: AuthenticationRepository,
    private val connectivityObserver: NetworkConnectivityObserver,
    app: Application
) : BaseViewModel(app) {

    companion object {
        const val TAG = "LoginSignupViewModel"
    }

    val username = MutableLiveData<String>()
    val emailAddress = MutableLiveData<String>()
    val department = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val updateUsername = MutableStateFlow<String?>(null)
    val updateDepartment = MutableStateFlow<String?>(null)

    private val _tempProfileImage = MutableStateFlow<ByteArray?>(null)
    val tempProfileImage: StateFlow<ByteArray?>
        get() = _tempProfileImage

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
                launch {
                    repository.getCurrentUserProfile().collectLatest {
                        _userAccount.emit(it)
                        updateUsername.emit(it?.displayName)
                        updateDepartment.emit(it?.department)
                    }
                }
                launch {
                    connectivityObserver.observe().collectLatest {
                        if (it == ConnectivityObserver.Status.Available) hasNetworkConnection.emit(
                            true
                        )
                        else hasNetworkConnection.emit(false)
                    }
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
            errorEmailText.value = ErrorMessage.ErrorText("Please enter your email address")
            showLoading.value = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString())
                .matches()
        ) {
            errorEmailText.value = ErrorMessage.ErrorText("Email address is not valid")
            showLoading.value = false
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
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString())
                .matches()
        ) {
            errorEmailText.value = ErrorMessage.ErrorText("Email address is not a valid format")
            showLoading.value = false
            return false
        } else if (TextUtils.isEmpty(password.value)) {
            errorPasswordText.value = ErrorMessage.ErrorText("Please enter a password")
            showLoading.value = false
            return false
        } else if (signUp && TextUtils.isEmpty(username.value)) {
            errorNameText.value = ErrorMessage.ErrorText("Please enter your name")
            showLoading.value = false
            return false
        } else if (signUp && TextUtils.isEmpty(department.value)) {
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

    fun validateUpdateCurrentUser() {
        showLoading.value = true
        val username = updateUsername.value
        val department = updateDepartment.value
        val tempImage = tempProfileImage.value
        if (username.isNullOrEmpty()) {
            errorNameText.value = ErrorMessage.ErrorText("Please enter your name")
            showLoading.value = false
            return
        } else if (department.isNullOrEmpty()) {
            errorDepartmentText.value = ErrorMessage.ErrorText("Please enter your department")
            showLoading.value = false
            return
        } else {
            viewModelScope.launch {
                if (tempImage != null && hasNetworkConnection.value) {
                    FirebaseStorageUtil.uploadProfilePhoto(tempImage) { imagePath ->
                        updateUserProfile(
                            username, department, imagePath, hasNetworkConnection.value
                        )
                    }
                } else {
                    updateUserProfile(username, department, null, hasNetworkConnection.value)
                }
            }
        }
    }

    private fun updateUserProfile(
        username: String,
        department: String,
        tempImage: String? = null,
        hasNetworkConnection: Boolean
    ) {
        viewModelScope.launch {
            val userFieldMap = mutableMapOf<String, Any>()
            if (username.isNotEmpty()) userFieldMap["displayName"] = username.trim()
            if (department.isNotEmpty()) userFieldMap["department"] = department.trim()
            if (tempImage != null) userFieldMap["profilePicturePath"] = tempImage

            repository.updateCurrentUser(userFieldMap) {
                if (it.isNullOrEmpty()) {
                    showToast.postValue("Saved Successfully")
                    navigateBack()
                } else {
                    showSnackBar.postValue(it)
                }
                showLoading.value = false
            }
            if (!hasNetworkConnection) {
                showLoading.value = false
                showSnackBar.postValue("Unable to connect to network, your changes will apply when reconnected.")
                navigateBack()
            }
        }
    }

    fun setTemporaryProfileImage(image: ByteArray) {
        _tempProfileImage.value = image
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