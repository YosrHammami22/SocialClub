package com.yosrhammami.socialclub.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PairIdTest {

    @Test
    fun `given two uids in either order when building the pair id then both ids are the same`() {
        assertEquals(pairIdOf("uidA", "uidB"), pairIdOf("uidB", "uidA"))
    }

    @Test
    fun `given uidA sorts before uidB when building the pair id then it is uidA_uidB`() {
        assertEquals("uidA_uidB", pairIdOf("uidB", "uidA"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a blank uid when building the pair id then IllegalArgumentException is thrown`() {
        pairIdOf("   ", "uidB")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a uid containing an underscore when building the pair id then IllegalArgumentException is thrown`() {
        pairIdOf("uid_A", "uidB")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given the same uid twice when building the pair id then IllegalArgumentException is thrown`() {
        pairIdOf("uidA", "uidA")
    }
}
