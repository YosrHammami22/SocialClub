package com.yosrhammami.socialclub.ui.navigation

import HomeScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.ui.createPassword.CreatePasswordScreen
import com.yosrhammami.socialclub.ui.guestDetail.AttendeeDetailScreen
import com.yosrhammami.socialclub.ui.currentAttendee.AttendeeScreen
import com.yosrhammami.socialclub.ui.guestList.EventAttendeesScreen
import com.yosrhammami.socialclub.ui.peopleList.PeopleListScreen
import com.yosrhammami.socialclub.ui.personDetail.PersonDetailScreen
import com.yosrhammami.socialclub.ui.settings.SettingsScreen
import com.yosrhammami.socialclub.ui.signIn.SignInScreen

@Composable
fun AppNavHost(
    logger: Logger,
    // Obtained here, outside the NavHost, so it's scoped to the Activity and outlives every screen.
    authObserver: AuthObserverViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val sessionExpiredMessage = stringResource(R.string.session_expired)

    // Every way of being logged out lands here (spec AC1, AC6, AC7). The snackbar lives above the NavHost,
    // so it survives the navigation that destroys the screen the user was on.
    LaunchedEffect(authObserver) {
        authObserver.navigation.collect { navigation ->
            if (navigation is AuthNavigation.ToSignIn) {
                logger.d("Logged out (sessionExpired=${navigation.showSessionExpired}) -> Home")
                navController.navigateToHomeClearingBackStack()
                if (navigation.showSessionExpired) snackbarHostState.showSnackbar(sessionExpiredMessage)
            }
        }
    }
// Logs every screen change, in one place
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            logger.d("Navigation -> ${destination.route}")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> {
                // The NavHost only maps outcomes to routes; which outcome applies (session or not)
                // is decided in CheckFirstConnectionUseCase, where it is unit-tested.
                HomeScreen(onValidEmail = {email ->
                    navController.navigate(AttendeeRoute(email = email)) { launchSingleTop = true }
                },
                    onSignInRequired = {email ->
                        navController.navigate(SignInRoute(email = email)) { launchSingleTop = true }
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
                    },
                    onSettingsClick = {
                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                    })
            }
            composable<SettingsRoute> {
                // No navigation callback on purpose: after logout, the auth observer above navigates.
                SettingsScreen()
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }
}