package ca.veltus.wraproulette.data.repository

import ca.veltus.wraproulette.data.objects.Member
import ca.veltus.wraproulette.data.objects.Message
import ca.veltus.wraproulette.data.objects.Pool
import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import java.util.*

interface HomeRepository {
    val currentUser: FirebaseUser?
    suspend fun getCurrentUserProfile(): Flow<User?>
    suspend fun getCurrentUser(onComplete: (User) -> Unit)
    suspend fun setUserPoolBet(
        poolId: String, userUid: String, bid: Date?, onComplete: (String?) -> Unit
    )

    suspend fun setPoolWrapTime(poolId: String, wrapTime: Date?, onComplete: (String?) -> Unit)
    suspend fun setPoolWinner(poolId: String, winners: List<Member>, onComplete: (String?) -> Unit)
    suspend fun addNewMemberToPool(member: Member, onComplete: (String?) -> Unit)
    suspend fun updateTempPoolMember(member: Member, onComplete: (String?) -> Unit)
    suspend fun deleteTempPoolMember(member: Member, onComplete: (String?) -> Unit)
    suspend fun getPoolData(poolId: String?): Flow<Pool?>
    suspend fun checkAdminForUpdate(poolId: String)
    suspend fun checkMembersForUpdate(poolId: String)
    suspend fun checkChatMessagesForUpdate(poolId: String)
    suspend fun getPoolMemberList(poolId: String): Flow<List<Member>>
    suspend fun leavePool(poolUid: String, userUid: String, onComplete: (String?) -> Unit)
    suspend fun sendChatMessage(activePool: String, message: Message, onComplete: (String?) -> Unit)
    suspend fun getChatList(activePool: String): Flow<List<Message>>
}