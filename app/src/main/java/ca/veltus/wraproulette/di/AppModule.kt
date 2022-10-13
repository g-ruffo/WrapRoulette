package ca.veltus.wraproulette.di

import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import ca.veltus.wraproulette.data.repository.AuthenticationRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideAuthenticationRepository(implementation: AuthenticationRepositoryImpl): AuthenticationRepository = implementation

}