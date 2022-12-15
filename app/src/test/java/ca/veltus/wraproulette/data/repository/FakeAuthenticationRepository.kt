package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.Result
import ca.veltus.wraproulette.data.objects.Feedback
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

class FakeAuthenticationRepository() : AuthenticationRepository {

    override val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        TODO("Not yet implemented")
    }

    override suspend fun signup(
        name: String, email: String, password: String
    ): Result<FirebaseUser> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(email: String, onComplete: (String?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun logout() {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUserProfile(): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun initCurrentUserIfFirstTime(
        department: String, onComplete: (String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(onComplete: (User) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCurrentUser(
        updateUserFieldMap: MutableMap<String, Any>, onComplete: (String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun sendFeedback(feedback: Feedback, onComplete: (String?) -> Unit) {
        TODO("Not yet implemented")
    }

}