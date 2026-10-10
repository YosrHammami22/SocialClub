package com.yosrhammami.socialclub.di

import com.google.firebase.auth.FirebaseAuth
import com.yosrhammami.socialclub.data.repository.AuthRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    // @Provides can't live directly in an abstract @Binds module, hence the companion object.
    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
