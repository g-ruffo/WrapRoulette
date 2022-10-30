package ca.veltus.wraproulette.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    fun navigateBack() {
        navigationCommand.postValue(NavigationCommand.Back)
    }
}