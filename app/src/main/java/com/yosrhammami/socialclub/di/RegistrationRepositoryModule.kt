package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.RegistrationRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RegistrationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRegistrationRepository(
        impl: RegistrationRepositoryImpl
    ): RegistrationRepository
}