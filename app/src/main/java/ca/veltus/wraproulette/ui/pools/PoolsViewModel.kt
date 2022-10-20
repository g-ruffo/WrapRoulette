package ca.veltus.wraproulette.ui.pools

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PoolsViewModel @Inject constructor(
    private val repository: AuthenticationRepository,
    app: Application
) :
    BaseViewModel(app) {

    companion object {
        private const val TAG = "PoolsViewModel"
    }

    val poolProduction = MutableStateFlow<String?>(null)
    val poolPassword = MutableStateFlow<String?>(null)
    val poolDate = MutableStateFlow<String?>(null)

    private val _pools = MutableStateFlow<List<Pool>>(listOf())
    val pools: StateFlow<List<Pool>>
        get() = _pools

    init {
        fetchPoolList()
    }

    fun fetchPoolList() {
        Log.i(TAG, "fetchPoolList: called")
        FirestoreUtil.getCurrentUser { user ->
            viewModelScope.launch {
                _pools.emitAll(FirestoreUtil.getPoolsList(user.uid))
            }
        }
    }


    fun setPoolDate(date: String) {
        poolDate.value = date
    }

    fun createPool() {

    }

}