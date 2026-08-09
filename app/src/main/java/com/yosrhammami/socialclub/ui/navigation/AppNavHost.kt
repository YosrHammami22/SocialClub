package com.yosrhammami.socialclub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.yosrhammami.socialclub.ui.peopleList.PeopleListScreen
import com.yosrhammami.socialclub.ui.personDetail.PersonDetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PeopleListRoute
    ) {

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