package ca.veltus.wraproulette.ui.home

import android.app.Application
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.objects.*
import ca.veltus.wraproulette.data.repository.HomeRepository
import ca.veltus.wraproulette.utils.Constants
import ca.veltus.wraproulette.utils.StringResourcesProvider
import ca.veltus.wraproulette.utils.calculateWinners
import ca.veltus.wraproulette.utils.isEqual
import ca.veltus.wraproulette.utils.network.ConnectivityObserver
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val stringResourcesProvider: StringResourcesProvider,
    app: Application
) : BaseViewModel(app) {

    private val _isPoolAdmin = MutableStateFlow(false)
    val isPoolAdmin: StateFlow<Boolean>
        get() = _isPoolAdmin.asStateFlow()

    private val _poolStartTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolStartTime: StateFlow<Date>
        get() = _poolStartTime.asStateFlow()

    private val _poolEndTime = MutableStateFlow<Date?>(null)
    val poolEndTime: StateFlow<Date?>
        get() = _poolEndTime.asStateFlow()

    private val _userBetTime = MutableStateFlow<Date?>(null)
    val userBetTime: StateFlow<Date?>
        get() = _userBetTime.asStateFlow()

    private val _isBettingOpen = MutableStateFlow(false)
    val isBettingOpen: StateFlow<Boolean>
        get() = _isBettingOpen.asStateFlow()

    val isFabClicked = MutableStateFlow(false)
    val isScrolling = MutableStateFlow(false)
    val userMessageEditText = MutableStateFlow<String?>(null)
    private val poolBettingLockTime = MutableStateFlow<Date?>(null)

    val poolMargin = MutableStateFlow<String?>(null)
    val poolPIREnabled = MutableStateFlow(false)

    val newMemberName = MutableStateFlow<String?>(null)
    val newMemberDepartment = MutableStateFlow<String?>(null)
    val newMemberEmail = MutableStateFlow<String?>(null)

    private val _poolWinningMembers = MutableStateFlow<List<Member>>(listOf())
    val poolWinningMembers: StateFlow<List<Member>>
        get() = _poolWinningMembers.asStateFlow()

    private val _poolAdminProfileImage = MutableStateFlow<String?>(null)
    val poolAdminProfileImage: StateFlow<String?>
        get() = _poolAdminProfileImage.asStateFlow()

    private val _isPoolActive = MutableStateFlow(false)
    val isPoolActive: StateFlow<Boolean>
        get() = _isPoolActive.asStateFlow()

    private val _actionbarTitle =
        MutableStateFlow(stringResourcesProvider.getString(R.string.menu_home))
    val actionbarTitle: StateFlow<String>
        get() = _actionbarTitle.asStateFlow()

    private val _currentPool = MutableStateFlow<Pool?>(null)
    val currentPool: StateFlow<Pool?>
        get() = _currentPool.asStateFlow()

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount.asStateFlow()

    private val _poolBetsList = MutableStateFlow<List<Member>>(listOf())
    val poolBetsList: StateFlow<List<Member>>
        get() = _poolBetsList.asStateFlow()

    private val _chatList = MutableStateFlow<List<Message>>(listOf())
    val chatList: StateFlow<List<Message>>
        get() = _chatList.asStateFlow()

    // A list of chat messages that the user has viewed.
    private val _readChatListItems =
        MutableStateFlow<Pair<List<Message>, Boolean>>(Pair(listOf(), false))
    val readChatListItems: StateFlow<Pair<List<Message>, Boolean>>
        get() = _readChatListItems.asStateFlow()

    private val _memberList = MutableStateFlow<List<Member>>(listOf())
    val memberList: StateFlow<List<Member>>
        get() = _memberList.asStateFlow()

    val timeWorkedDate = liveData {
        while (true) {
            val time = Calendar.getInstance().time.time - _poolStartTime.value.time
            if (_poolEndTime.value != null) {
                emit(_poolEndTime.value!!.time - _poolStartTime.value.time)
                delay(1000)
            } else if (showNoData.value || time > Constants.DAY || time < 0) {
                emit(0)
                delay(1000)
            } else {
                emit(time)
                delay(1000)
            }
        }
    }

    val betTimeRemainingDate = liveData {
        while (true) {
            if (poolBettingLockTime.value == null) {
                emit(null)
                delay(1000)
                if (isPoolActive.value) {
                    _isBettingOpen.emit(true)
                } else {
                    _isBettingOpen.emit(false)
                }
            } else {
                val time = poolBettingLockTime.value!!.time - Calendar.getInstance().time.time
                if (time > 0 && currentPool.value!!.endTime == null) {
                    emit(time)
                    _isBettingOpen.emit(true)
                    delay(1000)
                } else if (time > 0 && currentPool.value!!.endTime != null) {
                    _isBettingOpen.emit(false)
                    emit(abs(poolBettingLockTime.value!!.time - currentPool.value!!.endTime!!.time))
                    delay(1000)
                } else if (showNoData.value) {
                    emit(0)
                    delay(1000)
                } else {
                    _isBettingOpen.emit(false)
                    emit(0)
                    delay(1000)
                }
            }
        }
    }

    val currentTimeDate = liveData<Date> {
        while (true) {
            emit(Calendar.getInstance().time)
            if (this.latestValue != null) {
                val sortedList =
                    _poolBetsList.value.sortedBy { member -> abs(this.latestValue!!.time - member.bidTime!!.time) }
                if (!isEqual(sortedList, poolBetsList.value)) {
                    _poolBetsList.emit(sortedList)
                }
            }
            delay(1000)
        }
    }

    init {
        setShowLoadingValue(true)
        viewModelScope.launch {
            launch {
                repository.getCurrentUserProfile().collectLatest {
                    _userAccount.value = it
                    if (it != null && !it.activePool.isNullOrEmpty()) {
                        getPoolData(it.activePool)
                    } else {
                        setShowLoadingValue(false)
                        setNoDataValue(true)
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

    private fun getPoolData(activePool: String?) {
        if (!activePool.isNullOrEmpty()) {
            viewModelScope.launch {
                launch {
                    repository.getPoolData(activePool).collectLatest { pool ->
                        if (pool != null) {
                            _currentPool.emit(pool)
                            _poolAdminProfileImage.emit(pool.adminProfileImage)
                            _poolStartTime.emit(pool.startTime!!)
                            poolBettingLockTime.emit(pool.lockTime)
                            _poolEndTime.emit(pool.endTime)
                            _poolWinningMembers.emit(pool.winners)
                            poolMargin.emit(pool.margin)
                            poolPIREnabled.emit(pool.pIRRulesEnabled)
                            setNoDataValue(false)
                            if (pool.adminUid == _userAccount.value!!.uid) {
                                _isPoolAdmin.emit(true)
                                _actionbarTitle.emit(pool.production)
                            } else {
                                _isPoolAdmin.emit(false)
                                _actionbarTitle.emit(pool.production)
                            }
                            if (pool.endTime != null || (Calendar.getInstance().time.time - _poolStartTime.value.time) > Constants.DAY) {
                                _isPoolActive.emit(false)
                                _isBettingOpen.value = false
                            } else {
                                _isPoolActive.emit(true)
                            }
                        } else {
                            setNoDataValue(true)
                            _actionbarTitle.emit(stringResourcesProvider.getString(R.string.menu_home))
                        }
                        setShowLoadingValue(false)
                    }
                }

                launch {
                    repository.getChatList(activePool).collectLatest {
                        _chatList.emit(it)
                        markMessagesAsRead(true)
                    }
                }
                launch {
                    repository.getPoolMemberList(activePool).collectLatest { members ->
                        val list = mutableListOf<Member>()
                        _memberList.emit(members)
                        members.forEach {
                            if (it.uid == userAccount.value!!.uid && it.tempMemberUid == null) setUserBetTime(
                                it.bidTime
                            )
                            if (it.bidTime != null) list.add(it)
                        }
                        _poolBetsList.emit(list.sortedBy { member ->
                            abs(
                                (currentTimeDate.value?.time
                                    ?: Calendar.getInstance().time.time) - member.bidTime!!.time
                            )
                        })
                    }
                }
            }
            setShowLoadingValue(false)
        } else return
    }

    private fun setUserBetTime(date: Date?) {
        _userBetTime.value = date
    }

    fun sendChatMessage() {
        viewModelScope.launch {
            val message = Message(
                userMessageEditText.value!!,
                Date(Calendar.getInstance().time.time),
                userAccount.value!!.uid,
                userAccount.value!!.displayName,
                userAccount.value?.profilePicturePath,
                ""
            )
            userMessageEditText.emit(null)
            repository.sendChatMessage(userAccount.value!!.activePool!!, message) {
                if (!it.isNullOrEmpty()) showToast.postValue(it)
            }
        }
    }

    /**
     * Require the user to be connected to a network before leaving the pool.
     * If not, display a snack bar message.
     */
    fun leavePool() {
        if (!hasNetworkConnection.value) {
            showSnackBar.postValue(stringResourcesProvider.getString(R.string.noNetworkCantUpdateMessage))
            setShowLoadingValue(false)
            return
        } else {
            viewModelScope.launch {
                repository.leavePool(currentPool.value?.docId ?: "", userAccount.value?.uid ?: "") {
                    setShowLoadingValue(false)
                    if (!it.isNullOrEmpty()) showToast.postValue(it)
                    else {
                        _actionbarTitle.value =
                            stringResourcesProvider.getString(R.string.menu_home)
                        navigateToPoolFragment()
                    }
                }
            }
        }
    }

    /**
     * Sets the edit text values when admin is updating the temporary members information.
     */
    fun loadTempMemberValues(member: Member?) {
        newMemberName.value = member?.displayName
        newMemberDepartment.value = member?.department
        newMemberEmail.value = member?.email
    }

    /**
     * Called when the pool admin is creating or submitting changes to a temporary pool member.
     * If the required fields are missing values, return an error.
     */
    fun createUpdateTempMember(memberItem: MemberItem? = null): Boolean {
        val memberName = newMemberName.value?.trim()
        val memberDepartment = newMemberDepartment.value?.trim()
        var memberEmail = newMemberEmail.value?.trim()
        val poolUid = _currentPool.value?.docId
        val ownerUid = _userAccount.value!!.uid

        if (memberName.isNullOrEmpty()) {
            errorNameText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterMembersNameErrorMessage))
            return false
        }
        if (memberDepartment.isNullOrEmpty()) {
            errorDepartmentText.value =
                ErrorMessage.ErrorText(stringResourcesProvider.getString(R.string.enterMembersDepartmentErrorMessage))
            return false
        }
        if (!memberEmail.isNullOrEmpty()) {
            memberEmail = memberEmail.trim()
        }

        if (poolUid.isNullOrEmpty()) {
            showToast.value =
                stringResourcesProvider.getString(R.string.unableToFindPoolErrorMessage)
            return false
        }
        if (!_isPoolAdmin.value) {
            showToast.value =
                stringResourcesProvider.getString(R.string.doNotHavePermissionErrorMessage)
            return false
        }
        val newMember = Member(
            ownerUid,
            memberItem?.member?.tempMemberUid,
            poolUid,
            memberName.trim(),
            memberEmail,
            memberDepartment.trim(),
            memberItem?.member?.bidTime,
            null,
            null,
            true
        )
        // If the memberItem is null the admin is trying to create a new temporary member.
        if (memberItem == null) {
            viewModelScope.launch {
                repository.addNewMemberToPool(newMember) {
                    if (!it.isNullOrEmpty()) showSnackBar.postValue(it)
                    else clearTempMemberValues()
                }
            }
            return true
        } else {
            // If the memberItem is not null the admin is trying to update a temporary members information.
            viewModelScope.launch {
                repository.updateTempPoolMember(newMember) {
                    if (!it.isNullOrEmpty()) showSnackBar.postValue(it)
                    else clearTempMemberValues()
                }
            }
            return true
        }
    }

    /**
     * Called after creating a new temporary pool member to clear edit text values.
     */
    private fun clearTempMemberValues() {
        newMemberName.value = null
        newMemberDepartment.value = null
        newMemberEmail.value = null
    }

    /**
     * Called when the pool admin tries to delete a temporary member from the active pool.
     */
    fun deleteTempMember(memberItem: MemberItem) {
        if (!_isPoolAdmin.value) {
            showToast.value =
                stringResourcesProvider.getString(R.string.doNotHavePermissionErrorMessage)
            return
        } else {
            viewModelScope.launch {
                repository.deleteTempPoolMember(memberItem.member) {
                    if (!it.isNullOrEmpty()) showToast.postValue(it)
                }
            }
        }
    }

    /**
     * Called after the user navigates to the chat fragment and marks all messages as read.
     */
    fun markMessagesAsRead(isUpdated: Boolean) {
        viewModelScope.launch {
            if (isUpdated && !_readChatListItems.value.second || !isUpdated) {
                _readChatListItems.emit(Pair(_chatList.value, true))
            }
        }
    }

    /**
     * Used to maintain the expanded state of the floating action button.
     */
    fun toggleFabButton() {
        isFabClicked.value = !isFabClicked.value
    }

    /**
     * Called after admin confirms the wrap time for the pool. If wrapTime value is null, the admin
     * is trying to clear the previous set wrap time.
     */
    fun setWrapTime(wrapTime: Date?, isConfirmed: Boolean = false) {
        if (isConfirmed) {
            viewModelScope.launch {
                repository.setPoolWrapTime(currentPool.value!!.docId, wrapTime) {
                    if (it.isNullOrEmpty()) setWinningMember(wrapTime)
                    else showToast.postValue(it)
                }
            }
        }
    }

    fun setIsScrolling(isScrolled: Boolean = false) {
        viewModelScope.launch {
            isScrolling.emit(isScrolled)
        }
    }

    /**
     * Called after the pools wrap time has successfully been updated in Firestore. The winners are calculated
     * using the pools settings created by the admin. The winners list is generated and displayed in the
     * summary fragment.
     */
    private fun setWinningMember(wrapTime: Date?) {
        if (wrapTime != null) {
            val winnersList = poolBetsList.value.calculateWinners(
                poolBetsList.value.size,
                currentPool.value?.betAmount?.toInt() ?: 0,
                poolMargin.value ?: "0",
                poolPIREnabled.value,
                wrapTime
            )

            viewModelScope.launch {
                repository.setPoolWinner(currentPool.value!!.docId, winnersList) {
                    if (it.isNullOrEmpty()) _poolWinningMembers.value = winnersList
                    else showToast.postValue(it)
                }
            }
        }
    }

    /**
     * Sets the bid time for the user. If there is an error display the message in a toast.
     */
    fun setUserPoolBet(time: Date?) {
        viewModelScope.launch {
            repository.setUserPoolBet(
                userAccount.value!!.activePool!!, userAccount.value!!.uid, time
            ) {
                if (!it.isNullOrEmpty()) showToast.postValue(it)
            }
        }
    }

    /**
     * Called when the pool admin sets the bid time for a temporary member.
     * If there is an error display the message in a toast.
     */
    fun setMemberPoolBet(poolId: String, tempMemberUid: String, time: Date?) {
        viewModelScope.launch {
            repository.setUserPoolBet(
                poolId, tempMemberUid, time
            ) {
                if (!it.isNullOrEmpty()) showToast.postValue(it)

            }
        }
    }

    /**
     * Called when the pool admin clicks the edit options menu item.
     */
    fun navigateToEditPool() {
        if (_isPoolAdmin.value && currentPool.value != null) {
            val action = HomeFragmentDirections.actionNavHomeToAddPoolFragment()
            action.poolId = currentPool.value!!.docId
            navigationCommand.postValue(NavigationCommand.To(action))
        } else {
            showToast.postValue(stringResourcesProvider.getString(R.string.unableToEditPool))
        }
    }

    private fun navigateToPoolFragment() {
        navigationCommand.postValue(NavigationCommand.To(HomeFragmentDirections.actionNavHomeToNavPools()))
    }
}