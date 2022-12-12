package ca.veltus.wraproulette.di

import ca.veltus.wraproulette.data.repository.*
import ca.veltus.wraproulette.utils.StringResourcesProvider
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
        firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore
    ): AuthenticationRepository {
        return AuthenticationRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun providePoolListRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        stringResourcesProvider: StringResourcesProvider
    ): PoolListRepository {
        return PoolListRepositoryImpl(firebaseAuth, firestore, stringResourcesProvider)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore
    ): HomeRepository {
        return HomeRepositoryImpl(firebaseAuth, firestore)
    }
}