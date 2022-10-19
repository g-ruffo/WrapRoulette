package ca.veltus.wraproulette.utils

import android.content.Context
import android.util.Log
import ca.veltus.wraproulette.data.objects.*
import ca.veltus.wraproulette.databinding.MemberListItemBinding
import ca.veltus.wraproulette.databinding.PoolListItemBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.xwray.groupie.databinding.BindableItem
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

    fun createPool(production: String, password: String, date: String, onComplete: () -> Unit) {
        val docId: String = poolsCollectionReference.document().getId()
        val newPool = Pool(
            docId,
            FirebaseAuth.getInstance().currentUser?.uid ?: "",
            production,
            password,
            date,
            null,
            null,
            null,
            Timestamp.now(),
            null,
            mutableMapOf(FirebaseAuth.getInstance().currentUser?.uid!! to true)
        )

        poolsCollectionReference.document(docId).set(newPool).addOnSuccessListener {
            addPoolToUser(docId)
            addMemberToPool(docId, FirebaseAuth.getInstance().currentUser!!.uid)
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


    fun addPoolsListener(
        context: Context,
        onListen: (List<BindableItem<PoolListItemBinding>>) -> Unit
    ): ListenerRegistration {
        // TODO: Make private pools for individual users.
        return poolsCollectionReference.whereEqualTo(
            "users.${FirebaseAuth.getInstance().currentUser?.uid!!}",
            true
        ).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e(TAG, "addPoolsListener: User listener error.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            val items = mutableListOf<BindableItem<PoolListItemBinding>>()
            querySnapshot!!.documents.forEach {
                Log.i(TAG, "addPoolsListener: $it")
                items.add(PoolItem(it.toObject(Pool::class.java)!!))
            }
            onListen(items)

        }

    }

    fun addBidsListener(
        context: Context,
        pool: String,
        onListen: (List<BindableItem<MemberListItemBinding>>) -> Unit
    ): ListenerRegistration {
        // TODO: Make private pools for individual users.
        return poolsCollectionReference.document(pool).collection("members")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "addPoolsListener: User listener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<BindableItem<MemberListItemBinding>>()
                querySnapshot!!.documents.forEach {
                    Log.i(TAG, "addPoolsListener: $it")
                    items.add(MemberItem(it.toObject(Member::class.java)!!))
                }
                onListen(items)
            }

    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

}