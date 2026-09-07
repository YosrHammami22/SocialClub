package com.yosrhammami.socialclub.ui.navigation

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.yosrhammami.socialclub.ui.GuestDetail.AttendeeDetailScreen
import com.yosrhammami.socialclub.ui.currentAttendee.AttendeeScreen
import com.yosrhammami.socialclub.ui.GuestList.EventAttendeesScreen
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