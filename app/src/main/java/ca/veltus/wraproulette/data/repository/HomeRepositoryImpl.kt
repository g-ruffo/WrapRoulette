package ca.veltus.wraproulette.data.repository

import android.util.Log
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.utils.StringResourcesProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val stringResourcesProvider: StringResourcesProvider
) : HomeRepository {

    companion object {
        private const val TAG = "HomeRepository"
    }

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocReference: DocumentReference
        get() = firestoreInstance.document(
            "users/${
                FirebaseAuth.getInstance().uid ?: throw NullPointerException(
                    "UID is null."
                )
            }"
        )

    private val poolsCollectionReference = firestoreInstance.collection("pools")
    private val messagesCollectionReference = firestoreInstance.collection("messages")
    private val usersCollectionReference = firestoreInstance.collection("users")

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun getCurrentUserProfile(): Flow<User?> {
        return firestore.collection("users").document(currentUser!!.uid).snapshots()
            .map<DocumentSnapshot, User?> { it.toObject() }.onCompletion {
                Log.i(TAG, "getCurrentUserProfile: $it")
            }.catch {
                Log.e(TAG, "getCurrentUserProfile: $it")
                Firebase.crashlytics.recordException(it)
            }
    }

    override suspend fun getCurrentUser(onComplete: (User) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = currentUserDocReference.get().await().toObject(User::class.java)
                onComplete(user!!)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "getCurrentUser: ${e.message}")
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun checkAdminForUpdate(poolId: String) {
        val adminFieldMap = mutableMapOf<String, Any>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pool = poolsCollectionReference.document(poolId).get().await()
                    .toObject(Pool::class.java)
                if (pool != null) {
                    val user = usersCollectionReference.document(pool.adminUid).get().await()
                        .toObject(User::class.java)
                    if (user != null) {
                        if (user.displayName != pool.adminName) adminFieldMap["adminName"] =
                            user.displayName
                        if (user.profilePicturePath != pool.adminProfileImage) adminFieldMap["adminProfileImage"] =
                            user.profilePicturePath!!
                        if (adminFieldMap.isNotEmpty()) {
                            poolsCollectionReference.document(pool.docId).update(adminFieldMap)
                                .await()
                        }
                    }
                }
                checkMembersForUpdate(poolId)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "getCurrentUser: ${e.message}")
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun checkMembersForUpdate(poolId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val members =
                    poolsCollectionReference.document(poolId).collection("members").get().await()
                        .toObjects(Member::class.java)
                if (members.isNotEmpty()) {
                    members.forEach { member ->
                        if (member.tempMemberUid == null) {
                            val memberUpdateMap = mutableMapOf<String, Any>()
                            val user = usersCollectionReference.document(member.uid!!).get().await()
                                .toObject(User::class.java)
                            if (user != null) {
                                if (member.displayName != user.displayName) memberUpdateMap["displayName"] =
                                    user.displayName
                                if (member.profilePicturePath != user.profilePicturePath) memberUpdateMap["profilePicturePath"] =
                                    user.profilePicturePath!!

                                if (member.email != user.email) memberUpdateMap["email"] =
                                    user.email

                                if (memberUpdateMap.isNotEmpty()) {
                                    poolsCollectionReference.document(poolId).collection("members")
                                        .document(member.uid).update(memberUpdateMap).await()
                                }
                            }
                        }
                    }
                }

            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "getCurrentUser: ${e.message}")
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun checkChatMessagesForUpdate(poolId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messages =
                    messagesCollectionReference.document(poolId).collection("chat").get().await()
                        .toObjects(Message::class.java)
                if (messages.isNotEmpty()) {
                    messages.forEach { message ->
                        val messageUpdateMap = mutableMapOf<String, Any>()
                        val user =
                            usersCollectionReference.document(message.senderUid).get().await()
                                .toObject(User::class.java)
                        if (user != null) {
                            if (message.senderName != user.displayName) messageUpdateMap["senderName"] =
                                user.displayName
                            if (message.profilePicture != user.profilePicturePath) messageUpdateMap["profilePicture"] =
                                user.profilePicturePath!!

                            if (messageUpdateMap.isNotEmpty()) {
                                messagesCollectionReference.document(poolId).collection("chat")
                                    .document(message.messageUid).update(messageUpdateMap).await()
                            }
                        }
                    }
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "getCurrentUser: ${e.message}")
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun getPoolData(poolId: String?): Flow<Pool?> {
        if (poolId.isNullOrBlank()) return flowOf()
        checkAdminForUpdate(poolId)
        return firestore.collection("pools").document(poolId).snapshots()
            .map { querySnapshot -> querySnapshot.toObject<Pool?>() }.onCompletion {
                Log.i(TAG, "getPoolData OnCompletion: $it")
            }.catch {
                Log.e(TAG, "getPoolData Catch: ${it.message}  Caused: ${it.cause}")
                Firebase.crashlytics.recordException(it)
            }
    }

    override suspend fun getPoolMemberList(poolId: String): Flow<List<Member>> {
        return poolsCollectionReference.document(poolId).collection("members").snapshots()
            .map<QuerySnapshot, List<Member>> { querySnapshot -> querySnapshot.toObjects() }
            .onCompletion {
                Log.i(TAG, "getPoolMemberList OnCompletion: $it")
            }.catch {
                Log.e(TAG, "getPoolMemberList: $it")
                Firebase.crashlytics.recordException(it)
            }
    }

    override suspend fun getChatList(activePool: String): Flow<List<Message>> {
        checkChatMessagesForUpdate(activePool)
        return messagesCollectionReference.document(activePool).collection("chat")
            .orderBy("time", Query.Direction.ASCENDING).snapshots()
            .map<QuerySnapshot, List<Message>> { querySnapshot -> querySnapshot.toObjects() }
            .onCompletion {
                Log.i(TAG, "getChatList: $it")
            }.catch {
                Log.e(TAG, "getChatList: $it")
                Firebase.crashlytics.recordException(it)
            }
    }

    override suspend fun setUserPoolBet(
        poolId: String, userUid: String, bid: Date?, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                poolsCollectionReference.document(poolId).collection("members").document(userUid)
                    .update("bidTime", bid).await()
                // Save bet to pools mutable map
                poolsCollectionReference.document(poolId).update("bets.$userUid", bid).await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "setUserPoolBet: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun setPoolWrapTime(
        poolId: String, wrapTime: Date?, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                poolsCollectionReference.document(poolId).update("endTime", wrapTime).await()
                if (wrapTime == null) {
                    poolsCollectionReference.document(poolId).update("winners", listOf<Member>())
                        .await()
                }
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "setPoolWrapTime: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun setPoolWinner(
        poolId: String, winners: List<Member>, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                poolsCollectionReference.document(poolId).update("winners", winners).await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "setPoolWinner: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun leavePool(
        poolUid: String, userUid: String, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Set the pools users map field to false.
                poolsCollectionReference.document(poolUid).update("users.$userUid", false).await()
                // Set the activeMember field in the users member document to false.
                poolsCollectionReference.document(poolUid).collection("members").document(userUid)
                    .update("activeMember", false).await()
                // Set current users active pool to null.
                currentUserDocReference.update("activePool", null).await()
                // Update current users pools map field to poolUid is false.
                currentUserDocReference.update("pools.$poolUid", false).await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "leavePool: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun addNewMemberToPool(member: Member, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val tempMemberUid: String =
                poolsCollectionReference.document(member.poolId).collection("members").document().id
            member.tempMemberUid = tempMemberUid
            val result = poolsCollectionReference.document(member.poolId).collection("members")
                .whereEqualTo("displayName", member.displayName)
                .whereEqualTo("department", member.department).get().await()
            if (result.isEmpty) {
                try {
                    poolsCollectionReference.document(member.poolId).collection("members")
                        .document(tempMemberUid).set(member).await()
                    onComplete(null)
                } catch (e: FirebaseFirestoreException) {
                    Log.e(TAG, "addNewMemberToPool: ${e.message}")
                    onComplete(e.message)
                    Firebase.crashlytics.recordException(e)
                }
            } else {
                onComplete(stringResourcesProvider.getString(R.string.memberExistsErrorMessage))
            }
        }
    }

    override suspend fun updateTempPoolMember(member: Member, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = poolsCollectionReference.document(member.poolId).collection("members")
                .whereEqualTo("displayName", member.displayName)
                .whereEqualTo("department", member.department).get().await()
            if (result.isEmpty) {
                try {
                    poolsCollectionReference.document(member.poolId).collection("members")
                        .document(member.tempMemberUid!!).set(member, SetOptions.merge()).await()
                    onComplete(null)
                } catch (e: FirebaseFirestoreException) {
                    Log.e(TAG, "updateTempPoolMember: ${e.message}")
                    onComplete(e.message)
                    Firebase.crashlytics.recordException(e)
                }
            } else {
                onComplete(stringResourcesProvider.getString(R.string.memberExistsErrorMessage))
            }
        }
    }

    override suspend fun deleteTempPoolMember(member: Member, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                poolsCollectionReference.document(member.poolId).collection("members")
                    .document(member.tempMemberUid!!).delete().await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "deleteTempPoolMember: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun sendChatMessage(
        activePool: String, message: Message, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docId = messagesCollectionReference.document(activePool).collection("chat")
                    .document().id
                message.messageUid = docId
                messagesCollectionReference.document(activePool).collection("chat").document(docId)
                    .set(message).await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "sendChatMessage: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }
}