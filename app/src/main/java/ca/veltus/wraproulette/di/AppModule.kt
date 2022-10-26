package ca.veltus.wraproulette.di

import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.data.repository.AuthenticationRepositoryImpl
import ca.veltus.wraproulette.data.repository.PoolListRepository
import ca.veltus.wraproulette.data.repository.PoolListRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthenticationRepository {
        return AuthenticationRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun providePoolListRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): PoolListRepository {
        return PoolListRepositoryImpl(firebaseAuth, firestore)
    }
}