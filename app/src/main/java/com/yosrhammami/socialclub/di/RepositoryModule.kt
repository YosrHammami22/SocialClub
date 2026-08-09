package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.PersonRepositoryImpl
import com.yosrhammami.socialclub.domain.repository.PersonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds //used specifically when mapping an interface to its implementation. It must be an abstract class with an abstract fun, since Hilt generates the actual body.
    @Singleton   // <- this line is what actually enforces "only one instance, ever"
    abstract fun bindPersonRepository(
        impl: PersonRepositoryImpl
    ): PersonRepository
}