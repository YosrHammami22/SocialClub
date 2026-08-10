package com.yosrhammami.socialclub.data.remote

data class RandomUserResponse(
    val results: List<PersonDto>
)

data class PersonDto(
    val gender: String?=null,
    val name: NameDto,
    val email: String,
    val location: LocationDto,
    val picture: PictureDto,
    val phone: String,
    val dob: DobDto,
    val login: LoginDto
)

data class NameDto(val first: String, val last: String)
data class LocationDto(val city: String, val country: String)
data class PictureDto(val large: String, val thumbnail: String)
data class DobDto(val age: Int)
data class LoginDto(val uuid: String)
