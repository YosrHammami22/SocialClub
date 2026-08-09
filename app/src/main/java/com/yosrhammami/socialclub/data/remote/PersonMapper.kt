package com.yosrhammami.socialclub.data.remote

import com.yosrhammami.socialclub.domain.model.Person
/*
data layer  →  knows about BOTH DTO and domain Person  →  mapper lives here
domain layer → knows ONLY about Person → never imports DTO
 */
fun PersonDto.toDomain(): Person {
    return Person(
        id = login.uuid,
        fullName = "${name.first} ${name.last}",
        email = email,
        city = location.city,
        country = location.country,
        age = dob.age,
        photoUrl = picture.large
    )
}