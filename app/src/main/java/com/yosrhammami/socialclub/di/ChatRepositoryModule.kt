package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.ChatRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRepositoryModule {


    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

}