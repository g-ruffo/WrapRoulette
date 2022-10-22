package ca.veltus.wraproulette.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
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
    app: Application
) : BaseViewModel(app) {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val userMessageEditText = MutableStateFlow<String?>(null)

    val userData = MutableStateFlow<User?>(null)

    val userBetTime = MutableStateFlow<Date?>(null)
    val poolTotalBets = MutableStateFlow<List<Member>>(listOf())

    val _chatList = MutableStateFlow<List<Message>>(listOf())
    val chatList: StateFlow<List<Message>?>
        get() = _chatList

    val _bids = MutableStateFlow<List<Member>>(listOf())

    val currentPool = MutableStateFlow<Pool?>(null)


    init {
        Log.i(TAG, "ViewModel: Initialized")
    }

    fun getChatData() {
        FirestoreUtil.getCurrentUser { user ->
            viewModelScope.launch {
                _chatList.emitAll(FirestoreUtil.getChatList(user.activePool!!))
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

    fun getPoolMemberList() {
        FirestoreUtil.getCurrentUser { user ->
            viewModelScope.launch {
                userData.value = user
                _bids.emitAll(FirestoreUtil.getPoolMemberList(user.activePool!!))
            }
        }
    }

    fun getPoolData() {
        FirestoreUtil.getCurrentUser { user ->
            viewModelScope.launch {
                currentPool.emitAll(FirestoreUtil.getPoolData(user.activePool!!))
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

            Log.i(TAG, "addBidMemberToList: ${poolTotalBets.value.size}")

        }
    }
}