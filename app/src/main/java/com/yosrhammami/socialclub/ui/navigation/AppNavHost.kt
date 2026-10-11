package com.yosrhammami.socialclub.ui.navigation

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.ui.createPassword.CreatePasswordScreen
import com.yosrhammami.socialclub.ui.guestDetail.AttendeeDetailScreen
import com.yosrhammami.socialclub.ui.currentAttendee.AttendeeScreen
import com.yosrhammami.socialclub.ui.guestList.EventAttendeesScreen
import com.yosrhammami.socialclub.ui.peopleList.PeopleListScreen
import com.yosrhammami.socialclub.ui.personDetail.PersonDetailScreen
import com.yosrhammami.socialclub.ui.signIn.SignInScreen

@Composable
fun AppNavHost( logger: Logger) {
    val navController = rememberNavController()
// Logs every screen change, in one place
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            logger.d("Navigation -> ${destination.route}")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(onValidEmail = {email ->
                val signedInEmail = FirebaseAuth.getInstance().currentUser?.email  // Who is using this app? The account logged in on this device right now
                logger.i("onValidEmail email=$email signedInEmail=$signedInEmail")
                // Session still alive for this attendee -> straight in. Otherwise (reinstall, new device,
                // cleared data, or another account signed in) the password is asked again.
                if (signedInEmail.equals(email, ignoreCase = true)) {
                    navController.navigate(AttendeeRoute(email = email)) { launchSingleTop = true }
                } else {
                    navController.navigate(SignInRoute(email = email)) { launchSingleTop = true }
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
        composable<SignInRoute> {
            SignInScreen(onSignedIn = {email ->
                // Same reasoning as CreatePassword: Back from the attendee screen returns to Home,
                // not to a password form for a session that now exists.
                navController.navigate(AttendeeRoute(email = email)) {
                    popUpTo<SignInRoute> {inclusive = true}
                    launchSingleTop = true
                }
            })   // email comes automatically via SavedStateHandle
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