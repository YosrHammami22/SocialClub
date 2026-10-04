package com.yosrhammami.socialclub.ui.util

import androidx.annotation.StringRes
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState

fun formatDate(epochMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(epochMillis))
}

