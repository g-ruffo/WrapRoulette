package ca.veltus.wraproulette.ui.pools

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.PoolListRepository
import ca.veltus.wraproulette.ui.pools.createpool.AddPoolFragmentDirections
import ca.veltus.wraproulette.ui.pools.joinpool.JoinPoolFragmentDirections
import ca.veltus.wraproulette.utils.Constants.SET_BET_AMOUNT
import ca.veltus.wraproulette.utils.Constants.SET_FINAL_BETS
import ca.veltus.wraproulette.utils.Constants.SET_MARGIN_TIME
import ca.veltus.wraproulette.utils.StringResourcesProvider
import ca.veltus.wraproulette.utils.network.ConnectivityObserver
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
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
    private val repository: PoolListRepository,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val stringResourcesProvider: StringResourcesProvider,
    app: Application
) : BaseViewModel(app) {

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
    val poolPISRulesEnabled = MutableStateFlow<Boolean>(false)

    val poolDocUid = MutableStateFlow<String?>(null)
    private val poolAdminUid = MutableStateFlow<String?>(null)
    private val poolAdminName = MutableStateFlow<String?>(null)
    private val poolAdminProfileImage = MutableStateFlow<String?>(null)
    private val poolWinners = MutableStateFlow<List<Member>>(listOf())
    private val poolBets = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val poolUsers = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val poolEndTime = MutableStateFlow<Date?>(null)

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
            launch {
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

    private fun fetchPoolList(uid: String) {
        viewModelScope.launch {
            repository.getPoolsList(uid).collect {
                if (it.isNullOrEmpty()) {
                    showNoData.emit(true)
                } else {
                    _pools.emit(it)
                    showNoData.emit(false)
                }
                showLoading.emit(false)
            }
        }
    }

    fun setPoolDate(date: String) {
        poolDate.value = date
    }

    fun setPoolPriceAndMargin(value: String?, isPrice: Boolean = true) {
        if (isPrice) poolBetAmount.value = value
        else poolMargin.value = value
    }

    fun setPoolTime(time: Date?, isStartTime: Boolean = true) {
        if (isStartTime) poolStartTime.value = time
        else poolBetLockTime.value = time
    }

    fun clearPoolTimeAndAmount(value: Int) {
        when (value) {
            SET_FINAL_BETS -> poolBetLockTime.value = null
            SET_BET_AMOUNT -> poolBetAmount.value = null
            SET_MARGIN_TIME -> poolMargin.value = null
        }
    }

    fun joinPool() {
        showLoading.value = true
        val production = poolProduction.value
        val password = poolPassword.value
        val date = poolDate.value

        if (!hasNetworkConnection.value) {
            showSnackBar.postValue(stringResourcesProvider.getString(R.string.noNetworkCantJoinMessage))
            showLoading.value = false
            return
        }

        if (production.isNullOrEmpty()) {
            errorPoolNameText.value = ErrorMessage.ErrorText("Please Enter Production Name")
            showLoading.value = false
            return
        }

        if (date.isNullOrEmpty()) {
            errorPoolDateText.value = ErrorMessage.ErrorText("Please Enter Pool Date")
            showLoading.value = false
            return
        }
        viewModelScope.launch {
            repository.joinPool(
                production.trim(), password?.trim() ?: "", date.trim()
            ) {
                if (it.isNullOrEmpty()) {
                    navigateJoinPoolToHomeFragment()
                } else {
                    showToast.postValue(it)
                    showLoading.value = false
                }
            }
        }
    }

    fun createUpdatePool() {
        showLoading.value = true
        if (!hasNetworkConnection.value) {
            showSnackBar.postValue(stringResourcesProvider.getString(R.string.noNetworkCantUpdateMessage))
            showLoading.value = false
            return
        }
        if (poolProduction.value.isNullOrEmpty()) {
            errorPoolNameText.value = ErrorMessage.ErrorText("Please Enter Production Name")
            showLoading.value = false
            return
        }

        if (poolDate.value.isNullOrEmpty()) {
            errorPoolDateText.value = ErrorMessage.ErrorText("Please Enter Pool Date")
            showLoading.value = false
            return
        }
        if (poolStartTime.value == null) {
            errorPoolStartText.value = ErrorMessage.ErrorText("Please Enter Pool Start Time")
            showLoading.value = false
            return
        }

        var bidPrice = poolBetAmount.value
        if (bidPrice.isNullOrEmpty()) {
            bidPrice = "0"
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
            _userAccount.value?.profilePicturePath ?: poolAdminProfileImage.value,
            poolProduction.value!!.trim(),
            poolPassword.value?.trim() ?: "",
            poolDate.value!!,
            bidPrice,
            poolMargin.value ?: "0",
            betLockTime,
            startTime,
            poolEndTime.value,
            poolWinners.value,
            poolUsers.value,
            poolPISRulesEnabled.value,
            poolBets.value

        )
        if (!poolDocUid.value.isNullOrEmpty()) {
            viewModelScope.launch {
                repository.updatePool(pool) {
                    if (it.isNullOrEmpty()) {
                        navigateBack()
                    } else {
                        showToast.postValue(it)
                    }
                    showLoading.value = false
                }
            }
        } else {
            viewModelScope.launch {
                repository.createPool(pool) {
                    if (it.isNullOrEmpty()) {
                        navigateAddPoolToHomeFragment()
                    } else {
                        showToast.postValue(it)
                        showLoading.value = false
                    }
                }
            }
        }
    }

    fun loadEditPool(poolId: String) {
        showLoading.value = true
        viewModelScope.launch {
            repository.getEditPool(poolId) { pool ->
                if (pool != null) {
                    poolDocUid.value = poolId
                    poolProduction.value = pool.production
                    poolPassword.value = pool.password
                    poolDate.value = pool.date
                    poolBetAmount.value = pool.betAmount
                    poolMargin.value = pool.margin
                    poolBetLockTime.value = pool.lockTime
                    poolStartTime.value = pool.startTime
                    poolAdminName.value = pool.adminName
                    poolAdminUid.value = pool.adminUid
                    poolAdminProfileImage.value = pool.adminProfileImage
                    poolWinners.value = pool.winners
                    poolUsers.value = pool.users
                    poolEndTime.value = pool.endTime
                    poolPISRulesEnabled.value = pool.pIRRulesEnabled
                    poolBets.value = pool.bets
                }
                showLoading.value = false
            }
        }
    }

    fun setUsersActivePool(poolId: String) {
        viewModelScope.launch {
            showLoading.emit(true)
            repository.setActivePool(poolId) {
                if (!it.isNullOrEmpty()) showToast.postValue(it)
                else navigatePoolsToHomeFragment()
                showLoading.value = false
            }
            if (!hasNetworkConnection.value) {
                showSnackBar.postValue("No network connection, data will update when reconnected.")
                showLoading.value = false
                navigatePoolsToHomeFragment()
            }
        }
    }

    fun deletePool() {
        viewModelScope.launch {
            repository.deletePool(poolDocUid.value!!) {
                navigateAddPoolToHomeFragment()
            }
        }
    }

    fun toggleFabButton() {
        isFabClicked.value = !isFabClicked.value
    }

    fun navigateToAddPoolFragment() {
        navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToAddPoolFragment()))
    }

    private fun navigatePoolsToHomeFragment() {
        navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToNavHome()))
    }

    fun navigateToJoinPoolFragment() {
        navigationCommand.postValue(NavigationCommand.To(PoolsFragmentDirections.actionNavPoolsToJoinPoolFragment()))
    }

    private fun navigateJoinPoolToHomeFragment() {
        navigationCommand.postValue(NavigationCommand.To(JoinPoolFragmentDirections.actionJoinPoolFragmentToNavHome()))
        showLoading.value = false
    }

    private fun navigateAddPoolToHomeFragment() {
        navigationCommand.postValue(NavigationCommand.To(AddPoolFragmentDirections.actionAddPoolFragmentToNavHome()))
        showLoading.value = false
    }
}