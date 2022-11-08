package ca.veltus.wraproulette.ui.home

import android.app.Application
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.objects.*
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.Constants
import ca.veltus.wraproulette.utils.FirestoreUtil
import ca.veltus.wraproulette.utils.calculateWinners
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AuthenticationRepository, private val app: Application
) : BaseViewModel(app) {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    val isBettingOpen = MutableStateFlow<Boolean>(false)
    val isPoolAdmin = MutableStateFlow<Boolean>(false)
    val isFabClicked = MutableStateFlow<Boolean>(false)
    val isScrolling = MutableStateFlow<Boolean>(false)
    val userMessageEditText = MutableStateFlow<String?>(null)
    val userBetTime = MutableStateFlow<Date?>(null)
    val poolStartTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolRemainingBetTime = MutableStateFlow<Date?>(null)
    val poolEndTime = MutableStateFlow<Date?>(null)
    val poolMargin = MutableStateFlow<String?>(null)
    val poolPIREnabled = MutableStateFlow<Boolean>(false)

    val newMemberName = MutableStateFlow<String?>(null)
    val newMemberDepartment = MutableStateFlow<String?>(null)
    val newMemberEmail = MutableStateFlow<String?>(null)

    private val _poolWinningMembers = MutableStateFlow<List<Member>>(listOf())
    val poolWinningMembers: StateFlow<List<Member>>
        get() = _poolWinningMembers

    private val _isPoolActive = MutableStateFlow<Boolean>(false)
    val isPoolActive: StateFlow<Boolean>
        get() = _isPoolActive

    private val _actionbarTitle = MutableStateFlow<String>("Home")
    val actionbarTitle: StateFlow<String>
        get() = _actionbarTitle

    private val _currentPool = MutableStateFlow<Pool?>(null)
    val currentPool: StateFlow<Pool?>
        get() = _currentPool

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount

    private val _poolTotalBets = MutableStateFlow<List<Member>>(listOf())
    val poolTotalBets: StateFlow<List<Member>>
        get() = _poolTotalBets

    val _chatList = MutableStateFlow<List<Message>>(listOf())
    val chatList: StateFlow<List<Message>>
        get() = _chatList

    val _readChatListItems = MutableStateFlow<Pair<List<Message>, Boolean>>(Pair(listOf(), false))
    val readChatListItems: StateFlow<Pair<List<Message>, Boolean>>
        get() = _readChatListItems

    val _bids = MutableStateFlow<List<Member>>(listOf())
    val bids: StateFlow<List<Member>>
        get() = _bids

    val timeWorkedDate = liveData {
        while (true) {
            val time = Calendar.getInstance().time.time - poolStartTime.value.time
            if (poolEndTime.value != null) {
                emit(poolEndTime.value!!.time - poolStartTime.value.time)
                delay(1000)
            } else if (showNoData.value || time > Constants.DAY) {
                emit(0)
                delay(1000)
            } else {
                time
                emit(time)
                delay(1000)
            }
        }
    }

    val betTimeRemainingDate = liveData {
        while (true) {
            if (poolRemainingBetTime.value == null) {
                emit(null)
                delay(1000)
                if (isPoolActive.value) {
                    isBettingOpen.emit(true)
                } else {
                    isBettingOpen.emit(false)
                }
            } else {
                val time = poolRemainingBetTime.value!!.time - Calendar.getInstance().time.time
                if (time > 0) {
                    emit(time)
                    isBettingOpen.emit(true)
                    delay(1000)
                } else if (showNoData.value) {
                    emit(0)
                    delay(1000)
                } else {
                    isBettingOpen.emit(false)
                    emit(0)
                    delay(1000)
                }
            }
        }
    }

    val currentTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val currentTimeDate = liveData<Date> {
        while (true) {
            emit(Calendar.getInstance().time)
            currentTime.emit(Calendar.getInstance().time)
            delay(1000)
        }
    }

    init {
        showLoading.value = true
        viewModelScope.launch {
            repository.getCurrentUserProfile().collectLatest {
                _userAccount.value = it
                if (it != null && !it.activePool.isNullOrEmpty()) {
                    getPoolData(it.activePool)
                } else {
                    showLoading.emit(false)
                    showNoData.emit(true)
                }
            }
        }
    }

    fun sendChatMessage(onComplete: () -> Unit) {
        FirestoreUtil.getCurrentUser { user ->
            val message = Message(
                userMessageEditText.value!!,
                Date(Calendar.getInstance().time.time),
                user.uid,
                user.displayName,
                user.profilePicturePath,
                ""
            )
            FirestoreUtil.sendChatMessage(user.activePool!!, message) {
                viewModelScope.launch {
                    userMessageEditText.emit(null)
                    onComplete()
                }
            }
        }
    }


    private fun getPoolData(activePool: String?) {
        if (!activePool.isNullOrEmpty()) {
            viewModelScope.launch {
                launch {
                    FirestoreUtil.getPoolData(activePool).collect { pool ->
                        if (pool != null) {
                            _currentPool.emit(pool)
                            poolStartTime.emit(pool.startTime!!)
                            poolRemainingBetTime.emit(pool.lockTime)
                            poolEndTime.emit(pool.endTime)
                            _poolWinningMembers.emit(pool.winners)
                            poolMargin.emit(pool.margin)
                            poolPIREnabled.emit(pool.pIRRulesEnabled)
                            showNoData.emit(false)
                            if (pool.adminUid == _userAccount.value!!.uid) {
                                isPoolAdmin.emit(true)
                                _actionbarTitle.emit(pool.production + " (Admin)")
                            } else {
                                isPoolAdmin.emit(false)
                                _actionbarTitle.emit(pool.production)
                            }
                            if (pool.endTime != null || (Calendar.getInstance().time.time - poolStartTime.value.time) > Constants.DAY) {
                                _isPoolActive.emit(false)
                            } else {
                                _isPoolActive.emit(true)
                            }
                        } else {
                            showNoData.emit(true)
                            _actionbarTitle.emit("Home")
                        }
                        showLoading.emit(false)
                    }
                }

                launch {
                    FirestoreUtil.getChatList(activePool).collect {
                        _chatList.emit(it)
                        markMessagesAsRead(true)
                    }
                }

                launch {
                    FirestoreUtil.getPoolMemberList(activePool).collect { members ->
                        _bids.emit(members)
                        addBidMemberToList(members)
                        members.forEach {
                            if (it.uid == userAccount.value!!.uid) {
                                setUserBetTime(it.bidTime)
                            }
                        }
                    }
                }
            }
            showLoading.value = false
        } else return
    }

    private fun setUserBetTime(date: Date?) {
        userBetTime.value = date
    }

    fun leavePool() {
        FirestoreUtil.leavePool(currentPool.value?.docId ?: "", userAccount.value?.uid ?: "") {
            showLoading.value = false
            if (!it.isNullOrEmpty()) showToast.value = it
            else _actionbarTitle.value = "Home"
        }
    }

    fun loadTempMemberValues(member: Member?) {
        newMemberName.value = member?.displayName
        newMemberDepartment.value = member?.department
        newMemberEmail.value = member?.email
    }

    fun createUpdateTempMember(memberItem: MemberItem? = null): Boolean {
        val memberName = newMemberName.value
        val memberDepartment = newMemberDepartment.value
        var memberEmail = newMemberEmail.value
        val poolUid = _currentPool.value?.docId
        val ownerUid = _userAccount.value!!.uid

        if (memberName.isNullOrEmpty()) {
            showToast.value = "Please Enter Members Name"
            return false
        }
        if (memberDepartment.isNullOrEmpty()) {
            showToast.value = "Please Enter Members Department"
            return false
        }
        if (!memberEmail.isNullOrEmpty()) {
            memberEmail = memberEmail.trim()
        }

        if (poolUid.isNullOrEmpty()) {
            showToast.value = "Unable To Find Pool"
            return false
        }
        if (!isPoolAdmin.value) {
            showToast.value = "You Do Not Have Permission"
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
        if (memberItem == null) {
            FirestoreUtil.addNewMemberToPool(newMember) {
                if (!it.isNullOrEmpty()) {
                    showToast.value = "$it"
                } else {
                    newMemberName.value = null
                    newMemberDepartment.value = null
                    newMemberEmail.value = null
                }
            }
            return true
        } else {
            FirestoreUtil.updateTempPoolMember(newMember) {
                if (!it.isNullOrEmpty()) {
                    showToast.value = "$it"
                } else {
                    newMemberName.value = null
                    newMemberDepartment.value = null
                    newMemberEmail.value = null
                }
            }
            return true
        }
    }

    fun deleteTempMember(memberItem: MemberItem) {
        if (!isPoolAdmin.value) {
            showToast.value = "You Do Not Have Permission"
            return
        } else {
            FirestoreUtil.deleteTempPoolMember(memberItem.member) {

            }
        }
    }

    private fun addBidMemberToList(members: List<Member>) {
        viewModelScope.launch {
            val list = mutableListOf<Member>()
            members.forEach {
                if (it.bidTime != null) {
                    list.add(it)
                }
                _poolTotalBets.emit(list)
            }
        }
    }

    fun markMessagesAsRead(isUpdated: Boolean) {
        viewModelScope.launch {
            if (isUpdated && !_readChatListItems.value.second || !isUpdated) {
                _readChatListItems.emit(Pair(_chatList.value, true))
            }
        }
    }

    fun toggleFabButton() {
        isFabClicked.value = !isFabClicked.value
    }

    fun setWrapTime(wrapTime: Date?, isConfirmed: Boolean = false) {
        if (isConfirmed) {
            viewModelScope.launch {
                FirestoreUtil.setPoolWrapTime(currentPool.value!!.docId, wrapTime) {
                    setWinningMember(wrapTime)
                }
            }
        }
    }

    fun setIsScrolling(isScrolled: Boolean = false) {
        viewModelScope.launch {
            isScrolling.emit(isScrolled)
        }
    }

    private fun setWinningMember(wrapTime: Date?) {
        if (wrapTime != null) {
            val winnersList = poolTotalBets.value.calculateWinners(
                poolTotalBets.value.size,
                currentPool.value?.betAmount?.toInt() ?: 0,
                poolMargin.value ?: "0",
                poolPIREnabled.value,
                wrapTime
            )

            viewModelScope.launch {
                FirestoreUtil.setPoolWinner(currentPool.value!!.docId, winnersList) {
                    if (it.isNullOrEmpty()) {
                        _poolWinningMembers.value = winnersList
                    } else {
                        showToast.postValue(it)
                    }
                }
            }
        }
    }

    fun navigateToEditPool() {
        if (isPoolAdmin.value && currentPool.value != null) {
            val action = HomeFragmentDirections.actionNavHomeToAddPoolFragment()
            action.poolId = currentPool.value!!.docId
            navigationCommand.postValue(NavigationCommand.To(action))
        } else {
            showToast.postValue("You Are Unable To Edit This Pool")
        }
    }
}