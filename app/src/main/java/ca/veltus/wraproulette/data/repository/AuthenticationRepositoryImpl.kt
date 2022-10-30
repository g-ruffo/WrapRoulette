package ca.veltus.wraproulette.data.repository

import android.util.Log
import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.User
import ca.veltus.wraproulette.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthenticationRepository {

    companion object {
        private const val TAG = "AuthenticationRepoLog"
    }

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun getCurrentUserProfile(): Flow<User?> {
        return firestore.collection("users").document(currentUser!!.uid).snapshots()
            .map<DocumentSnapshot, User?> { it.toObject() }.onCompletion {
                Log.i(TAG, "getCurrentUserProfile: $it")
            }.catch {
                Log.e(TAG, "getCurrentUserProfile: $it")
            }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Failure(e)
        }
    }

    override suspend fun signup(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result?.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Failure(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}