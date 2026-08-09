package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.data.FakeRandomUserApiService
import com.yosrhammami.socialclub.data.remote.DobDto
import com.yosrhammami.socialclub.data.remote.LocationDto
import com.yosrhammami.socialclub.data.remote.LoginDto
import com.yosrhammami.socialclub.data.remote.NameDto
import com.yosrhammami.socialclub.data.remote.PersonDto
import com.yosrhammami.socialclub.data.remote.PictureDto
import com.yosrhammami.socialclub.data.remote.RandomUserResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PersonRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val fakeApiService = FakeRandomUserApiService()
    private val fakeLogger = FakeLogger()   // <- no Android framework involved at all

    @Test
    fun `getPersonById returns null when cache is empty`() = runTest {
        val repo = PersonRepositoryImpl(
            fakeApiService,
            fakeLogger
        )
        val result = repo.getPersonById("some-id")
        assertNull(result)
    }

    @Test
    fun `getPersonById finds person after getPeople was called`() = runTest {
        val dto = PersonDto(
            name = NameDto("Jane", "Doe"),
            email = "jane@test.com",
            location = LocationDto("Paris", "France"),
            picture = PictureDto("large.jpg", "thumb.jpg"),
            phone = "123",
            dob = DobDto(29),
            login = LoginDto("uuid-1")
        )
        fakeApiService.responseToReturn = RandomUserResponse(results = listOf(dto))

        val repo = PersonRepositoryImpl(fakeApiService, fakeLogger)
        repo.getPeople(1)                          // populates the cache
        val result = repo.getPersonById("uuid-1")  // should now find it

        assertEquals("Jane Doe", result?.fullName)
    }
}