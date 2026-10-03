package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.ContactRequestRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactRequestRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindContactRequestRepository(
        impl: ContactRequestRepositoryImpl
    ): ContactRequestRepository
}
