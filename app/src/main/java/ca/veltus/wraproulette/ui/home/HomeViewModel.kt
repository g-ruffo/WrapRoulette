package ca.veltus.wraproulette.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.base.NavigationCommand
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
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

    val isPoolActive = MutableStateFlow<Boolean>(false)
    val isBettingOpen = MutableStateFlow<Boolean>(false)
    val isPoolAdmin = MutableStateFlow<Boolean>(false)
    val isFabClicked = MutableStateFlow<Boolean>(false)
    val isScrolling = MutableStateFlow<Boolean>(false)
    val userMessageEditText = MutableStateFlow<String?>(null)
    val userBetTime = MutableStateFlow<Date?>(null)
    val poolStartTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolRemainingBetTime = MutableStateFlow<Date?>(null)
    val poolEndTime = MutableStateFlow<Date?>(null)
    val poolWinningMember = MutableStateFlow<Member?>(null)

    val newMemberName = MutableStateFlow<String?>(null)
    val newMemberDepartment = MutableStateFlow<String?>(null)
    val newMemberEmail = MutableStateFlow<String?>(null)

    private val _actionbarTitle = MutableStateFlow<String>("Home")
    val actionbarTitle: StateFlow<String>
        get() = _actionbarTitle

    private val _currentPool = MutableStateFlow<Pool?>(null)
    val currentPool: StateFlow<Pool?>
        get() = _currentPool

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount

    val _poolTotalBets = MutableStateFlow<List<Member>>(listOf())
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
            if (poolEndTime.value != null) {
                emit(poolEndTime.value!!.time - poolStartTime.value.time)
                delay(1000)
            } else if (showNoData.value) {
                emit(0)
                delay(1000)
            } else {
                val time = Calendar.getInstance().time.time - poolStartTime.value.time
                emit(time)
                delay(1000)
            }
        }
    }

    val betTimeRemainingDate = liveData {
        while (true) {
            if (poolRemainingBetTime.value == null) {
                isBettingOpen.emit(true)
                emit(null)
                delay(1000)
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

    val currentTimeDate = liveData {
        while (true) {
            emit(Calendar.getInstance().time)
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


    fun getPoolData(activePool: String?) {
        if (!activePool.isNullOrEmpty()) {
            viewModelScope.launch {
                launch {
                    FirestoreUtil.getPoolData(activePool).collect { pool ->
                        if (pool != null) {
                            _currentPool.emit(pool)
                            poolStartTime.emit(pool.startTime!!)
                            poolRemainingBetTime.emit(pool.lockTime)
                            poolEndTime.emit(pool.endTime)
                            poolWinningMember.emit(pool.winner)
                            showNoData.emit(false)
                            if (pool.adminUid == _userAccount.value!!.uid) {
                                isPoolAdmin.emit(true)
                                _actionbarTitle.emit(pool.production + " (Admin)")
                            } else {
                                isPoolAdmin.emit(false)
                                _actionbarTitle.emit(pool.production)
                            }
                            if (pool.endTime != null) {
                                isPoolActive.emit(false)

                            } else {
                                isPoolActive.emit(true)
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
                            if (it.uid == userAccount.value!!.uid && it.bidTime != null) {
                                setUserBetTime(it.bidTime!!)
                            }
                        }
                    }
                }
            }
            showLoading.value = false
        } else return
    }

    private fun setUserBetTime(date: Date) {
        userBetTime.value = date
    }

    fun createNewPoolMember(): Boolean {
        val memberName = newMemberName.value
        val memberDepartment = newMemberDepartment.value
        var memberEmail = newMemberEmail.value
        val poolUid = _currentPool.value!!.docId

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
        val newMember =
            Member(
                null,
                poolUid,
                memberName.trim(),
                memberEmail,
                memberDepartment.trim(),
                null,
                null
            )
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
                    if (wrapTime != null) {
                        setWinningMember(wrapTime)
                    }
                }
            }
        }
    }

    fun setIsScrolling(isScrolled: Boolean = false) {
        viewModelScope.launch {
            isScrolling.emit(isScrolled)
        }
    }

    // TODO -> Replace filter function
    private fun setWinningMember(wrapTime: Date) {
        val winnersTimeList = mutableListOf<Long>()

        _poolTotalBets.value.forEach {
            if (it.bidTime != null) {
                winnersTimeList.add(it.bidTime!!.time)
            }
        }

        val numbers = mutableListOf(23, 12, 20, 47, 36, 55)

        val target = wrapTime.time

        val answer = winnersTimeList.fold(0L) { acc: Long?, num ->
            if (num <= target && (acc == null || num > acc)) num
            else acc
        }

        val winner: Member? = _poolTotalBets.value.firstOrNull { it.bidTime!!.time == answer!! }

        Log.i(
            TAG,
            "setWinningMember: $winner $answer ${_poolTotalBets.value[1].bidTime!!.time.toInt()} ${winnersTimeList[0]}"
        )

        viewModelScope.launch {
            FirestoreUtil.setPoolWinner(winner!!.poolId, winner) {
                poolWinningMember.value = winner
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