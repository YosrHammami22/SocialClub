package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.core.util.AppLog
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.repository.PersonRepository
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val apiService: RandomUserApiService,
    private val logger: Logger          // <- injected, swappable
): PersonRepository {
    // Simple in-memory cache of the last fetched list
    private var cachedPeople: List<Person> = emptyList()

    override suspend fun getPeople(count: Int): List<Person> {
        val response = apiService.getPeople(count)
        val people = response.results.map { it.toDomain() }
        cachedPeople = people   // <- store the result for detail lookups
        logger.i( "cachedPeople: ${cachedPeople.count()}")
        return people
    }

    override suspend fun getPersonById(id: String): Person? {
        logger.i( "cachedPeople: ${cachedPeople.count()} id: ${id}")
        return cachedPeople.find { it.id == id }   // <- search the cache, no network call
    }

}
