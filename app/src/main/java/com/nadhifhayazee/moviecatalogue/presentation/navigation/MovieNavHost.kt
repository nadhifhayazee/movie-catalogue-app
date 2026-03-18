package com.nadhifhayazee.moviecatalogue.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nadhifhayazee.moviecatalogue.presentation.detail.DetailScreen
import com.nadhifhayazee.moviecatalogue.presentation.favorites.FavoritesScreen
import com.nadhifhayazee.moviecatalogue.presentation.home.HomeScreen
import com.nadhifhayazee.moviecatalogue.presentation.search.SearchScreen
import com.nadhifhayazee.moviecatalogue.presentation.seeall.SeeAllScreen

@Composable
fun MovieNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                },
                onSeeAllClick = { category ->
                    navController.navigate(Screen.SeeAll.createRoute(category))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) {
            DetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SeeAll.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            SeeAllScreen(
                category = category,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            )
        }

        composable(route = Screen.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            )
        }

        composable(route = Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Detail : Screen("movieDetail/{movieId}") {
        fun createRoute(movieId: Int) = "movieDetail/$movieId"
    }
    data object SeeAll : Screen("seeAll/{category}") {
        fun createRoute(category: String) = "seeAll/$category"
    }
    data object Favorites : Screen("favorites")
    data object Search : Screen("search")
}
