package com.yosrhammami.socialclub

import com.yosrhammami.socialclub.core.util.Logger

class FakeLogger: Logger {

    override fun i(message: String) {

    }

    override fun d(message: String) {

    }

    override fun e(
        message: String,
        throwable: Throwable?
    ) {
    }

}