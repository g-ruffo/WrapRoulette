package ca.veltus.wraproulette.data.repository

import android.util.Log
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.Feedback
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) : AuthenticationRepository {

    companion object {
        private const val TAG = "AuthenticationRepoLog"
    }

    private val currentUserDocReference: DocumentReference
        get() = firestore.document(
            "users/${
                FirebaseAuth.getInstance().uid ?: throw NullPointerException(
                    "UID is null."
                )
            }"
        )

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun getCurrentUserProfile(): Flow<User?> {
        return firestore.collection("users").document(currentUser!!.uid).snapshots()
            .map<DocumentSnapshot, User?> { it.toObject() }.onCompletion {
                Log.i(TAG, "getCurrentUserProfile: $it")
            }.catch {
                Firebase.crashlytics.recordException(it)
                Log.e(TAG, "getCurrentUserProfile: $it")
            }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            e.printStackTrace()
            Result.Failure(e)
        }
    }

    override suspend fun signup(
        name: String, email: String, password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result?.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            e.printStackTrace()
            Result.Failure(e)
        }
    }

    override suspend fun resetPassword(email: String, onComplete: (String?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { if (it.isSuccessful) onComplete(null) }
            .addOnFailureListener { onComplete(it.message) }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun initCurrentUserIfFirstTime(
        department: String, onComplete: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = currentUserDocReference.get().await()
                if (!result.exists()) {
                    val newUser = User(
                        firebaseAuth.currentUser?.uid ?: "",
                        firebaseAuth.currentUser?.displayName ?: "",
                        firebaseAuth.currentUser?.email ?: "",
                        department,
                        null,
                        null
                    )
                    currentUserDocReference.set(newUser).await()
                    onComplete(null)
                } else {
                    onComplete(null)
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "initCurrentUserIfFirstTime: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
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

    override suspend fun updateCurrentUser(
        updateUserFieldMap: MutableMap<String, Any>, onComplete: (String?) -> Unit
    ) {
        val nameUpdate = updateUserFieldMap["displayName"].toString()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                currentUserDocReference.update(updateUserFieldMap).await()
                currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(nameUpdate).build()
                )?.await()
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "updateCurrentUser: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    override suspend fun sendFeedback(feedback: Feedback, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("feedback").document().set(feedback)
                onComplete(null)
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "sendFeedback: ${e.message}")
                onComplete(e.message)
                Firebase.crashlytics.recordException(e)
            }
        }
    }
}