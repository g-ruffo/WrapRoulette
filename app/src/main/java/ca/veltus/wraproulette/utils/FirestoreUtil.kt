package ca.veltus.wraproulette.utils

import android.util.Log
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.*


object FirestoreUtil {
    private const val TAG = "FirestoreUtil"

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


    fun initCurrentUserIfFirstTime(department: String, onComplete: () -> Unit) {
        currentUserDocReference.get().addOnSuccessListener {
            if (!it.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                    department,
                    null,
                    null
                )
                currentUserDocReference.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else {
                onComplete()
            }
        }

    }

    fun updateCurrentUser(
        uid: String = FirebaseAuth.getInstance().currentUser?.uid ?: "",
        displayName: String = "",
        email: String = FirebaseAuth.getInstance().currentUser?.email ?: "",
        department: String = "",
        profilePicturePath: String? = null,
        pools: MutableMap<String, Any>? = null
    ) {

        val userFieldMap = mutableMapOf<String, Any>()

        if (displayName.isNotBlank()) userFieldMap["name"] = displayName
        if (department.isNotBlank()) userFieldMap["department"] = department
        if (profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath
        if (pools != null) userFieldMap["pools"] = pools
        currentUserDocReference.update(userFieldMap)


    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocReference.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    fun getCurrentUserFlow(): Flow<User?> {
        return currentUserDocReference.snapshots().map { it.toObject() }
    }


    fun createPool(pool: Pool, onComplete: () -> Unit) {
        val docId: String = poolsCollectionReference.document().id
        val currentUser = FirebaseAuth.getInstance().currentUser
        pool.adminUid = currentUser!!.uid
        pool.adminName = currentUser.displayName!!
        pool.users = mutableMapOf(currentUser.uid to true)
        pool.docId = docId
        poolsCollectionReference.document(docId).set(pool).addOnSuccessListener {
            addPoolToUser(docId)
            addMemberToPool(docId, currentUser.uid)
            onComplete()
        }
    }

    fun joinPool(production: String, password: String, date: String, onComplete: () -> Unit) {
        poolsCollectionReference.whereEqualTo("production", production)
            .whereEqualTo("password", password)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.i(TAG, "joinPool: $querySnapshot")
                querySnapshot.documents.forEach {
                    Log.i(TAG, "joinPool: $it")
                    val pool = it.toObject(Pool::class.java)
                    addPoolToUser(pool!!.docId)
                    addUserToPool(pool.docId, FirebaseAuth.getInstance().currentUser!!.uid)
                    addMemberToPool(pool.docId, FirebaseAuth.getInstance().currentUser!!.uid)

                }
                onComplete()
            }
    }

    fun setActivePool(poolId: String, onComplete: () -> Unit) {
        val newMap = mutableMapOf<String, Any>("activePool" to poolId)
        currentUserDocReference.set(newMap, SetOptions.merge()).addOnSuccessListener {
            onComplete()
        }
    }

    fun setUserPoolBet(poolId: String, userUid: String, bid: Date, onComplete: () -> Unit) {
        poolsCollectionReference.document(poolId).collection("members").document(userUid)
            .update("bidTime", bid).addOnSuccessListener {
                onComplete()
            }
    }

    private fun addPoolToUser(poolId: String) {
        val newMap = mutableMapOf<String, Any>("pools.$poolId" to true)
        currentUserDocReference.update(newMap)
        setActivePool(poolId) {
        }
    }

    private fun addUserToPool(poolId: String, uid: String) {
        val newMap = mutableMapOf<String, Any>("users.$uid" to true)
        poolsCollectionReference.document(poolId).update(newMap)
    }

    private fun addMemberToPool(poolId: String, uid: String) {
        getCurrentUser { user ->
            val member = Member(
                uid,
                poolId,
                user.displayName,
                user.email,
                user.department,
                null,
                user.profilePicturePath
            )
            poolsCollectionReference.document(poolId).collection("members").document(uid)
                .set(member)
        }
    }


    fun getPoolMemberList(poolId: String): Flow<List<Member>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("pools")
            .document(poolId)
            .collection("members")
            .snapshots().map { querySnapshot -> querySnapshot.toObjects() }
    }

    fun getPoolData(poolId: String): Flow<Pool?> {
        if (poolId.isNullOrBlank()) {
            return flowOf()
        }
        val db = FirebaseFirestore.getInstance()
        return db.collection("pools")
            .document(poolId).snapshots()
            .map { querySnapshot -> querySnapshot.toObject<Pool?>() }
    }

    fun getPoolsList(userUid: String): Flow<List<Pool>> {
        return poolsCollectionReference.whereEqualTo(
            "users.$userUid", true
        ).snapshots().map { querySnapshot -> querySnapshot.toObjects() }
    }

    fun sendChatMessage(activePool: String, message: Message, onComplete: () -> Unit) {
        val docId =
            messagesCollectionReference.document(activePool).collection("chat").document().id
        message.messageUid = docId
        messagesCollectionReference.document(activePool).collection("chat").document(docId)
            .set(message).addOnSuccessListener {
                onComplete()
            }
    }

    fun getChatList(activePool: String): Flow<List<Message>> {
        return messagesCollectionReference.document(activePool).collection("chat")
            .orderBy("time", Query.Direction.ASCENDING).snapshots()
            .map { querySnapshot -> querySnapshot.toObjects() }
    }
}