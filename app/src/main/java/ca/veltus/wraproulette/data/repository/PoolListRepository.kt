package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface PoolListRepository {
    val currentUser: FirebaseUser?
    suspend fun getPoolsList(userUid: String): Flow<List<Pool>>
    suspend fun getEditPool(poolId: String, onComplete: (Pool?) -> Unit)
    suspend fun deletePool(poolUid: String, onComplete: (String?) -> Unit)
    suspend fun getCurrentUserProfile(): Flow<User?>
    suspend fun createPool(pool: Pool, onComplete: (String?) -> Unit)
    suspend fun updatePool(pool: Pool, onComplete: (String?) -> Unit)
    suspend fun joinPool(
        production: String, password: String, date: String, onComplete: (String?) -> Unit
    )
}