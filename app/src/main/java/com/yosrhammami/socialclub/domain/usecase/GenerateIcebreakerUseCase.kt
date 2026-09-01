package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Person

interface GenerateIcebreakerUseCase {
    suspend operator fun invoke(person: Person): String
}