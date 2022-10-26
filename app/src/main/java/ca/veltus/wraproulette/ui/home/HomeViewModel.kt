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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    val userMessageEditText = MutableStateFlow<String?>(null)

    private val _userAccount = MutableStateFlow<User?>(null)
    val userAccount: StateFlow<User?>
        get() = _userAccount

    val currentPool = MutableStateFlow<Pool?>(null)

    val userBetTime = MutableStateFlow<Date?>(null)
    val poolTotalBets = MutableStateFlow<List<Member>>(listOf())

    val _chatList = MutableStateFlow<List<Message>>(listOf())
    val chatList: StateFlow<List<Message>>
        get() = _chatList

    val _bids = MutableStateFlow<List<Member>>(listOf())
    val bids: StateFlow<List<Member>>
        get() = _bids


    val poolStartTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolRemainingBetTime = MutableStateFlow<Date>(Calendar.getInstance().time)
    val poolEndTime = MutableStateFlow<Date>(Calendar.getInstance().time)


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
            emit(time)
            delay(1000)
        }
    }

    val currentTimeDate = liveData {
        while (true) {
            emit(Calendar.getInstance().time)
            delay(1000)
        }
    }

    init {
        viewModelScope.launch {
            repository.getCurrentUserProfile().collect {
                _userAccount.value = it

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


    fun getPoolData(activePool: String) {
        viewModelScope.launch {
            launch {
                FirestoreUtil.getPoolData(activePool).collect { pool ->
                    currentPool.emit(pool)
                    poolStartTime.emit(pool!!.startTime!!)
                    poolRemainingBetTime.emit(pool.lockTime!!)
                }
            }

            launch {
                _chatList.emitAll(FirestoreUtil.getChatList(activePool))
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
    }

    fun getActivePoolDate(): Date? {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return parsedDate.parse(currentPool.value!!.date)
    }

    fun setUserBetTime(date: Date) {
        userBetTime.value = date
    }

    fun addBidMemberToList(members: List<Member>) {
        val list = mutableListOf<Member>()
        members.forEach {
            if (it.bidTime != null) {
                list.add(it)
            }
            poolTotalBets.value = list
        }
    }
}