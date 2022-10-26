package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.objects.Pool
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface PoolListRepository {
    val currentUser: FirebaseUser?
    suspend fun getPoolsList(): Flow<List<Pool>>
    fun addMemberToPool(poolId: String, uid: String, onComplete: () -> Unit)
    fun addUserToPool(poolId: String, uid: String, onComplete: () -> Unit)
    fun addPoolToUser(poolId: String, onComplete: () -> Unit)
    fun setActivePool(poolId: String, onComplete: () -> Unit)
    suspend fun joinPool(production: String, password: String, date: String, onComplete: () -> Unit)
    suspend fun createPool(pool: Pool, onComplete: () -> Unit)
}