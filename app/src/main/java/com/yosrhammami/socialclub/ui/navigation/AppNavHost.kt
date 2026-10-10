package com.yosrhammami.socialclub.ui.navigation

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.yosrhammami.socialclub.ui.createPassword.CreatePasswordScreen
import com.yosrhammami.socialclub.ui.guestDetail.AttendeeDetailScreen
import com.yosrhammami.socialclub.ui.currentAttendee.AttendeeScreen
import com.yosrhammami.socialclub.ui.guestList.EventAttendeesScreen
import com.yosrhammami.socialclub.ui.peopleList.PeopleListScreen
import com.yosrhammami.socialclub.ui.personDetail.PersonDetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(onValidEmail = {email ->
                navController.navigate(AttendeeRoute(email = email)) {
                    launchSingleTop = true
                }
            },
                onFirstConnection = {attendeeId, email ->
                    navController.navigate(CreatePasswordRoute(attendeeId = attendeeId, email = email)) {
                        launchSingleTop = true
                    }
                },
                onGetFromApiClick = {
                    navController.navigate(PeopleListRoute)
                })
        }
        composable<AttendeeRoute> {backStackEntry ->
            val route: AttendeeRoute = backStackEntry.toRoute() // needs no backStackEntry.toRoute() thinks to SavedStateHandle
            AttendeeScreen(
                onEventClick = {eventId ->
                    navController.navigate(
                        EventAttendeesRoute(
                            eventId = eventId
                        )
                    )
                })
        }
        composable<CreatePasswordRoute> {
            CreatePasswordScreen(onAccountCreated = {email ->
                // Pop the password screen so Back from the attendee screen returns to Home,
                // not to a form for an account that now exists.
                navController.navigate(AttendeeRoute(email = email)) {
                    popUpTo<CreatePasswordRoute> {inclusive = true}
                    launchSingleTop = true
                }
            })   // attendeeId + email come automatically via SavedStateHandle
        }
        composable<EventAttendeesRoute> {
            EventAttendeesScreen( onGuestClick = { guestId ->
                navController.navigate(AttendeeDetailRoute(attendeeId = guestId))
            })   // eventId comes automatically via SavedStateHandle
        }
        composable<AttendeeDetailRoute> {
            AttendeeDetailScreen()
        }

        composable<PeopleListRoute> {
            PeopleListScreen(onPersonClick = {personId ->
                navController.navigate(PersonDetailRoute(personId))
            })
        }
        composable<PersonDetailRoute> {backStackEntry ->
            val route: PersonDetailRoute = backStackEntry.toRoute()
            PersonDetailScreen(personId = route.personId) // <- explicit, visible value
        }

    }
}