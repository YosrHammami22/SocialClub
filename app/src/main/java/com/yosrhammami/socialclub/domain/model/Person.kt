package com.yosrhammami.socialclub.domain.model

data class Person(
    val id: String,
    val fullName: String,
    val email: String,
    val city: String,
    val country: String,
    val age: Int,
    val photoUrl: String,
    val gender: Gender= Gender.UNKNOWN
)
enum class Gender {
    MALE, FEMALE, UNKNOWN
}
