package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.repository.PersonRepository
import javax.inject.Inject

class GetPersonDetailUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(id: String): Person? {
        // Simplest approach for now: refetch the list and find the match.
        // (We'll improve this once Room caching is added.)
        return personRepository.getPersonById(id)
    }
}