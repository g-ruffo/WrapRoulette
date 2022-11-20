package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun signup(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun resetPassword(email: String, onComplete: (String?) -> Unit)
    fun logout()
    suspend fun getCurrentUserProfile(): Flow<User?>
    suspend fun initCurrentUserIfFirstTime(department: String, onComplete: (String?) -> Unit)
    suspend fun getCurrentUser(onComplete: (User) -> Unit)
    suspend fun updateCurrentUser(
        updateUserFieldMap: MutableMap<String, Any>, onComplete: (String?) -> Unit
    )
}