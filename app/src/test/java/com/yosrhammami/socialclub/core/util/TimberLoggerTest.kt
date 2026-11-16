package com.yosrhammami.socialclub.core.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class TimberLoggerTest {

    private data class Entry(val priority: Int, val tag: String?, val message: String, val t: Throwable?)

    // A recording Tree instead of DebugTree: DebugTree writes to android.util.Log,
    // which isn't available in JVM unit tests.
    private val entries = mutableListOf<Entry>()
    private val recordingTree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            entries += Entry(priority, tag, message, t)
        }
    }

    private val logger = TimberLogger()

    @Before
    fun plant() = Timber.plant(recordingTree)

    @After
    fun uproot() = Timber.uprootAll()

    @Test
    fun `i forwards the message at INFO priority with the app tag`() {
        logger.i("attendee loaded")

        assertEquals(listOf(Entry(INFO, "So_Social", "attendee loaded", null)), entries)
    }

    @Test
    fun `d forwards the message at DEBUG priority with the app tag`() {
        logger.d("cache hit")

        assertEquals(listOf(Entry(DEBUG, "So_Social", "cache hit", null)), entries)
    }

    @Test
    fun `e forwards the message and the throwable at ERROR priority with the app tag`() {
        val error = IllegalStateException("boom")

        logger.e("load failed", error)

        val entry = entries.single()
        assertEquals(ERROR, entry.priority)
        assertEquals("So_Social", entry.tag)
        assertEquals(error, entry.t)
    }

    private companion object {
        // android.util.Log constants, inlined: reading them from android.jar in a JVM test isn't reliable.
        const val DEBUG = 3
        const val INFO = 4
        const val ERROR = 6
    }
}