package ca.veltus.wraproulette.ui.pools

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    val poolBetAmount = MutableStateFlow<String?>(null)
    val poolMargin = MutableStateFlow<String?>(null)
    val poolBetLockTime = MutableStateFlow<Date?>(null)
    val poolStartTime = MutableStateFlow<Date?>(null)

    val currentUserProfile = MutableStateFlow<User?>(null)

    private val _pools = MutableStateFlow<List<Pool>>(listOf())
    val pools: StateFlow<List<Pool>>
        get() = _pools

    init {
        fetchPoolList()
    }

    fun fetchPoolList() {
        Log.i(TAG, "fetchPoolList: called")
        viewModelScope.launch {
            _pools.emitAll(FirestoreUtil.getPoolsList(repository.currentUser!!.uid))
        }
    }


    fun setPoolDate(date: String) {
        poolDate.value = date
    }

    fun setPoolStartTime(time: Date) {
        poolStartTime.value = time
    }

    fun setPoolBetLockTime(time: Date) {
        poolBetLockTime.value = time
    }

    fun createPool() {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateObject = parsedDate.parse(poolDate.value)
        val startTime = poolStartTime.value
        val betLockTime = poolBetLockTime.value
        betLockTime!!.year = dateObject.year
        betLockTime.month = dateObject.month
        betLockTime.date = dateObject.date
        startTime!!.year = dateObject.year
        startTime.month = dateObject.month
        startTime.date = dateObject.date

        if (betLockTime.before(startTime)) {
            betLockTime.date += 1
        }
        Log.i(TAG, "createPool: $startTime")
        Log.i(TAG, "createPool: $betLockTime")

        val pool = Pool(
            "",
            "",
            "",
            poolProduction.value!!.trim(),
            poolPassword.value!!.trim(),
            poolDate.value!!,
            poolBetAmount.value!!,
            poolMargin.value!!,
            poolBetLockTime.value!!,
            poolStartTime.value!!,
            null,
            mutableMapOf()
        )
        FirestoreUtil.createPool(pool) {
            navigateBack()
        }
    }
}