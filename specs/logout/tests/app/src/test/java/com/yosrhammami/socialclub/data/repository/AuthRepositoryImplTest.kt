package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.remote.AuthRemoteDataSource
import com.yosrhammami.socialclub.data.session.DeliberateLogoutFlag
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.model.Attendee
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T2 · Data · Covers AC5, AC6
 *
 * The most important decision of the Plan, locked in a test:
 *   1. mark the logout as deliberate  (AC6: no "session expired" message)
 *   2. clear the session              (AC5: nothing left for the next user)
 *   3. sign out LAST                  (never "logged out" while data is still there)
 *
 * Project rule: mock interfaces only, never concrete classes.
 * So SessionManager and DeliberateLogoutFlag are REAL objects, and only
 * AuthRemoteDataSource (an interface) is mocked.
 *
 * How the order is checked without verifyOrder: when signOut() is called,
 * the fake records what the session and the flag look like AT THAT MOMENT.
 * If someone moves signOut() before clear(), the session is still Alice
 * at that moment, and the test turns red.
 */
class AuthRepositoryImplTest {

    private val sessionManager = SessionManager()
    private val logoutFlag = DeliberateLogoutFlag()
    private val remoteDataSource: AuthRemoteDataSource = mockk()

    private val repository = AuthRepositoryImpl(
        remoteDataSource = remoteDataSource,
        sessionManager = sessionManager,
        logoutFlag = logoutFlag
    )

    @Test
    fun `when signOut is called, session is already cleared and logout is marked deliberate`() = runTest {
        // ADAPT: use the real setter name from your SessionManager.
        sessionManager.setAttendee(mockk<Attendee>())

        var attendeeAtSignOut: Attendee? = mockk()
        var deliberateAtSignOut = false
        every { remoteDataSource.signOut() } answers {
            attendeeAtSignOut = sessionManager.currentAttendee.value
            deliberateAtSignOut = logoutFlag.consume()
        }

        repository.logout()

        verify(exactly = 1) { remoteDataSource.signOut() }
        assertNull("Session must be cleared BEFORE signOut()", attendeeAtSignOut)
        assertTrue("Logout must be marked deliberate BEFORE signOut()", deliberateAtSignOut)
    }
}
