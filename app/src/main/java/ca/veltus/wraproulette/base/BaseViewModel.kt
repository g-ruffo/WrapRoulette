package ca.veltus.wraproulette.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.utils.Constants.DEPARTMENT_ERROR
import ca.veltus.wraproulette.utils.Constants.EMAIL_ERROR
import ca.veltus.wraproulette.utils.Constants.NAME_ERROR
import ca.veltus.wraproulette.utils.Constants.PASSWORD_ERROR
import ca.veltus.wraproulette.utils.Constants.POOL_DATE_ERROR
import ca.veltus.wraproulette.utils.Constants.POOL_NAME_ERROR
import ca.veltus.wraproulette.utils.Constants.POOL_START_ERROR
import ca.veltus.wraproulette.utils.Constants.SHOW_SNACKBAR
import ca.veltus.wraproulette.utils.Constants.SHOW_TOAST
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
    val errorPoolNameText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val errorPoolDateText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val errorPoolStartText: MutableStateFlow<ErrorMessage<String>?> = MutableStateFlow(null)
    val hasNetworkConnection: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun navigateBack() {
        navigationCommand.postValue(NavigationCommand.Back)
    }

    fun clearErrorHelperText(error: Int?) {
        when (error) {
            PASSWORD_ERROR -> errorPasswordText.value = null
            EMAIL_ERROR -> errorEmailText.value = null
            DEPARTMENT_ERROR -> errorDepartmentText.value = null
            NAME_ERROR -> errorNameText.value = null
            POOL_NAME_ERROR -> errorPoolNameText.value = null
            POOL_DATE_ERROR -> errorPoolDateText.value = null
            POOL_START_ERROR -> errorPoolStartText.value = null

            null -> {
                errorPasswordText.value = null
                errorEmailText.value = null
                errorDepartmentText.value = null
                errorNameText.value = null
                errorPoolNameText.value = null
                errorPoolDateText.value = null
                errorPoolStartText.value = null
            }
        }
    }

    fun postSnackbarToastMessage(value: Int, string: String) {
        when (value) {
            SHOW_SNACKBAR -> showSnackBar.postValue(string)
            SHOW_TOAST -> showToast.postValue(string)
        }
    }
}