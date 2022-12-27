package ca.veltus.wraproulette.authentication

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.login.LoginFragmentDirections
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.Feedback
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import ca.veltus.wraproulette.utils.StringResourcesProvider
import ca.veltus.wraproulette.utils.network.ConnectivityObserver
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LoginSignupViewModel @Inject constructor(
    private val repository: AuthenticationRepository,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val stringResourcesProvider: StringResourcesProvider,
    app: Application
) : BaseViewModel(app) {

    val username = MutableLiveData<String>()
    val emailAddress = MutableLiveData<String>()
    val department = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val updateUsername = MutableStateFlow<String?>(null)
    val updateDepartment = MutableStateFlow<String?>(null)

    val feedbackMessage = MutableStateFlow<String?>(null)

    private val _tempProfileImage = MutableStateFlow<ByteArray?>(null)
    val tempProfileImage: StateFlow<ByteArray?>
        get() = _tempProfileImage.asStateFlow()

    private val _loginFlow = MutableStateFlow<Result<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Result<FirebaseUser>?>
        get() = _loginFlow.asStateFlow()

    private val _signupFlow = MutableStateFlow<Result<FirebaseUser>?>(null)
    val signupFlow: StateFlow<Result<FirebaseUser>?>
        get() = _signupFlow.asStateFlow()

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount.asStateFlow()

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    init {
        // If the user is logged in, set the loginFlow flow value and emit the profiles values.
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
                    // The network connectivity state is used to prevent the user from making certain Firebase changes if not connected.
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
        setShowLoadingValue(false)
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Result.Loading
        val result = repository.signup(name, email, password)
        _signupFlow.value = result
        setShowLoadingValue(false)
    }

    fun logout() {
        repository.logout()
        _loginFlow.value = null
        _signupFlow.value = null
    }

    /**
     * When the user initially signs up, create a corresponding Firestore user document using the values provided
     * and update the users Firebase Authentication values.
     */
    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.initCurrentUserIfFirstTime(department.value ?: "") {
                if (it == null) onComplete()
                else showToast.postValue(it)
            }
        }
    }

    /**
     *  Check to see if the entered email is valid and matches correct format. If valid return true and request
     *  a reset password to be sent to the provided email and navigate back. If there is an issue locating a
     *  corresponding account, notify the user with a helper text error.
     */
    fun resetPassword() {
        setShowLoadingValue(true)
        emailAddress.value = emailAddress.value?.trim()
        if (TextUtils.isEmpty(emailAddress.value)) {
            errorEmailText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterEmailErrorMessage))
            setShowLoadingValue(false)
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString())
                .matches()
        ) {
            errorEmailText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.emailNotValidErrorMessage))
            setShowLoadingValue(false)
        } else {
            viewModelScope.launch {
                repository.resetPassword(emailAddress.value!!) {
                    setShowLoadingValue(false)
                    if (it.isNullOrEmpty()) {
                        showToast.postValue(stringResourcesProvider.getString(R.string.resetEmailSentMessage))
                        navigateBack()
                    } else errorEmailText.value = ErrorMessage.ErrorText(it)
                }
            }
        }
    }

    /**
     *  Check if the entered email, password, department and name is valid. If invalid return false and display
     *  the issue as a helper text error in the appropriate EditText.
     */
    fun validateEmailAndPassword(signUp: Boolean = false): Boolean {
        setShowLoadingValue(true)
        emailAddress.value = emailAddress.value?.trim()
        password.value = password.value?.trim()
        username.value = username.value?.trim()

        if (signUp) department.value = department.value?.trim()

        if (TextUtils.isEmpty(emailAddress.value)) {
            errorEmailText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterEmailErrorMessage))
            setShowLoadingValue(false)
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.value.toString())
                .matches()
        ) {
            errorEmailText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.emailNotValidErrorMessage))
            setShowLoadingValue(false)
            return false
        } else if (TextUtils.isEmpty(password.value)) {
            errorPasswordText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterPasswordErrorMessage))
            setShowLoadingValue(false)
            return false
        } else if (signUp && TextUtils.isEmpty(username.value)) {
            errorNameText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterNameErrorMessage))
            setShowLoadingValue(false)
            return false
        } else if (signUp && TextUtils.isEmpty(department.value)) {
            errorDepartmentText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterDepartmentErrorMessage))
            setShowLoadingValue(false)
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

    /**
     * Ensure the provided values are valid and update the profile information in Firebase. If image is not null,
     * upload the bitmap to Firebase Storage before updating account details.
     */
    fun validateUpdateCurrentUser() {
        setShowLoadingValue(true)
        val username = updateUsername.value
        val department = updateDepartment.value
        val tempImage = tempProfileImage.value
        if (username.isNullOrEmpty()) {
            errorNameText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterNameErrorMessage))
            setShowLoadingValue(false)
            return
        } else if (department.isNullOrEmpty()) {
            errorDepartmentText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterDepartmentErrorMessage))
            setShowLoadingValue(false)
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

    /**
     * Trim provided string values before uploading to Firestore. If the user is not connected to a network
     * display a toast message notifying them about the pending update.
     */
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
                    showToast.postValue(stringResourcesProvider.getString(R.string.savedSuccessfullyMessage))
                    navigateBack()
                } else {
                    showSnackBar.postValue(it)
                }
                setShowLoadingValue(false)
            }
            if (!hasNetworkConnection) {
                setShowLoadingValue(false)
                showSnackBar.postValue(stringResourcesProvider.getString(R.string.noNetworkUpdateDelayMessage))
                navigateBack()
            }
        }
    }

    /**
     * If the message is not null, empty or blank, create a Feedback object with the date variable
     * set to the current timestamp and upload to Firestore. After completing successfully, notify user with
     * a toast message.
     */

    fun sendFeedbackMessage(): Boolean {
        val message = feedbackMessage.value
        if (message.isNullOrEmpty() || message.isBlank()) {
            return false
        } else {
            val feedback = Feedback(
                message,
                Calendar.getInstance().time,
                currentUser?.uid ?: "",
                currentUser?.displayName ?: "",
                currentUser?.email ?: ""
            )
            viewModelScope.launch {
                repository.sendFeedback(feedback) {
                    if (!it.isNullOrEmpty()) showToast.postValue(it)
                    else feedbackMessage.value = null
                }
            }
            return true
        }
    }

    /**
     * Used in the AccountFragment to set the new profile image selected by the user. The ByteArray is stored
     * as a value until the save button is clicked at which point it is uploaded to Firebase and then set to null.
     */
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