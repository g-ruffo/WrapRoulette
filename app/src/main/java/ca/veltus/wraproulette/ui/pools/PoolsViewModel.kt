package ca.veltus.wraproulette.ui.pools

import android.app.Application
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PoolsViewModel @Inject constructor(private val repository: AuthenticationRepository, app: Application) :
    BaseViewModel(app) {

    val poolProduction = MutableStateFlow<String?>(null)
    val poolPassword = MutableStateFlow<String?>(null)
    val poolDate = MutableStateFlow<String?>(null)


    fun setPoolDate(date: String) {
        poolDate.value = date
    }


}