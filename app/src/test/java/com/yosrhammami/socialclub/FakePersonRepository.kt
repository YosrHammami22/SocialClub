package com.yosrhammami.socialclub

import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.repository.PersonRepository

class FakePersonRepository: PersonRepository {

    var peopleToReturn: List<Person> = emptyList()
    var shouldThrowError: Boolean = false

    override suspend fun getPeople(count: Int): List<Person> {
        if (shouldThrowError) {
            throw Exception("Network error")
        }
        return peopleToReturn
    }

    override suspend fun getPersonById(id: String): Person? {
        return peopleToReturn.find {it.id == id}
    }

}