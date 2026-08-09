package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.Person
//This is the important part for testing later. Create the interface in your domain layer:
interface PersonRepository {
    suspend fun getPeople(count: Int): List<Person>
    suspend fun getPersonById(id: String): Person?
}