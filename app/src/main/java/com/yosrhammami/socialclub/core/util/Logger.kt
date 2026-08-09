package com.yosrhammami.socialclub.core.util

interface Logger {
    fun i(message: String)
    fun d(message: String)
    fun e(message: String, throwable: Throwable? = null)
}