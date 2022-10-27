package ca.veltus.wraproulette.ui.home

import android.app.Application
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
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
    private val repository: AuthenticationRepository,
    private val app: Application
) : BaseViewModel(app) {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    val isBettingOpen = MutableStateFlow<Boolean>(false)
    val isPoolAdmin = MutableStateFlow<Boolean>(false)
    val isFabClicked = MutableStateFlow<Boolean>(false)
    val userMessageEditText = MutableStateFlow<String?>(null)
    val currentPool = MutableStateFlow<Pool?>(null)
    val userBetTime = MutableStateFlow<Date?>(null)
    val poolStartTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolRemainingBetTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolEndTime = MutableStateFlow<Date?>(null)

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
            val time = Calendar.getInstance().time.time - poolStartTime.value.time
            emit(time)
            delay(1000)
        }
    }

    val betTimeRemainingDate = liveData {
        while (true) {
            val time = poolRemainingBetTime.value.time - Calendar.getInstance().time.time
            if (time > 0) {
                emit(time)
                isBettingOpen.emit(true)
                delay(1000)
            } else {
                isBettingOpen.emit(false)
                emit(0)
                delay(1000)
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
        showLoading.postValue(true)
        viewModelScope.launch {
            repository.getCurrentUserProfile().collectLatest {
                _userAccount.value = it
                if (it != null && !it.activePool.isNullOrEmpty()) {
                    getPoolData(it.activePool)
                } else {
                    showLoading.postValue(false)
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
                        currentPool.emit(pool)
                        poolStartTime.emit(pool!!.startTime!!)
                        poolRemainingBetTime.emit(pool.lockTime!!)
                        poolEndTime.emit(pool.endTime)
                        if (pool.adminUid == _userAccount.value!!.uid) {
                            isPoolAdmin.emit(true)
                        } else {
                            isPoolAdmin.emit(false)
                        }
                        showLoading.postValue(false)
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
                                setUserBetTime(it.bidTime)
                            }
                        }
                    }
                }
            }
            showLoading.postValue(false)
        } else return
    }

    private fun setUserBetTime(date: Date) {
        userBetTime.value = date
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
                }
            }
        }
    }
}