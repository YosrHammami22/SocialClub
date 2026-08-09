package com.yosrhammami.socialclub.di

import com.yosrhammami.socialclub.data.repository.RandomUserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/*
Use @Provides because Hilt doesn't know how to build external objects (like Retrofit) or Interfaces automatically.
Use @Singleton to save memory and ensure that different parts of your app are sharing the exact same data/connection.
 */

@Module //Modules (classes marked with @Module) to tell Hilt how to create and manage the objects you want to inject.
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides  // Tells Hilt: "Use this function to create Retrofit"
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {level = HttpLoggingInterceptor.Level.BODY}

    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://randomuser.me/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideRandomUserApiService(retrofit: Retrofit): RandomUserApiService =
        retrofit.create(RandomUserApiService::class.java)
}