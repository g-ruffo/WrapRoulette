package ca.veltus.wraproulette.di

import android.content.Context
import ca.veltus.wraproulette.data.repository.*
import ca.veltus.wraproulette.utils.network.ConnectivityObserver
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
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
        firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore
    ): PoolListRepository {
        return PoolListRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore
    ): HomeRepository {
        return HomeRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}