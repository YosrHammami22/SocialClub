package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Person
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockGenerateIcebreakerUseCase @Inject constructor(): GenerateIcebreakerUseCase {

    override suspend fun invoke(person: Person): String {
        delay(1000)  // simulate network latency, so the Loading state is visible
        return "Ask ${person.fullName} what's the best thing about living in ${person.city} — " + "you might just find your next travel destination."
    }
}