package com.yosrhammami.socialclub.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /*
    Memory cache only, no disk persistence (Firestore's Android default). The app is online-only (spec C1),
    so a disk cache adds nothing, and it would keep the previous user's documents on the device after
    logout (AC5). clearPersistence() was rejected: it needs a terminated instance, which can't be reused
    as this Hilt singleton (plan.md).
     */
    @Provides
    @Singleton
    fun provideFirestoreSettings(): FirebaseFirestoreSettings =
        FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(memoryCacheSettings { })
            .build()

    // Settings must be applied before the first read/write; Hilt providing this singleton is that first use.
    @Provides
    @Singleton
    fun provideFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore =
        FirebaseFirestore.getInstance().apply { firestoreSettings = settings }
}
