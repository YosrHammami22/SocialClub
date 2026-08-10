package com.yosrhammami.socialclub.data.remote

import com.yosrhammami.socialclub.domain.model.Gender
import org.junit.Assert.*
import org.junit.Test

class PersonDtoTest{
    @Test
    fun `toDomain maps DTO fields correctly`() {
        val dto = PersonDto(
            name = NameDto("Jane", "Doe"),
            email = "jane@test.com",
            location = LocationDto("Paris", "France"),
            picture = PictureDto("large.jpg", "thumb.jpg"),
            phone = "123",
            dob = DobDto(29),
            login = LoginDto("uuid-1"),
            gender = "female"
        )
        val person = dto.toDomain()
        assertEquals("Jane Doe", person.fullName)
        assertEquals("Paris", person.city)
        assertEquals(29, person.age)
        assertEquals(Gender.FEMALE,person.gender)
    }

}