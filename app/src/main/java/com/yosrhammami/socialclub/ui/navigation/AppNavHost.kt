package com.yosrhammami.socialclub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.yosrhammami.socialclub.ui.HomeScreen
import com.yosrhammami.socialclub.ui.attendee.AttendeeScreen
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
            HomeScreen(
                onGetFromFirestoreClick = {
                    // Replace with a real test attendee document ID from your console
                    val idTest="s3wUfHDdH8VSthHtds2q"
                    navController.navigate(AttendeeRoute(personId = idTest))
                },
                onGetFromApiClick = {
                    navController.navigate(PeopleListRoute)
                }
            )
        }
        composable<AttendeeRoute> { backStackEntry ->
            val route: AttendeeRoute = backStackEntry.toRoute()
            AttendeeScreen(personId = route.personId)
        }

        composable<PeopleListRoute> {
            PeopleListScreen(
                onPersonClick = { personId ->
                    navController.navigate(PersonDetailRoute(personId))
                }
            )
        }
        composable<PersonDetailRoute> { backStackEntry ->
            val route: PersonDetailRoute = backStackEntry.toRoute()
            PersonDetailScreen(personId = route.personId) // <- explicit, visible value
        }

    }
}