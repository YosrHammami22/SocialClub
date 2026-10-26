package com.yosrhammami.socialclub.core.util

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RunSuspendCatchingTest {

    @Test
    fun `when block succeeds, returns success with its value`() {
        val result = runSuspendCatching { "sent" }

        assertEquals("sent", result.getOrNull())
    }

    @Test
    fun `when block throws, returns failure with that exception`() {
        val result = runSuspendCatching { throw IllegalStateException("PERMISSION_DENIED") }

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("PERMISSION_DENIED", result.exceptionOrNull()?.message)
    }

    // The whole point of the helper: plain runCatching would return Result.failure here.
    @Test
    fun `when block is cancelled, rethrows CancellationException instead of returning failure`() {
        val error = assertThrows(CancellationException::class.java) {
            runSuspendCatching { throw CancellationException("screen left") }
        }

        assertEquals("screen left", error.message)
    }
}
