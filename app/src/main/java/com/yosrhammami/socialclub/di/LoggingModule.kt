package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.core.util.AppLog
import com.yosrhammami.socialclub.core.util.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {
    @Binds
    @Singleton
    abstract fun bindLogger(impl: AppLog): Logger
}
