package ca.veltus.wraproulette.data.repository

import android.util.Log
import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PoolListRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) : PoolListRepository {

    companion object {
        private const val TAG = "PoolListRepository"
    }

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun getPoolsList(): Flow<List<Pool>> {
        return firestore.collection("pools").whereEqualTo(
            "users.${currentUser!!.uid}", true
        ).snapshots().map { querySnapshot -> querySnapshot.toObjects() }
    }

    override suspend fun createPool(pool: Pool, onComplete: () -> Unit) {
        val docId: String = firestore.collection("pools").document().id
        val currentUser = FirebaseAuth.getInstance().currentUser
        pool.adminUid = currentUser!!.uid
        pool.adminName = currentUser.displayName!!
        pool.users = mutableMapOf(currentUser.uid to true)
        pool.docId = docId
        firestore.collection("pools").document(docId).set(pool).addOnSuccessListener {
            addPoolToUser(docId) {
                addMemberToPool(docId, currentUser.uid) {
                    onComplete()
                }
            }
        }
    }

    override suspend fun joinPool(
        production: String, password: String, date: String, onComplete: () -> Unit
    ) {
        firestore.collection("pools").whereEqualTo("production", production)
            .whereEqualTo("password", password).whereEqualTo("date", date).get()
            .addOnSuccessListener { querySnapshot ->
                Log.i(TAG, "joinPool: $querySnapshot")
                querySnapshot.documents.forEach {
                    Log.i(TAG, "joinPool: $it")
                    val pool = it.toObject(Pool::class.java)
                    addPoolToUser(pool!!.docId) {
                        addUserToPool(
                            pool.docId, FirebaseAuth.getInstance().currentUser!!.uid
                        ) {
                            addMemberToPool(
                                pool.docId, FirebaseAuth.getInstance().currentUser!!.uid
                            ) {
                                onComplete()
                            }
                        }
                    }
                }
            }
    }

    override fun setActivePool(poolId: String, onComplete: () -> Unit) {
        val newMap = mutableMapOf<String, Any>("activePool" to poolId)
        firestore.collection("users").document(currentUser!!.uid).set(newMap, SetOptions.merge())
            .addOnSuccessListener {
                onComplete()
            }
    }

    override fun addPoolToUser(poolId: String, onComplete: () -> Unit) {
        val newMap = mutableMapOf<String, Any>("pools.$poolId" to true)
        firestore.collection("users").document(currentUser!!.uid).update(newMap)
        setActivePool(poolId) {}
    }

    override fun addUserToPool(poolId: String, uid: String, onComplete: () -> Unit) {
        val newMap = mutableMapOf<String, Any>("users.$uid" to true)
        firestore.collection("pools").document(poolId).update(newMap)
    }

    override fun addMemberToPool(poolId: String, uid: String, onComplete: () -> Unit) {
        FirestoreUtil.getCurrentUser { user ->
            val member = Member(
                uid,
                null,
                poolId,
                user.displayName,
                user.email,
                user.department,
                null,
                user.profilePicturePath,
                null,
                true
            )
            firestore.collection("pools").document(poolId).collection("members").document(uid)
                .set(member)
        }
    }

}