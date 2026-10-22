package com.yosrhammami.socialclub.di

import com.google.firebase.firestore.MemoryCacheSettings
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T9 · Config · Covers AC5 (with constraint C1: the app is online-only)
 *
 * Firestore keeps a DISK cache by default on Android. This test checks that
 * the settings your Hilt module provides use a memory-only cache instead,
 * so no previous user's documents are written to the phone's storage.
 *
 * Runs as an instrumented test because it uses the real Firebase SDK classes.
 */
class FirestoreSettingsTest {

    @Test
    fun firestoreSettings_useMemoryCacheOnly_noDiskPersistence() {
        val settings = FirestoreModule.provideFirestoreSettings()

        assertTrue(
            "Firestore must not persist data to disk (spec AC5 + constraint C1)",
            settings.cacheSettings is MemoryCacheSettings
        )
    }
}
