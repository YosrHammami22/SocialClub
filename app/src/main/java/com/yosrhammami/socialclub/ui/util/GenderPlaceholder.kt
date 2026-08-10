package com.yosrhammami.socialclub.ui.util

import androidx.annotation.DrawableRes
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Gender

@DrawableRes
fun Gender.toPlaceholderDrawable(): Int {
    return when (this) {
        Gender.MALE -> R.drawable.ic_avatar_male
        Gender.FEMALE -> R.drawable.ic_avatar_female
        Gender.UNKNOWN -> R.drawable.ic_avatar_neutral
    }
}