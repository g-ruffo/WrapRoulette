package ca.veltus.wraproulette.ui.pools

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

    val poolDocUid = MutableStateFlow<String?>(null)
    val poolAdminUid = MutableStateFlow<String?>(null)
    val poolAdminName = MutableStateFlow<String?>(null)
    val poolWinner = MutableStateFlow<Member?>(null)
    val poolUsers = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    val poolEndTime = MutableStateFlow<Date?>(null)


    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount

    private val _pools = MutableStateFlow<List<Pool>>(listOf())
    val pools: StateFlow<List<Pool>>
        get() = _pools

    val isFabClicked = MutableStateFlow<Boolean>(false)

    init {
        showLoading.value = true
        viewModelScope.launch {
            repository.getCurrentUserProfile().collectLatest {
                _userAccount.emit(it)
                Log.i(TAG, "_userAccount: $it")
                if (it != null && !it.uid.isNullOrEmpty()) {
                    fetchPoolList(it.uid)
                } else {
                    showLoading.emit(false)
                }
            }
        }
    }

    private fun fetchPoolList(uid: String) {
        viewModelScope.launch {
            FirestoreUtil.getPoolsList(uid).collect {
                if (it.isNullOrEmpty()) {
                    showNoData.emit(true)
                }
                Log.i(TAG, "fetchPoolList: $it")
                _pools.emit(it)
                showLoading.emit(false)
            }
        }
    }


    fun setPoolDate(date: String) {
        poolDate.value = date
    }

    fun setPoolStartTime(time: Date) {
        poolStartTime.value = time
    }

    fun setPoolBetLockTime(time: Date?) {
        poolBetLockTime.value = time
        Log.i(TAG, "setPoolBetLockTime: ${poolBetLockTime.value}")
    }

    fun createUpdatePool() {
        if (poolProduction.value.isNullOrEmpty()) {
            showToast.value = "Please Enter Production Name"
            return
        }
        if (poolPassword.value.isNullOrEmpty()) {
            showToast.value = "Please Enter Pool Password"
            return
        }
        if (poolDate.value.isNullOrEmpty()) {
            showToast.value = "Please Enter Pool Date"
            return
        }
        if (poolStartTime.value == null) {
            showToast.value = "Please Enter Pool Start Time"
            return
        }

        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateObject = parsedDate.parse(poolDate.value)

        val startTime = poolStartTime.value
        startTime!!.year = dateObject.year
        startTime.month = dateObject.month
        startTime.date = dateObject.date

        val betLockTime = poolBetLockTime.value
        if (betLockTime != null) {
            betLockTime.year = dateObject.year
            betLockTime.month = dateObject.month
            betLockTime.date = dateObject.date
            if (betLockTime.before(startTime)) {
                betLockTime.date += 1
            }
        }

        val pool = Pool(
            poolDocUid.value ?: "",
            poolAdminUid.value ?: "",
            poolAdminName.value ?: "",
            poolProduction.value!!.trim(),
            poolPassword.value!!.trim(),
            poolDate.value!!,
            poolBetAmount.value ?: "0",
            poolMargin.value ?: "0",
            betLockTime,
            startTime,
            poolEndTime.value,
            poolWinner.value,
            poolUsers.value
        )
        if (!poolDocUid.value.isNullOrEmpty()) {
            FirestoreUtil.updatePool(pool) {
                navigateBack()
            }
        } else {
            FirestoreUtil.createPool(pool) {
                navigateBack()
            }
        }
    }

    fun loadEditPool(poolId: String) {
        showLoading.value = true
        FirestoreUtil.getEditPool(poolId) { pool ->
            viewModelScope.launch {
                if (pool != null) {
                    poolDocUid.emit(poolId)
                    poolProduction.emit(pool.production)
                    poolPassword.emit(pool.password)
                    poolDate.emit(pool.date)
                    poolBetAmount.emit(pool.betAmount)
                    poolMargin.emit(pool.margin)
                    poolBetLockTime.emit(pool.lockTime)
                    poolStartTime.emit(pool.startTime)
                    poolAdminName.emit(pool.adminName)
                    poolAdminUid.emit(pool.adminUid)
                    poolWinner.emit(pool.winner)
                    poolUsers.emit(pool.users)
                    poolEndTime.emit(pool.endTime)
                }
                showLoading.emit(false)
            }
        }
    }

    fun toggleFabButton() {
        isFabClicked.value = !isFabClicked.value
    }

    fun navigateToAddPoolFragment() {
        navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToAddPoolFragment()))
    }

    fun navigateToJoinPoolFragment() {
        navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToJoinPoolFragment()))
    }
}