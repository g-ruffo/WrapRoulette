package ca.veltus.wraproulette.utils

import android.util.Log
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.*
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
        currentUserDocReference.get().addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    fun createPool(pool: Pool, onComplete: (String?) -> Unit) {
        val docId: String = poolsCollectionReference.document().id
        val currentUser = FirebaseAuth.getInstance().currentUser
        pool.adminUid = currentUser!!.uid
        pool.adminName = currentUser.displayName!!
        pool.users = mutableMapOf(currentUser.uid to true)
        pool.docId = docId

        poolsCollectionReference.whereEqualTo("production", pool.production)
            .whereEqualTo("password", pool.password).whereEqualTo("date", pool.date).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    poolsCollectionReference.document(docId).set(pool).addOnSuccessListener {
                        addPoolToUser(docId)
                        addMemberToPool(docId, currentUser.uid)
                        onComplete(null)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "createPool: $exception")
                    }
                } else {
                    onComplete("Pool already exists")
                }
            }
    }

    fun updatePool(pool: Pool, onComplete: () -> Unit) {
        poolsCollectionReference.document(pool.docId).set(pool, SetOptions.merge())
            .addOnSuccessListener {
                onComplete()
            }.addOnFailureListener {
                Log.e(TAG, "createPool: $it")
            }
    }

    fun joinPool(
        production: String, password: String, date: String, onComplete: (String?) -> Unit
    ) {
        poolsCollectionReference.whereEqualTo("production", production)
            .whereEqualTo("password", password).whereEqualTo("date", date).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.i(TAG, "querySnapshot.isEmpty: $querySnapshot")
                    onComplete("No Pool Found Matching Credentials")
                } else {
                    querySnapshot.documents.forEach {
                        Log.i(TAG, "joinPool: $it")
                        val pool = it.toObject(Pool::class.java)
                        currentUserDocReference.get().addOnSuccessListener { user ->
                            if (user.contains("pools.${pool!!.docId}")) {
                                onComplete("You are already a member of this pool")
                            } else {
                                addPoolToUser(pool.docId)
                                addUserToPool(
                                    pool.docId, FirebaseAuth.getInstance().currentUser!!.uid
                                )
                                addMemberToPool(
                                    pool.docId, FirebaseAuth.getInstance().currentUser!!.uid
                                )
                                onComplete(null)
                            }
                        }.addOnFailureListener { exception ->
                            onComplete(exception.message)
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                onComplete(exception.message)
            }
    }

    fun setActivePool(poolId: String, onComplete: () -> Unit) {
        val newMap = mutableMapOf<String, Any>("activePool" to poolId)
        currentUserDocReference.set(newMap, SetOptions.merge()).addOnSuccessListener {
            onComplete()
        }
    }

    fun setUserPoolBet(poolId: String, userUid: String, bid: Date?, onComplete: (String?) -> Unit) {
        poolsCollectionReference.document(poolId).collection("members").document(userUid)
            .update("bidTime", bid).addOnSuccessListener {
                onComplete(null)
            }.addOnFailureListener { exception ->
                onComplete(exception.message)
            }
    }

    fun setPoolWrapTime(poolId: String, wrapTime: Date?, onComplete: () -> Unit) {
        poolsCollectionReference.document(poolId).update("endTime", wrapTime).addOnSuccessListener {
            onComplete()
        }
    }

    fun setPoolWinner(poolId: String, winners: List<Member>, onComplete: (String?) -> Unit) {
        poolsCollectionReference.document(poolId).update("winners", winners).addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { exception ->
            onComplete(exception.message)
        }
    }

    private fun addPoolToUser(poolId: String) {
        val newMap = mutableMapOf<String, Any>("pools.$poolId" to true)
        currentUserDocReference.update(newMap)
        setActivePool(poolId) {}
    }

    private fun addUserToPool(poolId: String, uid: String) {
        val newMap = mutableMapOf<String, Any>("users.$uid" to true)
        poolsCollectionReference.document(poolId).update(newMap)
    }

    fun addNewMemberToPool(member: Member, onComplete: (String?) -> Unit) {
        val tempMemberUid: String =
            poolsCollectionReference.document(member.poolId).collection("members").document().id
        member.tempMemberUid = tempMemberUid
        poolsCollectionReference.document(member.poolId).collection("members")
            .whereEqualTo("displayName", member.displayName)
            .whereEqualTo("department", member.department).get().addOnSuccessListener {
                if (it.isEmpty) {
                    poolsCollectionReference.document(member.poolId).collection("members")
                        .document(tempMemberUid).set(member).addOnSuccessListener {
                            onComplete(null)
                        }.addOnFailureListener { exception ->
                            onComplete(exception.message)
                        }
                } else {
                    onComplete("Member already exists")
                }
            }
    }


    private fun addMemberToPool(poolId: String, uid: String) {
        getCurrentUser { user ->
            val member = Member(
                uid,
                null,
                poolId,
                user.displayName,
                user.email,
                user.department,
                null,
                user.profilePicturePath,
                null
            )
            poolsCollectionReference.document(poolId).collection("members").document(uid)
                .set(member)
        }
    }


    fun getPoolMemberList(poolId: String): Flow<List<Member>> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("pools").document(poolId).collection("members").snapshots()
            .map<QuerySnapshot, List<Member>> { querySnapshot -> querySnapshot.toObjects() }
            .onCompletion {
                Log.i(TAG, "getPoolMemberList OnCompletion: $it")
            }.catch {
                Log.e(TAG, "getPoolMemberList: $it")
            }
    }

    fun getPoolData(poolId: String): Flow<Pool?> {
        if (poolId.isNullOrBlank()) {
            return flowOf()
        }
        val db = FirebaseFirestore.getInstance()
        return db.collection("pools").document(poolId).snapshots()
            .map { querySnapshot -> querySnapshot.toObject<Pool?>() }.onCompletion {
                Log.i(TAG, "getPoolData OnCompletion: $it")
            }.catch {
                Log.e(TAG, "getPoolData Catch: ${it.message}  Caused: ${it.cause}")
            }
    }

    fun getEditPool(poolId: String, onComplete: (Pool?) -> Unit) {
        poolsCollectionReference.document(poolId).get().addOnSuccessListener {
            onComplete(it.toObject(Pool::class.java))
        }.addOnFailureListener {
            Log.e(TAG, "getEditPool: $it")
        }
    }

    fun deletePool(poolUid: String, onComplete: () -> Unit) {
        poolsCollectionReference.document(poolUid).delete().addOnSuccessListener {
            deletePoolFromUser(poolUid) {
                onComplete()
            }
        }.addOnFailureListener {
            Log.e(TAG, "getEditPool: $it")
        }
    }

    fun deletePoolFromUser(poolUid: String, onComplete: () -> Unit) {
        currentUserDocReference.update("activePool", null).addOnSuccessListener {
            currentUserDocReference.update("pools.$poolUid", false).addOnSuccessListener {
                onComplete()
            }.addOnFailureListener {
                Log.e(TAG, "getEditPool: $it")
            }
        }.addOnFailureListener {
            Log.e(TAG, "getEditPool: $it")
        }
    }


    fun getPoolsList(userUid: String): Flow<List<Pool>> {
        return poolsCollectionReference.whereEqualTo(
            "users.$userUid", true
        ).snapshots().map<QuerySnapshot, List<Pool>> { querySnapshot -> querySnapshot.toObjects() }
            .onCompletion {
                Log.i(TAG, "getPoolsList: $it")
            }.catch {
                Log.e(TAG, "getPoolsList: $it")
            }
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
            .map<QuerySnapshot, List<Message>> { querySnapshot -> querySnapshot.toObjects() }
            .onCompletion {
                Log.i(TAG, "getChatList: $it")
            }.catch {
                Log.e(TAG, "getChatList: $it")
            }
    }
}