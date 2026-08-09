package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.repository.PersonRepository
import javax.inject.Inject

class GetPeopleUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {

    suspend operator fun invoke(count: Int): List<Person> {
        return personRepository.getPeople(count)
    }
}