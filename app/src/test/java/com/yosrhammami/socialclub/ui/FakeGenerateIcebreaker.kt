package com.yosrhammami.socialclub.ui

import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.usecase.GenerateIcebreakerUseCase

class FakeGenerateIcebreaker: GenerateIcebreakerUseCase {

    override suspend fun invoke(person: Person): String {
        return "Icebreaker for ${person.fullName}"
    }
}