package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.domain.usecase.GenerateIcebreakerUseCase
import com.yosrhammami.socialclub.domain.usecase.MockGenerateIcebreakerUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindGenerateIcebreakerUseCase(
        impl: MockGenerateIcebreakerUseCase
    ): GenerateIcebreakerUseCase
}