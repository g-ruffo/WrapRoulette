package ca.veltus.wraproulette.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.utils.Constants
import ca.veltus.wraproulette.utils.SingleLiveEvent
import kotlinx.coroutines.flow.MutableStateFlow

// Base class for View Models to declare the common LiveData objects in a single place
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showNoData: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val errorPasswordText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val errorEmailText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val errorDepartmentText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val errorNameText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)

    fun navigateBack() {
        navigationCommand.postValue(NavigationCommand.Back)
    }

    fun clearErrorHelperText(error: Int?) {
        when (error) {
            Constants.PASSWORD_ERROR -> errorPasswordText.value = null
            Constants.EMAIL_ERROR -> errorEmailText.value = null
            Constants.DEPARTMENT_ERROR -> errorDepartmentText.value = null
            Constants.NAME_ERROR -> errorNameText.value = null
            null -> {
                errorPasswordText.value = null
                errorEmailText.value = null
                errorDepartmentText.value = null
                errorNameText.value = null
            }
        }
    }
}