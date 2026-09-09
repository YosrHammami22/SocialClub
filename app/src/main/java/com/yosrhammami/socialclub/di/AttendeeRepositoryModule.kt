package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.AttendeeRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttendeeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAttendeeRepository(
        impl: AttendeeRepositoryImpl
    ): AttendeeRepository
}