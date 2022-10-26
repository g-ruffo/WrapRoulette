package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.Resource
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser>
    fun logout()
    suspend fun getCurrentUserProfile(): Flow<User?>

}