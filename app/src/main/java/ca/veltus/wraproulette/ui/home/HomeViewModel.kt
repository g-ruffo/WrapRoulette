package ca.veltus.wraproulette.ui.home

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
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

    val _chatListArray = MutableStateFlow<List<Message>?>(null)
    val chatListArray: StateFlow<List<Message>?>
        get() = _chatListArray

    val _betListArray = MutableStateFlow<List<Member>?>(null)
    val betListArray: StateFlow<List<Member>?>
        get() = _betListArray

    val _bids = MutableStateFlow<List<Member>>(listOf())


    init {
//        getPoolData()
    }

    fun getPoolData() {
        FirestoreUtil.getCurrentUser { user ->
            viewModelScope.launch {
                _bids.emitAll(FirestoreUtil.getPoolMemberList(user.activePool!!))

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage() {
        val document = firestore.collection("chat").document(userData.value!!.activePool!!).collection("messages")
            .document()

        val message = Message(
            userMessageEditText.value!!,
            Date(Calendar.getInstance().time.time),
            userData.value!!.uid,
            userData.value!!.profilePicturePath,
            document.id
        )
        document.set(message)
        Log.i(TAG, "sendMessage: $message")
    }

}