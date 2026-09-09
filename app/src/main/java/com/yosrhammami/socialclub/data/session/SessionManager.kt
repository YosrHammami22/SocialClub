package com.yosrhammami.socialclub.data.session

import com.yosrhammami.socialclub.domain.model.Attendee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
/*
No Hilt module needed — since SessionManager has an @Inject constructor() with no interface to bind, Hilt can construct it directly; @Singleton alone (no separate @Provides/@Binds) is enough.
 */
@Singleton
class SessionManager @Inject constructor(){
    private val _currentAttendee = MutableStateFlow<Attendee?>(null)
    val currentAttendee: StateFlow<Attendee?> = _currentAttendee.asStateFlow()

    fun setCurrentAttendee(attendee: Attendee) {
        _currentAttendee.value = attendee
    }

    fun clear() {
        _currentAttendee.value = null
    }
}